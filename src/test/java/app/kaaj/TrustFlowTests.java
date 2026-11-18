package app.kaaj;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
 * Scam screening and moderation (Member-3, Module 2) end to end, through the HTTP API and a real
 * PostgreSQL 18 (Docker Desktop must be running; without Docker these tests are skipped). No AI key is set,
 * so screening uses the rules only, which is also how the demo works without the internet.
 * The job-post assistant (Module 3) has its own flow test, {@code JobDraftFlowTests}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@TestPropertySource(properties = {
        "DB_PASSWORD=unused",
        "GEMINI_API_KEY=",
        "GROQ_API_KEY=",
        "kaaj.admin.email=admin@test.kaaj",
        "kaaj.admin.password=Admin@12345",
        "kaaj.files.dir=target/test-uploads"
})
class TrustFlowTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired
    MockMvc mvc;

    // ------------------------------------------------------------------ screening

    @Test
    void aPostAskingForMoneyIsBlockedAndAnHonestOneIsPublished() throws Exception {
        MockHttpSession employer = verifiedEmployer();
        MockHttpSession admin = signIn("admin@test.kaaj", "Admin@12345");

        String scam = create(employer, job("Malaysia factory job", "Processing fee Tk 50,000 by bKash. Visa in 30 days."));
        mvc.perform(get("/api/v1/employer/jobs/" + scam).session(employer))
                .andExpect(jsonPath("$.status").value("BLOCKED"))
                .andExpect(jsonPath("$.statusReason").value("SCREENING_BLOCKED"));
        mvc.perform(get("/api/v1/employer/jobs/" + scam + "/screening").session(employer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseStatus").value("OPEN"))
                .andExpect(jsonPath("$.findings[*].code", hasItem("money-request")));
        mvc.perform(get("/api/v1/jobs/" + scam)).andExpect(status().isNotFound());

        String honest = create(employer, job("Pickup driver", "Drive a pickup around Tongi. One day off a week."));
        mvc.perform(get("/api/v1/employer/jobs/" + honest).session(employer))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        // The block is in the admin queue; an admin can dismiss it, and the post goes live
        String caseId = caseFor(admin, scam);
        mvc.perform(get("/api/v1/admin/cases/" + caseId).session(employer)).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/admin/cases/" + caseId).session(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.screening").value("BLOCK"))
                .andExpect(jsonPath("$.job.status").value("BLOCKED"))
                .andExpect(jsonPath("$.employer.verified").value(true));
        decide(admin, caseId, "DISMISS", "Checked with the employer by phone")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISMISSED"))
                .andExpect(jsonPath("$.job.status").value("PUBLISHED"));
        decide(admin, caseId, "DISMISS", null).andExpect(status().isConflict());
    }

    @Test
    void contactDetailsSendAPostToReviewAndAnAdminCanRemoveIt() throws Exception {
        MockHttpSession employer = verifiedEmployer();
        MockHttpSession admin = signIn("admin@test.kaaj", "Admin@12345");

        String post = create(employer, job("Security guard", "Night watch. Call 01712-345678 for details."));
        mvc.perform(get("/api/v1/employer/jobs/" + post).session(employer))
                .andExpect(jsonPath("$.status").value("IN_REVIEW"))
                .andExpect(jsonPath("$.statusReason").value("SCREENING_REVIEW"));

        String caseId = caseFor(admin, post);
        decide(admin, caseId, "REMOVE_POST", null).andExpect(status().isBadRequest());   // a note is required
        decide(admin, caseId, "REMOVE_POST", "Phone numbers are not allowed in posts")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REMOVED"))
                .andExpect(jsonPath("$.job.status").value("BLOCKED"));
        mvc.perform(get("/api/v1/employer/jobs/" + post + "/screening").session(employer))
                .andExpect(jsonPath("$.decision").value("REMOVE_POST"))
                .andExpect(jsonPath("$.note").value("Phone numbers are not allowed in posts"));
    }

    // ------------------------------------------------------------------ reports

    @Test
    void threeReportsHideAPostAndSuspendingTheEmployerEndsTheirSession() throws Exception {
        String employerEmail = "emp-" + UUID.randomUUID() + "@test.kaaj";
        MockHttpSession employer = verifiedEmployer(employerEmail);
        MockHttpSession admin = signIn("admin@test.kaaj", "Admin@12345");
        String post = create(employer, job("Warehouse helper", "Loading and packing at our warehouse."));

        report(employer, post).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("report.own-post"));
        MockHttpSession first = signUp("s1-" + UUID.randomUUID() + "@test.kaaj", "SEEKER");
        report(first, post).andExpect(status().isCreated());
        report(first, post).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("report.duplicate"));
        report(signUp("s2-" + UUID.randomUUID() + "@test.kaaj", "SEEKER"), post).andExpect(status().isCreated());
        mvc.perform(get("/api/v1/jobs/" + post)).andExpect(status().isOk());              // two reports: still live
        report(signUp("s3-" + UUID.randomUUID() + "@test.kaaj", "SEEKER"), post).andExpect(status().isCreated());
        mvc.perform(get("/api/v1/jobs/" + post)).andExpect(status().isNotFound());        // three: out of search
        mvc.perform(get("/api/v1/employer/jobs/" + post).session(employer))
                .andExpect(jsonPath("$.status").value("IN_REVIEW"))
                .andExpect(jsonPath("$.statusReason").value("REPORTED"));

        String caseId = caseFor(admin, post);
        mvc.perform(get("/api/v1/admin/cases/" + caseId).session(admin))
                .andExpect(jsonPath("$.reportCount").value(3))
                .andExpect(jsonPath("$.reports.length()").value(3));
        decide(admin, caseId, "SUSPEND_EMPLOYER", "Asked applicants for money at the interview")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employer.ownerActive").value(false));

        // The employer's open session stops working, and they can't sign in again
        mvc.perform(get("/api/v1/employer/jobs").session(employer))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("auth.suspended"));
        mvc.perform(post("/api/v1/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"Secret@123\"}".formatted(employerEmail)))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------ helpers

    private String create(MockHttpSession employer, String body) throws Exception {
        String created = mvc.perform(post("/api/v1/employer/jobs?submit=true").session(employer).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(created, "$.id");
    }

    private String caseFor(MockHttpSession admin, String jobId) throws Exception {
        String queue = mvc.perform(get("/api/v1/admin/cases?status=OPEN").session(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].subjectId", hasItem(jobId)))
                .andReturn().getResponse().getContentAsString();
        List<String> ids = JsonPath.read(queue, "$.items[?(@.subjectId == '" + jobId + "')].id");
        return ids.getFirst();
    }

    private org.springframework.test.web.servlet.ResultActions decide(MockHttpSession admin, String caseId, String decision,
                                                                      String note) throws Exception {
        String body = note == null ? "{\"decision\":\"%s\"}".formatted(decision)
                : "{\"decision\":\"%s\",\"note\":\"%s\"}".formatted(decision, note);
        return mvc.perform(post("/api/v1/admin/cases/" + caseId + "/decision").session(admin).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private org.springframework.test.web.servlet.ResultActions report(MockHttpSession session, String jobId) throws Exception {
        return mvc.perform(post("/api/v1/reports").session(session).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"subjectType\":\"JOB\",\"subjectId\":\"%s\",\"reason\":\"FAKE_JOB\"}".formatted(jobId)));
    }

    private MockHttpSession verifiedEmployer() throws Exception {
        return verifiedEmployer("emp-" + UUID.randomUUID() + "@test.kaaj");
    }

    private MockHttpSession verifiedEmployer(String email) throws Exception {
        MockHttpSession employer = signUp(email, "EMPLOYER");
        MockHttpSession admin = signIn("admin@test.kaaj", "Admin@12345");
        mvc.perform(put("/api/v1/employer/profile").session(employer).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"name": "Test Business", "kind": "SME", "address": "Station Road, Tongi, Gazipur",
                                 "latitude": 23.8916, "longitude": 90.4023, "contactPerson": "Md. Karim",
                                 "contactPhone": "01712-345678", "contactEmail": "info@example.com"}
                                """))
                .andExpect(status().isOk());
        String profile = mvc.perform(multipart("/api/v1/employer/profile/document").file(licence()).session(employer)
                        .param("documentType", "TRADE_LICENCE").with(csrf()))
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

    private String job(String title, String description) throws Exception {
        String found = mvc.perform(get("/api/v1/taxonomy/occupations").param("q", "driver"))
                .andReturn().getResponse().getContentAsString();
        String occupationId = JsonPath.read(found, "$[0].id");
        return """
                {"titleBn": "%s", "titleEn": "%s", "descriptionBn": "%s", "descriptionEn": "%s",
                 "occupationId": "%s", "jobType": "FULL_TIME", "shift": "DAY", "hours": "08:00-16:00",
                 "openings": 2, "wageMin": 16000, "wageMax": 20000, "wagePeriod": "MONTH",
                 "latitude": 23.8916, "longitude": 90.4023, "areaName": "Tongi, Gazipur",
                 "minExperienceYears": 1, "skillIds": [], "requiredDocuments": []}
                """.formatted(title, title, description, description, occupationId);
    }

    private static MockMultipartFile licence() throws IOException {
        BufferedImage image = new BufferedImage(60, 40, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return new MockMultipartFile("file", "licence.png", "image/png", out.toByteArray());
    }
}
