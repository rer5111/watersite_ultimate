package org.example.userservice.controller;

import org.example.userservice.model.AccountModel;
import org.example.userservice.model.RoleEnum;
import org.example.userservice.model.RoleRequest;
import org.example.userservice.model.RoleRequestStatus;
import org.example.userservice.model.UserModel;
import org.example.userservice.dto.SeasonParticipationResponse;
import org.example.userservice.repository.AccountRepository;
import org.example.userservice.repository.RoleRequestRepository;
import org.example.userservice.repository.UserRepository;
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
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRequestRepository roleRequestRepository;

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

    @Test
    @WithMockUser(roles = "ADMIN")
    void approveRoleRequest_updatesUserRoleAndRequestInPostgres() throws Exception {
        RoleRequest roleRequest = createRoleRequest(RoleEnum.MODERATOR);

        mockMvc.perform(post("/api/user/admin/role-requests/{id}/approve", roleRequest.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.requestedRole").value("MODERATOR"))
                .andExpect(jsonPath("$.reviewedAt").isNotEmpty());

        UserModel updatedUser = userRepository.findById(roleRequest.getUser().getId()).orElseThrow();
        RoleRequest updatedRequest = roleRequestRepository.findById(roleRequest.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(updatedUser.getRoles()).containsExactly(RoleEnum.MODERATOR);
        org.assertj.core.api.Assertions.assertThat(updatedRequest.getStatus()).isEqualTo(RoleRequestStatus.APPROVED);

        mockMvc.perform(post("/api/user/admin/role-requests/{id}/approve", roleRequest.getId()))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectRoleRequest_returnsProblemDetailsForInvalidAndMissingRequests() throws Exception {
        RoleRequest roleRequest = createRoleRequest(RoleEnum.ADMIN);

        mockMvc.perform(post("/api/user/admin/role-requests/{id}/reject", roleRequest.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));

        mockMvc.perform(post("/api/user/admin/role-requests/{id}/reject", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Недостаточно информации\"}"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));

        mockMvc.perform(post("/api/user/admin/role-requests/{id}/reject", roleRequest.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Нужно больше опыта\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.rejectionReason").value("Нужно больше опыта"));

        UserModel unchangedUser = userRepository.findById(roleRequest.getUser().getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(unchangedUser.getRoles()).containsExactly(RoleEnum.USER);
    }

    @Test
    @WithMockUser(roles = "USER")
    void roleRequestReviewIsForbiddenForNonAdministrators() throws Exception {
        mockMvc.perform(post("/api/user/admin/role-requests/{id}/approve", 123L))
                .andExpect(status().isForbidden());
    }

    @Test
    void currentPlayerCanReadSeasonParticipationHistory() throws Exception {
        var season = new SeasonParticipationResponse.SeasonEntry(
                5L, "Пятый сезон", LocalDate.of(2025, 1, 1), LocalDate.of(2026, 1, 1),
                "История сервера", "/images/seasons/season-5.webp",
                SeasonParticipationResponse.Phase.COMPLETED);
        when(seasonService.getParticipation(42L)).thenReturn(new SeasonParticipationResponse(42L, 1, List.of(season)));

        mockMvc.perform(get("/api/season/me")
                        .with(jwt().jwt(token -> token.claim("user_id", 42L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId").value(42))
                .andExpect(jsonPath("$.totalSeasons").value(1))
                .andExpect(jsonPath("$.seasons[0].title").value("Пятый сезон"))
                .andExpect(jsonPath("$.seasons[0].phase").value("COMPLETED"));

        verify(seasonService).getParticipation(42L);
    }

    @Test
    void seasonParticipationRequiresUserIdInAccessToken() throws Exception {
        mockMvc.perform(get("/api/season/me").with(jwt()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateAdminUser_whenBanningAdmin_returnsBadRequestWithMeaningfulMessage() throws Exception {
        String username = "admin-to-ban-" + UUID.randomUUID();
        AccountModel account = accountRepository.saveAndFlush(new AccountModel(0, username, LocalDate.now()));
        UserModel adminUser = new UserModel(0, username, "hash", new HashSet<>(Set.of(RoleEnum.ADMIN)), account);
        UserModel savedAdmin = userRepository.saveAndFlush(adminUser);

        mockMvc.perform(put("/api/user/admin/users/{id}", savedAdmin.getId())
                        .with(jwt().jwt(token -> token.claim("user_id", 999L).claim("roles", List.of("ADMIN"))))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\",\"status\":\"BANNED\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Нельзя заблокировать учетную запись администратора"));
    }

    private RoleRequest createRoleRequest(RoleEnum requestedRole) {
        String username = "role-test-" + UUID.randomUUID();
        AccountModel account = new AccountModel();
        account.setUsername(username);
        account.setJoinDate(LocalDate.now());
        AccountModel savedAccount = accountRepository.saveAndFlush(account);

        UserModel user = new UserModel();
        user.setName(username);
        user.setPassword("test-password-hash");
        user.setRoles(new HashSet<>(Set.of(RoleEnum.USER)));
        user.setAccount(savedAccount);
        UserModel savedUser = userRepository.saveAndFlush(user);

        RoleRequest roleRequest = new RoleRequest(savedUser, requestedRole, "Тестовая заявка", LocalDateTime.now());
        return roleRequestRepository.saveAndFlush(roleRequest);
    }
}
