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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
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
        when(userService.findByIdentifier("player")).thenReturn(List.of(user));
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
        when(userService.findByIdentifier("player")).thenReturn(List.of(user));
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

    @Test
    void minecraftLogin_returnsUserData() throws Exception {
        UUID uuid = UUID.randomUUID();
        UserModel user = new UserModel();
        user.setId(42L);
        user.setName("Steve");
        user.setRoles(Set.of(RoleEnum.USER));

        when(userService.loginOrRegisterWithMinecraft(eq(uuid), eq("Steve"))).thenReturn(user);

        mockMvc.perform(post("/internal/auth/minecraft-login")
                        .header("X-Internal-Auth", SHARED_SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"uuid":"%s","username":"Steve"}
                                """, uuid)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.userId").value(42))
                .andExpect(jsonPath("$.username").value("Steve"))
                .andExpect(jsonPath("$.roles[0]").value("USER"));
    }

    @Test
    void linkMinecraft_success() throws Exception {
        UUID uuid = UUID.randomUUID();

        mockMvc.perform(post("/internal/auth/link-minecraft")
                        .header("X-Internal-Auth", SHARED_SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"userId":42,"uuid":"%s","username":"Steve"}
                                """, uuid)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.userId").value(42))
                .andExpect(jsonPath("$.uuid").value(uuid.toString()));

        verify(userService).linkMinecraft(eq(42L), eq(uuid), eq("Steve"));
    }

    @Test
    void linkMinecraft_conflictWhenAlreadyLinked() throws Exception {
        UUID uuid = UUID.randomUUID();
        doThrow(new IllegalStateException("Already linked")).when(userService)
                .linkMinecraft(eq(42L), eq(uuid), eq("Steve"));

        mockMvc.perform(post("/internal/auth/link-minecraft")
                        .header("X-Internal-Auth", SHARED_SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"userId":42,"uuid":"%s","username":"Steve"}
                                """, uuid)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }
}
