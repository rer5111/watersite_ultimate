package org.example.userservice.service;

import org.example.userservice.dto.AdminUserResponse;
import org.example.userservice.dto.AdminUserUpdateRequest;
import org.example.userservice.dto.RoleRequestResponse;
import org.example.userservice.model.AccountModel;
import org.example.userservice.model.RoleEnum;
import org.example.userservice.model.RoleRequest;
import org.example.userservice.model.UserModel;
import org.example.userservice.model.UserStatus;
import org.example.userservice.repository.RoleRequestRepository;
import org.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRequestRepository roleRequestRepository;

    private AdminUserService adminUserService;

    @BeforeEach
    void setUp() {
        adminUserService = new AdminUserService(userRepository, roleRequestRepository);
    }

    @Test
    void updateUser_changesRoleAndStatus() {
        UserModel user = user(1, "player");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        AdminUserResponse response = adminUserService.updateUser(
                1L,
                new AdminUserUpdateRequest(RoleEnum.MODERATOR, UserStatus.BANNED)
        );

        assertThat(response.role()).isEqualTo("MODERATOR");
        assertThat(response.status()).isEqualTo("BANNED");
        assertThat(user.getRoles()).containsExactly(RoleEnum.MODERATOR);
        assertThat(user.getStatus()).isEqualTo(UserStatus.BANNED);
    }

    @Test
    void approveRoleRequest_grantsRequestedRoleAndMarksReviewed() {
        UserModel user = user(2, "applicant");
        RoleRequest request = new RoleRequest(user, RoleEnum.MODERATOR, "Хочу модерировать", LocalDateTime.now());
        request.setId(7);
        when(roleRequestRepository.findById(7L)).thenReturn(Optional.of(request));
        when(roleRequestRepository.save(request)).thenReturn(request);

        RoleRequestResponse response = adminUserService.approveRoleRequest(7L);

        assertThat(response.status()).isEqualTo("APPROVED");
        assertThat(response.reviewedAt()).isNotNull();
        assertThat(user.getRoles()).containsExactly(RoleEnum.MODERATOR);
        verify(userRepository).save(user);
    }

    private UserModel user(long id, String name) {
        UserModel user = new UserModel(id, name, "hash", Set.of(RoleEnum.USER),
                new AccountModel(id, name, LocalDate.of(2025, 1, 1)));
        user.setEmail(name + "@example.com");
        return user;
    }
}
