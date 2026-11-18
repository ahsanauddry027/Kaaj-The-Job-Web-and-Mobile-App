package app.kaaj;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import app.kaaj.job.internal.JobService;
import com.jayway.jsonpath.JsonPath;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
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
 * Member-3's two features end to end, through the real HTTP API and a real PostgreSQL 18 started by
 * Testcontainers (Docker Desktop must be running; without Docker these tests are skipped, not failed).
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
class Member3FlowTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired
    MockMvc mvc;

    @Autowired
    JobService jobService;

    // ------------------------------------------------------------------ Feature 3 + Feature 4 together

    @Test
    void anUnverifiedEmployersPostWaitsAndGoesLiveWhenTheAdminApproves() throws Exception {
        MockHttpSession karim = signUp("karim-" + UUID.randomUUID() + "@test.kaaj", "EMPLOYER");
        MockHttpSession admin = signIn("admin@test.kaaj", "Admin@12345");

        // No business profile yet: no profile, and no posting
        mvc.perform(get("/api/v1/employer/profile").session(karim)).andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/employer/jobs").session(karim).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(job("Pickup driver", "16000", "20000")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("job.employer-profile-missing"));

        // Feature 3: the business profile
        mvc.perform(put("/api/v1/employer/profile").session(karim).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(profile("Karim Transport")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNVERIFIED"))
                .andExpect(jsonPath("$.missing[0]").value("document"))
                .andExpect(jsonPath("$.canPublishJobs").value(false));

        // Feature 4: a complete post from an unverified employer waits as a draft
        String created = mvc.perform(post("/api/v1/employer/jobs?submit=true").session(karim).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(job("Pickup driver", "16000", "20000")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.awaitingVerification").value(true))
                .andExpect(jsonPath("$.employer.verified").value(false))
                .andReturn().getResponse().getContentAsString();
        String jobId = JsonPath.read(created, "$.id");

        // Upload the trade licence: the profile goes to the admin queue
        String profile = mvc.perform(multipart("/api/v1/employer/profile/document").file(licence()).session(karim)
                        .param("documentType", "TRADE_LICENCE").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.documentName").value("licence.png"))
                .andReturn().getResponse().getContentAsString();
        String employerId = JsonPath.read(profile, "$.id");

        String queue = mvc.perform(get("/api/v1/admin/employers?status=PENDING").session(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].id", hasItem(employerId)))
                .andReturn().getResponse().getContentAsString();
        List<String> documentUrls = JsonPath.read(queue, "$.items[?(@.id == '" + employerId + "')].documentUrl");
        String documentUrl = documentUrls.getFirst();

        // The document is for admins only
        mvc.perform(get(documentUrl).session(karim)).andExpect(status().isNotFound());
        mvc.perform(get(documentUrl).session(admin)).andExpect(status().isOk());

        // Only admins reach the queue; only employers reach the job API
        mvc.perform(get("/api/v1/admin/employers").session(karim)).andExpect(status().isForbidden());
        MockHttpSession rahim = signUp("rahim-" + UUID.randomUUID() + "@test.kaaj", "SEEKER");
        mvc.perform(get("/api/v1/employer/jobs").session(rahim)).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/employer/jobs")).andExpect(status().isUnauthorized());

        // The admin approves: the waiting post goes live with the Verified badge
        mvc.perform(post("/api/v1/admin/employers/" + employerId + "/verification").session(admin).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"decision\":\"APPROVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VERIFIED"));

        mvc.perform(get("/api/v1/employer/jobs/" + jobId).session(karim))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.employer.verified").value(true))
                .andExpect(jsonPath("$.expiresAt").exists());
        mvc.perform(get("/api/v1/jobs/" + jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employer.verified").value(true))
                .andExpect(jsonPath("$.wageMin").value(16000));

        // Another employer can't see or change this post
        MockHttpSession other = signUp("other-" + UUID.randomUUID() + "@test.kaaj", "EMPLOYER");
        mvc.perform(put("/api/v1/employer/profile").session(other).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(profile("Other Shop"))).andExpect(status().isOk());
        mvc.perform(get("/api/v1/employer/jobs/" + jobId).session(other)).andExpect(status().isNotFound());
        mvc.perform(patch("/api/v1/employer/jobs/" + jobId).session(other).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"action\":\"CLOSE\"}"))
                .andExpect(status().isNotFound());

        // Manage: pause, pause again (refused), resume, close, repost
        action(karim, jobId, "PAUSE").andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PAUSED"));
        action(karim, jobId, "PAUSE").andExpect(status().isConflict());
        action(karim, jobId, "RESUME").andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PUBLISHED"));
        action(karim, jobId, "CLOSE").andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CLOSED"));
        mvc.perform(get("/api/v1/jobs/" + jobId)).andExpect(status().isNotFound());
        action(karim, jobId, "REPOST").andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.repostedFrom").value(jobId));

        // A verified employer's new post is published straight away
        mvc.perform(post("/api/v1/employer/jobs?submit=true").session(karim).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(job("Microbus driver", "18000", "22000")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        mvc.perform(get("/api/v1/employer/jobs/summary").session(karim))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.PUBLISHED").value(1))
                .andExpect(jsonPath("$.CLOSED").value(1))
                .andExpect(jsonPath("$.DRAFT").value(1));
    }

    // ------------------------------------------------------------------ Feature 3: rejection

    @Test
    void aRejectedEmployerSeesTheReasonFixesItAndIsReviewedAgain() throws Exception {
        MockHttpSession shop = signUp("shop-" + UUID.randomUUID() + "@test.kaaj", "EMPLOYER");
        MockHttpSession admin = signIn("admin@test.kaaj", "Admin@12345");
        mvc.perform(put("/api/v1/employer/profile").session(shop).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(profile("Tongi Store"))).andExpect(status().isOk());
        String profile = mvc.perform(multipart("/api/v1/employer/profile/document").file(licence()).session(shop).with(csrf()))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String employerId = JsonPath.read(profile, "$.id");

        mvc.perform(post("/api/v1/admin/employers/" + employerId + "/verification").session(admin).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"decision\":\"REJECT\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.reason").exists());
        mvc.perform(post("/api/v1/admin/employers/" + employerId + "/verification").session(admin).with(csrf())
                        .header("Accept-Language", "en")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"decision\":\"REJECT\",\"reason\":\"The licence is not readable\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        mvc.perform(get("/api/v1/employer/profile").session(shop))
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.verificationNote").value("The licence is not readable"));

        mvc.perform(multipart("/api/v1/employer/profile/document").file(licence()).session(shop).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.verificationNote").doesNotExist());

        // A second decision on the same submission is refused
        mvc.perform(post("/api/v1/admin/employers/" + employerId + "/verification").session(admin).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"decision\":\"APPROVE\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/admin/employers/" + employerId + "/verification").session(admin).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"decision\":\"APPROVE\"}"))
                .andExpect(status().isConflict());
    }

    // ------------------------------------------------------------------ Feature 4: rules

    @Test
    void aPostWithoutAWageOrWithTheRangeUpsideDownIsRefused() throws Exception {
        MockHttpSession karim = verifiedEmployer();
        mvc.perform(post("/api/v1/employer/jobs?submit=true").session(karim).with(csrf()).header("Accept-Language", "bn")
                        .contentType(MediaType.APPLICATION_JSON).content(job("Driver", "null", "null")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.wageMin").exists())
                .andExpect(jsonPath("$.errors.wageMax").exists());
        mvc.perform(post("/api/v1/employer/jobs?submit=true").session(karim).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(job("Driver", "20000", "16000")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.wageMax").exists());
        // Nothing was saved
        mvc.perform(get("/api/v1/employer/jobs").session(karim))
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void theSameIdempotencyKeyCreatesOnlyOnePost() throws Exception {
        MockHttpSession karim = verifiedEmployer();
        String key = UUID.randomUUID().toString();
        String first = mvc.perform(post("/api/v1/employer/jobs").session(karim).with(csrf()).header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON).content(job("Loader", "600", "800")))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String second = mvc.perform(post("/api/v1/employer/jobs").session(karim).with(csrf()).header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON).content(job("Loader", "600", "800")))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        assertThat((String) JsonPath.read(second, "$.id")).isEqualTo(JsonPath.read(first, "$.id"));
    }

    @Test
    void aPublishedPostClosesItselfAfter30Days() throws Exception {
        MockHttpSession karim = verifiedEmployer();
        String created = mvc.perform(post("/api/v1/employer/jobs?submit=true").session(karim).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(job("Cook", "15000", "18000")))
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andReturn().getResponse().getContentAsString();
        String jobId = JsonPath.read(created, "$.id");

        jobService.closeExpired(Instant.now().plus(Duration.ofDays(29)));
        mvc.perform(get("/api/v1/employer/jobs/" + jobId).session(karim)).andExpect(jsonPath("$.status").value("PUBLISHED"));

        jobService.closeExpired(Instant.now().plus(Duration.ofDays(31)));
        mvc.perform(get("/api/v1/employer/jobs/" + jobId).session(karim))
                .andExpect(jsonPath("$.status").value("CLOSED"))
                .andExpect(jsonPath("$.statusReason").value("EXPIRED"));
    }

    @Test
    void draftsCanBeDeletedButPublishedPostsCannot() throws Exception {
        MockHttpSession karim = verifiedEmployer();
        String draft = mvc.perform(post("/api/v1/employer/jobs").session(karim).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"titleBn\":\"লোডার\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.titleEn").value("লোডার"))
                .andReturn().getResponse().getContentAsString();
        mvc.perform(delete("/api/v1/employer/jobs/" + JsonPath.read(draft, "$.id")).session(karim).with(csrf()))
                .andExpect(status().isNoContent());

        String live = mvc.perform(post("/api/v1/employer/jobs?submit=true").session(karim).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(job("Guard", "12000", "15000")))
                .andReturn().getResponse().getContentAsString();
        mvc.perform(delete("/api/v1/employer/jobs/" + JsonPath.read(live, "$.id")).session(karim).with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    void taxonomySearchFindsBanglaBanglishAndMisspelledNames() throws Exception {
        for (String q : new String[] {"draiver", "ড্রাইভার", "chalok"}) {
            mvc.perform(get("/api/v1/taxonomy/occupations").param("q", q))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].nameEn").value("Driver (car, pickup, microbus)"));
        }
    }

    @Test
    void requestsWithoutTheCsrfTokenAreRefused() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@test.kaaj\",\"password\":\"Admin@12345\"}"))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------ helpers

    private MockHttpSession verifiedEmployer() throws Exception {
        MockHttpSession employer = signUp("emp-" + UUID.randomUUID() + "@test.kaaj", "EMPLOYER");
        MockHttpSession admin = signIn("admin@test.kaaj", "Admin@12345");
        mvc.perform(put("/api/v1/employer/profile").session(employer).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(profile("Test Business"))).andExpect(status().isOk());
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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value(role));
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

    private org.springframework.test.web.servlet.ResultActions action(MockHttpSession session, String jobId, String action)
            throws Exception {
        return mvc.perform(patch("/api/v1/employer/jobs/" + jobId).session(session).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"action\":\"" + action + "\"}"));
    }

    private String occupationId() throws Exception {
        String found = mvc.perform(get("/api/v1/taxonomy/occupations").param("q", "driver"))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(found, "$[0].id");
    }

    private String job(String title, String wageMin, String wageMax) throws Exception {
        return """
                {"titleBn": "%s", "titleEn": "%s", "descriptionBn": "Test post for %s",
                 "occupationId": "%s", "jobType": "FULL_TIME", "shift": "DAY", "hours": "08:00-18:00",
                 "openings": 2, "wageMin": %s, "wageMax": %s, "wagePeriod": "MONTH",
                 "latitude": 23.8916, "longitude": 90.4023, "areaName": "Tongi, Gazipur",
                 "minExperienceYears": 1, "skillIds": [], "requiredDocuments": ["DRIVING_PRO_LIGHT"]}
                """.formatted(title, title, title, occupationId(), wageMin, wageMax);
    }

    private static String profile(String name) {
        return """
                {"name": "%s", "kind": "SME", "address": "Station Road, Tongi, Gazipur",
                 "latitude": 23.8916, "longitude": 90.4023, "contactPerson": "Md. Karim",
                 "contactPhone": "01712-345678", "contactEmail": "info@example.com"}
                """.formatted(name);
    }

    private static MockMultipartFile licence() throws IOException {
        BufferedImage image = new BufferedImage(60, 40, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return new MockMultipartFile("file", "licence.png", "image/png", out.toByteArray());
    }
}
