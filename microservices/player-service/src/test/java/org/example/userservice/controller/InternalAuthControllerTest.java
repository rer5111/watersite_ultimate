package org.example.userservice.controller;

import org.example.userservice.config.SecurityConfig;
import org.example.userservice.model.RoleEnum;
import org.example.userservice.model.UserModel;
import org.example.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalAuthController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "security.auth-service.shared-secret=test-secret-with-at-least-32-characters")
class InternalAuthControllerTest {

    private static final String SHARED_SECRET = "test-secret-with-at-least-32-characters";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    void verifiesValidCredentialsAndReturnsOnlyIdentityClaims() throws Exception {
        UserModel user = new UserModel();
        user.setId(23L);
        user.setName("player");
        user.setPassword("$2a$encoded");
        user.setRoles(Set.of(RoleEnum.USER));
        when(userService.findByName("player")).thenReturn(List.of(user));
        when(passwordEncoder.matches("correct", "$2a$encoded")).thenReturn(true);

        mockMvc.perform(post("/internal/auth/verify")
                        .header("X-Internal-Auth", SHARED_SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"player","password":"correct"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.userId").value(23))
                .andExpect(jsonPath("$.username").value("player"))
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void rejectsWrongPasswordWithoutReturningUserData() throws Exception {
        UserModel user = new UserModel();
        user.setId(23L);
        user.setName("player");
        user.setPassword("$2a$encoded");
        user.setRoles(Set.of(RoleEnum.USER));
        when(userService.findByName("player")).thenReturn(List.of(user));
        when(passwordEncoder.matches(eq("wrong"), eq("$2a$encoded"))).thenReturn(false);

        mockMvc.perform(post("/internal/auth/verify")
                        .header("X-Internal-Auth", SHARED_SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"player","password":"wrong"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(false))
                .andExpect(jsonPath("$.userId").doesNotExist())
                .andExpect(jsonPath("$.username").doesNotExist())
                .andExpect(jsonPath("$.roles").isEmpty());
    }

    @Test
    void rejectsRequestsWithoutInternalSecret() throws Exception {
        mockMvc.perform(post("/internal/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"player","password":"correct"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
