package com.waltercraft.payment;

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

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false"
})
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
class PaymentServiceIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("payment_test")
            .withUsername("payment_test")
            .withPassword("payment_test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void endpointsRequireAdminAndHealthIsPublic() throws Exception {
        mockMvc.perform(get("/api/v1/admin/payments"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        mockMvc.perform(get("/api/v1/admin/payments").with(user("player").roles("USER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void administratorCanCreateListUpdateAndDeletePayment() throws Exception {
        String reference = "payment-" + UUID.randomUUID();
        String createBody = createRequest("Walter", reference, "1250.50");

        mockMvc.perform(post("/api/v1/admin/payments")
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.payerUsername").value("Walter"))
                .andExpect(jsonPath("$.paymentDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.amountRub").value(1250.5))
                .andExpect(jsonPath("$.version").value(0));

        long id = paymentId(reference);
        mockMvc.perform(get("/api/v1/admin/payments/{id}", id).with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalReference").value(reference));
        mockMvc.perform(get("/api/v1/admin/payments?sort=paymentDate,desc").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));

        String update = """
                {"payerUsername":"Walter","paymentDate":"%s","amountRub":1300.75,"externalReference":"%s","version":0}
                """.formatted(LocalDate.now(), reference);
        mockMvc.perform(put("/api/v1/admin/payments/{id}", id)
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(update))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amountRub").value(1300.75))
                .andExpect(jsonPath("$.version").value(1));

        mockMvc.perform(put("/api/v1/admin/payments/{id}", id)
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(update))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));

        mockMvc.perform(delete("/api/v1/admin/payments/{id}", id).with(admin()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/admin/payments/{id}", id).with(admin()))
                .andExpect(status().isNotFound());
    }

    @Test
    void validatesAmountsDatesAndRequiredFields() throws Exception {
        mockMvc.perform(post("/api/v1/admin/payments")
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"payerUsername":"","paymentDate":"%s","amountRub":0}
                                """.formatted(LocalDate.now().plusDays(1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details.payerUsername").exists())
                .andExpect(jsonPath("$.details.paymentDate").exists())
                .andExpect(jsonPath("$.details.amountRub").exists());
    }

    @Test
    void duplicateExternalReferenceReturnsConflict() throws Exception {
        String reference = "duplicate-" + UUID.randomUUID();
        String request = createRequest("Walter", reference, "100.00");
        mockMvc.perform(post("/api/v1/admin/payments")
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/admin/payments")
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    void invalidSortAndMissingPaymentReturnClientErrors() throws Exception {
        mockMvc.perform(get("/api/v1/admin/payments?sort=unknownField")
                        .with(admin()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        mockMvc.perform(get("/api/v1/admin/payments/999999999").with(admin()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    private long paymentId(String reference) {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM payments WHERE external_reference = ?", Long.class, reference);
    }

    private static String createRequest(String username, String reference, String amount) {
        return """
                {"payerUsername":"%s","paymentDate":"%s","amountRub":%s,"externalReference":"%s"}
                """.formatted(username, LocalDate.now(), amount, reference);
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor admin() {
        return user("payment-admin").roles("ADMIN");
    }
}
