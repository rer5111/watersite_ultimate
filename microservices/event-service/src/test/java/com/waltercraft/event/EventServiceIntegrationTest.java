package com.waltercraft.event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(
        properties = {
                "spring.cloud.config.enabled=false",
                "eureka.client.enabled=false"
        }
)
@AutoConfigureMockMvc
class EventServiceIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("event_test")
            .withUsername("event_test")
            .withPassword("event_test");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicCatalogShowsSeededEventsInDatabaseOrderWithCacheHeader() throws Exception {
        mockMvc.perform(get("/api/v1/events"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "max-age=600, public"))
                .andExpect(jsonPath("$.content[0].code").value("season-1"))
                .andExpect(jsonPath("$.content[0].title").value("1 сезон"))
                .andExpect(jsonPath("$.content[1].code").value("season-2"))
                .andExpect(jsonPath("$.content[2].code").value("season-3"))
                .andExpect(jsonPath("$.content[3].code").value("season-4"))
                .andExpect(jsonPath("$.content[4].code").value("season-5"))
                .andExpect(jsonPath("$.page.totalElements").value(greaterThanOrEqualTo(5)));
    }

    @Test
    void getEventById_ReturnsEvent() throws Exception {
        mockMvc.perform(get("/api/v1/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("season-1"))
                .andExpect(jsonPath("$.title").value("1 сезон"));
    }

    @Test
    void getEventById_Returns404WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/events/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void adminEndpointsRequireAdminRole() throws Exception {
        mockMvc.perform(post("/api/v1/admin/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"test\",\"title\":\"Test\",\"description\":\"desc\",\"startDate\":\"2026\",\"endDate\":\"2026\",\"image\":\"/img.png\",\"displayOrder\":100}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/admin/events")
                        .with(user("regularUser").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"test\",\"title\":\"Test\",\"description\":\"desc\",\"startDate\":\"2026\",\"endDate\":\"2026\",\"image\":\"/img.png\",\"displayOrder\":100}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateAndDeleteEvent() throws Exception {
        mockMvc.perform(post("/api/v1/admin/events")
                        .with(user("adminUser").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "new-event",
                                  "title": "Новое событие",
                                  "description": "Описание нового события сервера",
                                  "startDate": "2026",
                                  "endDate": "2027",
                                  "image": "/images/seasons/season-5.webp",
                                  "displayOrder": 90,
                                  "organizer": "LeadCone_TNF",
                                  "status": "DRAFT",
                                  "active": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("new-event"))
                .andExpect(jsonPath("$.title").value("Новое событие"))
                .andExpect(jsonPath("$.organizer").value("LeadCone_TNF"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.active").value(false));

        // Duplicate code triggers conflict 409
        mockMvc.perform(post("/api/v1/admin/events")
                        .with(user("adminUser").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "new-event",
                                  "title": "Дубликат",
                                  "description": "Попытка создать с тем же кодом",
                                  "startDate": "2026",
                                  "endDate": "2027",
                                  "image": "/images/seasons/season-5.webp",
                                  "displayOrder": 95,
                                  "active": true
                                }
                                """))
                .andExpect(status().isConflict());
    }
}
