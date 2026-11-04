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
    @WithMockUser
    void registerUser_WithJsonBody_ReturnsOk() throws Exception {
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
    @WithMockUser
    void deleteUser_WhenFound_CallsUserServiceDeleteAndReturnsNoContent() throws Exception {
        AccountModel account = new AccountModel(10, "to_delete", LocalDate.now());
        UserModel user = new UserModel(10, "to_delete", "hash", Set.of(RoleEnum.USER), account);
        when(userService.findById(10)).thenReturn(List.of(user));

        mockMvc.perform(delete("/api/user/10"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(10);
    }
}
