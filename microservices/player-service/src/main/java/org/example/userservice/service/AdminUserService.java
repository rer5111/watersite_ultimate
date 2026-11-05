package org.example.userservice.service;

import org.example.userservice.dto.AdminUserResponse;
import org.example.userservice.dto.AdminUserUpdateRequest;
import org.example.userservice.dto.RoleRequestResponse;
import org.example.userservice.model.AccountModel;
import org.example.userservice.model.RoleEnum;
import org.example.userservice.model.RoleRequest;
import org.example.userservice.model.RoleRequestStatus;
import org.example.userservice.model.UserModel;
import org.example.userservice.model.UserStatus;
import org.example.userservice.repository.RoleRequestRepository;
import org.example.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final RoleRequestRepository roleRequestRepository;

    public AdminUserService(UserRepository userRepository, RoleRequestRepository roleRequestRepository) {
        this.userRepository = userRepository;
        this.roleRequestRepository = roleRequestRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> listUsers() {
        return userRepository.findAllForAdmin().stream()
                .map(this::toUserResponse)
                .toList();
    }

    @Transactional
    public AdminUserResponse updateUser(long id, AdminUserUpdateRequest request) {
        UserModel user = findUser(id);
        user.setRoles(Set.of(request.role()));
        user.setStatus(request.status());
        return toUserResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<RoleRequestResponse> listRoleRequests() {
        return roleRequestRepository.findAllByOrderBySubmittedAtDesc().stream()
                .map(this::toRoleRequestResponse)
                .toList();
    }

    @Transactional
    public RoleRequestResponse approveRoleRequest(long id) {
        RoleRequest request = findPendingRequest(id);
        UserModel user = request.getUser();
        user.setRoles(Set.of(request.getRequestedRole()));
        userRepository.save(user);

        request.setStatus(RoleRequestStatus.APPROVED);
        request.setReviewedAt(LocalDateTime.now());
        request.setRejectionReason(null);
        return toRoleRequestResponse(roleRequestRepository.save(request));
    }

    @Transactional
    public RoleRequestResponse rejectRoleRequest(long id, String reason) {
        RoleRequest request = findPendingRequest(id);
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Причина отклонения обязательна");
        }

        request.setStatus(RoleRequestStatus.REJECTED);
        request.setReviewedAt(LocalDateTime.now());
        request.setRejectionReason(reason.trim());
        return toRoleRequestResponse(roleRequestRepository.save(request));
    }

    private UserModel findUser(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }

    private RoleRequest findPendingRequest(long id) {
        RoleRequest request = roleRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена"));
        if (request.getStatus() != RoleRequestStatus.PENDING) {
            throw new IllegalStateException("Заявка уже рассмотрена");
        }
        return request;
    }

    private AdminUserResponse toUserResponse(UserModel user) {
        AccountModel account = user.getAccount();
        LocalDate registeredAt = account != null && account.getJoinDate() != null
                ? account.getJoinDate()
                : LocalDate.now();
        return new AdminUserResponse(
                user.getId(),
                user.getName(),
                account != null ? account.getUsername() : user.getName(),
                user.getEmail() == null ? "" : user.getEmail(),
                primaryRole(user).name(),
                (user.getStatus() == null ? UserStatus.ACTIVE : user.getStatus()).name(),
                registeredAt
        );
    }

    private RoleEnum primaryRole(UserModel user) {
        Set<RoleEnum> roles = user.getRoles() == null ? Set.of() : user.getRoles();
        if (roles.contains(RoleEnum.ADMIN)) {
            return RoleEnum.ADMIN;
        }
        if (roles.contains(RoleEnum.MODERATOR)) {
            return RoleEnum.MODERATOR;
        }
        return RoleEnum.USER;
    }

    private RoleRequestResponse toRoleRequestResponse(RoleRequest request) {
        UserModel user = request.getUser();
        return new RoleRequestResponse(
                request.getId(),
                user.getId(),
                user.getName(),
                user.getEmail() == null ? "" : user.getEmail(),
                request.getRequestedRole().name(),
                request.getSubmittedAt(),
                request.getNote(),
                request.getStatus().name(),
                request.getReviewedAt(),
                request.getRejectionReason()
        );
    }
}
