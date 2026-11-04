package com.waltercraft.donation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@Testcontainers
@SpringBootTest(
        properties = {
                "spring.cloud.config.enabled=false",
                "eureka.client.enabled=false"
        }
)
@AutoConfigureMockMvc
class DonationServiceIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("donation_test")
            .withUsername("donation_test")
            .withPassword("donation_test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void publicCatalogShowsSeededTypesInDatabaseOrderWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/donation-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].code").value("support-project"))
                .andExpect(jsonPath("$.content[1].code").value("help-server"))
                .andExpect(jsonPath("$.content[2].code").value("major-support"))
                .andExpect(jsonPath("$.page.totalElements").value(greaterThanOrEqualTo(3)));

        mockMvc.perform(get("/api/v1/donation-types?sort=unknownField"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void actuatorHealthIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void adminOperationsRequireAuthenticationAndAdminRole() throws Exception {
        mockMvc.perform(get("/api/v1/admin/donations"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/admin/donations").with(user("player").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void donationTypeCreateValidatesInputAndCreatesRecord() throws Exception {
        mockMvc.perform(post("/api/v1/admin/donation-types")
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"bad code","name":"","priceRub":-1,"displayOrder":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details").isMap());

        String code = uniqueCode();
        mockMvc.perform(post("/api/v1/admin/donation-types")
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"%s","name":"Тестовый донат","description":"Проверка","priceRub":75.50,"displayOrder":40}
                                """.formatted(code)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.priceRub").value(75.5))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.version").value(0));

        mockMvc.perform(get("/api/v1/admin/donation-types").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].code").value("support-project"));
    }

    @Test
    void donationTypeUpdateUsesVersionAndDeleteReturnsNoContent() throws Exception {
        String code = uniqueCode();
        mockMvc.perform(post("/api/v1/admin/donation-types")
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"%s","name":"Версия","priceRub":123,"displayOrder":40}
                                """.formatted(code)))
                .andExpect(status().isCreated());
        long id = jdbcTemplate.queryForObject("SELECT id FROM donation_types WHERE code = ?", Long.class, code);

        mockMvc.perform(get("/api/v1/admin/donation-types/{id}", id).with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mockMvc.perform(put("/api/v1/admin/donation-types/{id}", id)
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"%s","name":"Версия обновлена","priceRub":125,"displayOrder":45,"active":true,"version":0}
                                """.formatted(code)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(1));

        mockMvc.perform(put("/api/v1/admin/donation-types/{id}", id)
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"%s","name":"Устаревшая запись","priceRub":125,"displayOrder":45,"active":true,"version":0}
                                """.formatted(code)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));

        mockMvc.perform(delete("/api/v1/admin/donation-types/{id}", id).with(admin()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/admin/donation-types/{id}", id).with(admin()))
                .andExpect(status().isNotFound());
    }

    @Test
    void donationCreationSnapshotsPriceProtectsReferencedTypeAndSupportsDelete() throws Exception {
        String reference = "payment-" + UUID.randomUUID();
        String invalidDonation = """
                {"playerId":42,"donationTypeId":999999,"paymentReference":"%s"}
                """.formatted(reference);
        mockMvc.perform(post("/api/v1/admin/donations")
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON).content(invalidDonation))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.donationTypeId").exists());

        String code = uniqueCode();
        mockMvc.perform(post("/api/v1/admin/donation-types")
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"%s","name":"Для записи","priceRub":50,"displayOrder":40}
                                """.formatted(code)))
                .andExpect(status().isCreated());
        long typeId = jdbcTemplate.queryForObject("SELECT id FROM donation_types WHERE code = ?", Long.class, code);

        String donationRequest = """
                {"playerId":42,"donationTypeId":%d,"paymentReference":"%s"}
                """.formatted(typeId, reference);
        mockMvc.perform(post("/api/v1/admin/donations")
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON).content(donationRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.playerId").value(42))
                .andExpect(jsonPath("$.donationTypeName").value("Для записи"))
                .andExpect(jsonPath("$.amountRub").value(50));
        long donationId = jdbcTemplate.queryForObject(
                "SELECT id FROM donations WHERE payment_reference = ?", Long.class, reference);

        mockMvc.perform(post("/api/v1/admin/donations")
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON).content(donationRequest))
                .andExpect(status().isConflict());
        mockMvc.perform(delete("/api/v1/admin/donation-types/{id}", typeId).with(admin()))
                .andExpect(status().isConflict());
        mockMvc.perform(get("/api/v1/admin/donations").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(donationId));
        mockMvc.perform(get("/api/v1/admin/donations/{id}", donationId).with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentReference").value(reference));
        mockMvc.perform(delete("/api/v1/admin/donations/{id}", donationId).with(admin()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/admin/donations/{id}", donationId).with(admin()))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/v1/admin/donation-types/{id}", typeId).with(admin()))
                .andExpect(status().isNoContent());
    }

    private static RequestPostProcessor admin() {
        return user("admin").roles("ADMIN");
    }

    private static String uniqueCode() {
        return "test-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
