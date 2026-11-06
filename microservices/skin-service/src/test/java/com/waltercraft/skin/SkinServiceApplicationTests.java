package com.waltercraft.skin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SkinServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void actuatorHealthIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void actuatorMetricsRequireAdministratorRole() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/actuator/metrics").with(
                        org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("viewer")
                                .roles("USER")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/actuator/metrics").with(
                        org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin")
                                .roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.names").isArray());
    }
}
