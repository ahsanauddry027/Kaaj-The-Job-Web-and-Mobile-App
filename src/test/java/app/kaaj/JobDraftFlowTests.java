package app.kaaj;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * The AI job-post assistant (Member-3, Module 3) end to end, through the HTTP API and a real PostgreSQL 18
 * (Docker Desktop must be running; without Docker these tests are skipped). No AI key is set, so the
 * assistant uses its templates, which is also how the demo works without the internet.
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
class JobDraftFlowTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired
    MockMvc mvc;

    @Test
    void theAssistantDraftsKarimsSentenceWithoutAi() throws Exception {
        MockHttpSession employer = signUp("emp-" + UUID.randomUUID() + "@test.kaaj", "EMPLOYER");
        mvc.perform(post("/api/v1/employer/jobs/drafts").session(employer).with(csrf())
                        .header("Accept-Language", "en")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\": \"2 jon pickup driver lagbe, Tongi, beton 16-20 hajar, sokal 8ta theke sondha 6ta\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.source").value("TEMPLATE"))
                .andExpect(jsonPath("$.titleEn").value("Pickup driver"))
                .andExpect(jsonPath("$.titleBn").value("পিকআপ ড্রাইভার"))
                .andExpect(jsonPath("$.openings").value(2))
                .andExpect(jsonPath("$.wageMin").value(16000))
                .andExpect(jsonPath("$.wageMax").value(20000))
                .andExpect(jsonPath("$.wagePeriod").value("MONTH"))
                .andExpect(jsonPath("$.hours").value("08:00-18:00"))
                .andExpect(jsonPath("$.areaName").value("Tongi"))
                .andExpect(jsonPath("$.findings[*].code", hasItem("long-day")))
                .andExpect(jsonPath("$.blocked").value(false));

        mvc.perform(post("/api/v1/employer/jobs/drafts").session(employer).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\": \"Guard lagbe, registration fee 500 taka\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.blocked").value(true));

        MockHttpSession seeker = signUp("s-" + UUID.randomUUID() + "@test.kaaj", "SEEKER");
        mvc.perform(post("/api/v1/employer/jobs/drafts").session(seeker).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"text\": \"driver lagbe\"}"))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------ helpers

    private MockHttpSession signUp(String email, String role) throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(post("/api/v1/auth/register").session(session).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"Secret@123\",\"role\":\"%s\"}".formatted(email, role)))
                .andExpect(status().isOk());
        return session;
    }
}
