package org.example.userservice.controller;

import org.example.userservice.model.AccountModel;
import org.example.userservice.model.RoleEnum;
import org.example.userservice.model.UserModel;
import org.example.userservice.service.AccountService;
import org.example.userservice.service.SeasonService;
import org.example.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AccountService accountService;

    @MockBean
    private SeasonService seasonService;

    @Test
    void getAdmins_PermitAll_ReturnsAdminProfilesWithoutPassword() throws Exception {
        AccountModel account = new AccountModel(1, "LeadCone_TNF", LocalDate.now());
        account.setUuid(UUID.fromString("ccb40ba9-e0af-4939-9c80-3ce15fb417ed"));

        UserModel admin = new UserModel(1, "LeadCone_TNF", "secret_hash", Set.of(RoleEnum.ADMIN), account);
        when(userService.findAdmins()).thenReturn(List.of(admin));

        mockMvc.perform(get("/api/user/admins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username", is("LeadCone_TNF")))
                .andExpect(jsonPath("$[0].uuid", is("ccb40ba9-e0af-4939-9c80-3ce15fb417ed")))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void registerUser_WithJsonBody_WhenAdmin_ReturnsOk() throws Exception {
        AccountModel account = new AccountModel(1, "test_user", LocalDate.now());
        when(accountService.findById(1)).thenReturn(List.of(account));

        String json = """
                {
                    "name": "test_user",
                    "password": "secret_password",
                    "roles": ["USER"],
                    "accountId": 1
                }
                """;

        mockMvc.perform(put("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(userService).addUser(any(UserModel.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void registerUser_WhenNotAdmin_ReturnsForbidden() throws Exception {
        String json = """
                {
                    "name": "test_user",
                    "password": "secret_password",
                    "roles": ["USER"],
                    "accountId": 1
                }
                """;

        mockMvc.perform(put("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_WhenFoundAndAdmin_CallsUserServiceDeleteAndReturnsNoContent() throws Exception {
        AccountModel account = new AccountModel(10, "to_delete", LocalDate.now());
        UserModel user = new UserModel(10, "to_delete", "hash", Set.of(RoleEnum.USER), account);
        when(userService.findById(10)).thenReturn(List.of(user));

        mockMvc.perform(delete("/api/user/10"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(10);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteUser_WhenNotAdmin_ReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/user/10"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test_player")
    void getCurrentUserProfile_WhenAuthenticated_ReturnsProfile() throws Exception {
        org.example.userservice.dto.UserProfileResponse profile = new org.example.userservice.dto.UserProfileResponse(
                5L,
                "test_player",
                "player@example.com",
                "Steve_Craft",
                UUID.fromString("ccb40ba9-e0af-4939-9c80-3ce15fb417ed"),
                LocalDate.of(2025, 1, 1),
                Set.of("USER")
        );
        when(userService.getProfile(0L, "test_player")).thenReturn(profile);

        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.username", is("test_player")))
                .andExpect(jsonPath("$.email", is("player@example.com")))
                .andExpect(jsonPath("$.minecraftNickname", is("Steve_Craft")));
    }

    @Test
    @WithMockUser(username = "test_player")
    void updateMinecraftNickname_WhenValid_ReturnsUpdatedProfile() throws Exception {
        org.example.userservice.dto.UserProfileResponse updated = new org.example.userservice.dto.UserProfileResponse(
                5L,
                "test_player",
                "player@example.com",
                "New_Steve",
                null,
                LocalDate.of(2025, 1, 1),
                Set.of("USER")
        );
        when(userService.updateMinecraftNickname(0L, "test_player", "New_Steve")).thenReturn(updated);

        String json = """
                {
                    "minecraftNickname": "New_Steve"
                }
                """;

        mockMvc.perform(put("/api/user/me/minecraft-nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minecraftNickname", is("New_Steve")));
    }

    @Test
    @WithMockUser(username = "test_player")
    void updateMinecraftNickname_WhenNicknameIsTaken_ReturnsConflict() throws Exception {
        when(userService.updateMinecraftNickname(0L, "test_player", "TakenName"))
                .thenThrow(new IllegalStateException("Пользователь с таким никнеймом уже зарегистрирован"));

        String json = """
                {
                    "minecraftNickname": "TakenName"
                }
                """;

        mockMvc.perform(put("/api/user/me/minecraft-nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Пользователь с таким никнеймом уже зарегистрирован")));
    }

    @Test
    @WithMockUser(username = "test_player")
    void deleteCurrentUserAccount_WhenAuthenticated_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/user/me"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUserAndAccount(0L, "test_player");
    }
}
