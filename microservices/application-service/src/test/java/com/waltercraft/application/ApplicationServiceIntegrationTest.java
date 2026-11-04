package com.waltercraft.application;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false"
})
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
class ApplicationServiceIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("application_test")
            .withUsername("application_test")
            .withPassword("application_test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void endpointsRequireAuthenticationAndHealthIsPublic() throws Exception {
        mockMvc.perform(get("/api/v1/applications"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        mockMvc.perform(post("/api/v1/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Новая заявка\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void applicantCanCreateAndOnlyListTheirOwnApplications() throws Exception {
        String username = username("alice");
        String text = "Заявка " + UUID.randomUUID();

        mockMvc.perform(post("/api/v1/applications")
                        .with(applicant(username))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text":"%s","applicantUsername":"another-user","status":"APPROVED"}
                                """.formatted(text)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.applicantUsername").value(username))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.version").value(0));

        createApplication(username("bob"), "Чужая заявка " + UUID.randomUUID());

        mockMvc.perform(get("/api/v1/applications").with(applicant(username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].applicantUsername").value(username));

        mockMvc.perform(get("/api/v1/applications").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));
    }

    @Test
    void applicantCannotReadAnotherUsersApplicationButAdminCan() throws Exception {
        String owner = username("owner");
        long id = createApplication(owner, "Private " + UUID.randomUUID());

        mockMvc.perform(get("/api/v1/applications/{id}", id).with(applicant(username("stranger"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
        mockMvc.perform(get("/api/v1/applications/{id}", id).with(applicant(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
        mockMvc.perform(get("/api/v1/applications/{id}", id).with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicantUsername").value(owner));
    }

    @Test
    void validatesApplicationTextAndRejectsUnknownSort() throws Exception {
        mockMvc.perform(post("/api/v1/applications")
                        .with(applicant(username("validator")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details.text").exists());

        mockMvc.perform(get("/api/v1/applications?sort=unknownField")
                        .with(applicant(username("validator"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void onlyAdminCanUpdateAndOptimisticVersionPreventsStaleChanges() throws Exception {
        String owner = username("reviewer");
        long id = createApplication(owner, "Исходный текст " + UUID.randomUUID());
        String update = """
                {"text":"Обновлённая заявка","status":"APPROVED","version":0}
                """;

        mockMvc.perform(put("/api/v1/applications/{id}", id)
                        .with(applicant(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(update))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/v1/applications/{id}", id)
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(update))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.version").value(1));

        mockMvc.perform(put("/api/v1/applications/{id}", id)
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(update))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    void applicantCanWithdrawOnlyPendingApplications() throws Exception {
        String owner = username("withdrawal");
        long pendingId = createApplication(owner, "Отменить " + UUID.randomUUID());
        mockMvc.perform(delete("/api/v1/applications/{id}", pendingId).with(applicant(owner)))
                .andExpect(status().isNoContent());

        long reviewedId = createApplication(owner, "Уже проверена " + UUID.randomUUID());
        mockMvc.perform(put("/api/v1/applications/{id}", reviewedId)
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text":"Уже проверена","status":"REJECTED","version":0}
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/v1/applications/{id}", reviewedId).with(applicant(owner)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
        mockMvc.perform(delete("/api/v1/applications/{id}", reviewedId).with(admin()))
                .andExpect(status().isNoContent());
    }

    private long createApplication(String username, String text) throws Exception {
        mockMvc.perform(post("/api/v1/applications")
                        .with(applicant(username))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text":"%s"}
                                """.formatted(text)))
                .andExpect(status().isCreated());
        return jdbcTemplate.queryForObject(
                "SELECT id FROM applications WHERE application_text = ?", Long.class, text);
    }

    private static String username(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor admin() {
        return user("application-admin").roles("ADMIN");
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor applicant(String username) {
        return user(username).roles("USER");
    }
}
