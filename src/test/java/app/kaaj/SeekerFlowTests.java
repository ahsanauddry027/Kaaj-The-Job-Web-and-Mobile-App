package app.kaaj;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * The job seeker side of the Kotlin/Android build, end to end: profile, search, saved jobs, one-click
 * apply, the employer's ranked applicants and decisions, the timeline and withdrawing.
 * Needs Docker Desktop (Testcontainers); without Docker these tests are skipped.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@TestPropertySource(properties = {
        "DB_PASSWORD=unused",
        "kaaj.admin.email=admin@test.kaaj",
        "kaaj.admin.password=Admin@12345",
        "kaaj.files.dir=target/test-uploads"
})
class SeekerFlowTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired
    MockMvc mvc;

    @Test
    void aJobSeekerFindsAJobAppliesAndTheEmployerShortlistsThem() throws Exception {
        MockHttpSession employer = verifiedEmployer();
        String jobId = publishedJob(employer, "ড্রাইভার");
        MockHttpSession seeker = signUp("seeker-" + UUID.randomUUID() + "@test.kaaj", "SEEKER");

        // No profile yet: the profile page says so, and applying is refused.
        mvc.perform(get("/api/v1/me/profile").session(seeker)).andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/jobs/" + jobId + "/applications").session(seeker).with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("application.profile-needed"));

        // The profile: the home point is rounded, and the meter shows what's missing.
        mvc.perform(put("/api/v1/me/profile").session(seeker).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"displayName": "রহিম", "phone": "01811-223344", "occupationIds": ["%s"],
                                 "skillIds": [], "yearsExperience": 5, "documents": ["DRIVING_PRO_LIGHT"],
                                 "homeLatitude": 23.8913, "homeLongitude": 90.4049, "homeAreaName": "Tongi",
                                 "travelRadiusKm": 10, "expectedWage": 18000, "wagePeriod": "MONTH", "shifts": ["DAY"]}
                                """.formatted(occupationId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.homeLatitude").value(23.89))
                .andExpect(jsonPath("$.readyToApply").value(true))
                .andExpect(jsonPath("$.missing", hasItem("skills")));

        // Search: a Banglish misspelling finds the post, nearest first, with the distance.
        mvc.perform(get("/api/v1/jobs").param("q", "draiver").param("lat", "23.89").param("lng", "90.405")
                        .param("radiusKm", "10").param("sort", "NEAREST").session(seeker))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].job.id", hasItem(jobId)));

        // Save it for later.
        mvc.perform(put("/api/v1/me/saved-jobs/" + jobId).session(seeker).with(csrf())).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/me/saved-jobs").session(seeker))
                .andExpect(jsonPath("$[*].job.id", hasItem(jobId)));

        // One-click apply; the same Idempotency-Key gives back the same application.
        String key = UUID.randomUUID().toString();
        String first = mvc.perform(post("/api/v1/jobs/" + jobId + "/applications").session(seeker).with(csrf())
                        .header("Idempotency-Key", key).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\": \"৫ বছরের অভিজ্ঞতা\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("APPLIED"))
                .andReturn().getResponse().getContentAsString();
        String applicationId = JsonPath.read(first, "$.id");
        mvc.perform(post("/api/v1/jobs/" + jobId + "/applications").session(seeker).with(csrf())
                        .header("Idempotency-Key", key))
                .andExpect(jsonPath("$.id").value(applicationId));
        mvc.perform(post("/api/v1/jobs/" + jobId + "/applications").session(seeker).with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("application.already-applied"));

        // The employer sees the applicant with the score and reasons, but not the phone yet.
        mvc.perform(get("/api/v1/employer/jobs/" + jobId + "/applicants").session(employer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].applicationId").value(applicationId))
                .andExpect(jsonPath("$[0].reasons[*].code", hasItem("OCCUPATION_MATCH")))
                .andExpect(jsonPath("$[0].seeker.phone").doesNotExist());

        // Opening it marks it viewed; shortlisting shares the phone number.
        mvc.perform(get("/api/v1/employer/applications/" + applicationId).session(employer))
                .andExpect(jsonPath("$.status").value("VIEWED"));
        mvc.perform(post("/api/v1/employer/applications/" + applicationId + "/status").session(employer).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"SHORTLISTED\", \"note\": \"কাল ১০টায় আসুন\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHORTLISTED"))
                .andExpect(jsonPath("$.seeker.phone").value("01811-223344"));
        mvc.perform(post("/api/v1/employer/applications/" + applicationId + "/status").session(employer).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\": \"APPLIED\"}"))
                .andExpect(status().isBadRequest());

        // The job seeker follows it on "My applications", with the timeline.
        String mine = mvc.perform(get("/api/v1/me/applications/" + applicationId).session(seeker))
                .andExpect(jsonPath("$.status").value("SHORTLISTED"))
                .andReturn().getResponse().getContentAsString();
        assertThat(JsonPath.<java.util.List<String>>read(mine, "$.timeline[*].toStatus"))
                .containsExactly("APPLIED", "VIEWED", "SHORTLISTED");

        // Withdrawing hides the application from the employer.
        mvc.perform(post("/api/v1/me/applications/" + applicationId + "/withdraw").session(seeker).with(csrf()))
                .andExpect(jsonPath("$.status").value("WITHDRAWN"));
        mvc.perform(get("/api/v1/employer/jobs/" + jobId + "/applicants").session(employer))
                .andExpect(jsonPath("$.length()").value(0));

        mvc.perform(delete("/api/v1/me/saved-jobs/" + jobId).session(seeker).with(csrf())).andExpect(status().isNoContent());
    }

    @Test
    void onlyJobSeekersReachTheirPagesAndOnlyOwnersSeeApplicants() throws Exception {
        MockHttpSession employer = verifiedEmployer();
        String jobId = publishedJob(employer, "নিরাপত্তা প্রহরী");
        mvc.perform(get("/api/v1/me/profile").session(employer)).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/jobs/" + jobId + "/applications").session(employer).with(csrf()))
                .andExpect(status().isForbidden());

        MockHttpSession otherEmployer = verifiedEmployer();
        mvc.perform(get("/api/v1/employer/jobs/" + jobId + "/applicants").session(otherEmployer))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/v1/jobs").param("cursor", "junk")).andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------------ helpers

    private String publishedJob(MockHttpSession employer, String title) throws Exception {
        String body = """
                {"titleBn": "%s", "descriptionBn": "Test post", "occupationId": "%s", "jobType": "FULL_TIME",
                 "shift": "DAY", "hours": "08:00-18:00", "openings": 2, "wageMin": 16000, "wageMax": 20000,
                 "wagePeriod": "MONTH", "latitude": 23.8916, "longitude": 90.4023, "areaName": "Tongi, Gazipur",
                 "minExperienceYears": 1, "skillIds": [], "requiredDocuments": ["DRIVING_PRO_LIGHT"]}
                """.formatted(title, occupationId());
        String job = mvc.perform(post("/api/v1/employer/jobs").param("submit", "true").session(employer).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(job, "$.id");
    }

    private MockHttpSession verifiedEmployer() throws Exception {
        MockHttpSession employer = signUp("emp-" + UUID.randomUUID() + "@test.kaaj", "EMPLOYER");
        MockHttpSession admin = signIn("admin@test.kaaj", "Admin@12345");
        mvc.perform(put("/api/v1/employer/profile").session(employer).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "Test Business", "kind": "SME", "address": "Station Road, Tongi, Gazipur",
                         "contactPerson": "Md. Karim", "contactPhone": "01712-345678"}
                        """)).andExpect(status().isOk());
        String profile = mvc.perform(multipart("/api/v1/employer/profile/document").file(licence()).session(employer).with(csrf()))
                .andReturn().getResponse().getContentAsString();
        mvc.perform(post("/api/v1/admin/employers/" + JsonPath.read(profile, "$.id") + "/verification").session(admin)
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"decision\":\"APPROVE\"}"))
                .andExpect(status().isOk());
        return employer;
    }

    private MockHttpSession signUp(String email, String role) throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/api/v1/auth/register").session(session).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"Secret@123\",\"role\":\"%s\"}".formatted(email, role)))
                .andExpect(status().isOk());
        return session;
    }

    private MockHttpSession signIn(String email, String password) throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/api/v1/auth/login").session(session).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)))
                .andExpect(status().isOk());
        return session;
    }

    private String occupationId() throws Exception {
        String found = mvc.perform(get("/api/v1/taxonomy/occupations").param("q", "driver"))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(found, "$[0].id");
    }

    private static MockMultipartFile licence() throws IOException {
        BufferedImage image = new BufferedImage(60, 40, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return new MockMultipartFile("file", "licence.png", "image/png", out.toByteArray());
    }
}
