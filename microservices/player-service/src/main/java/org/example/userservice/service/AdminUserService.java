package org.example.userservice.service;

import org.example.userservice.dto.AdminUserResponse;
import org.example.userservice.dto.AdminUserUpdateRequest;
import org.example.userservice.dto.CreateRoleRequestRequest;
import org.example.userservice.dto.RoleRequestResponse;
import org.example.userservice.exception.BadRequestException;
import org.example.userservice.exception.ConflictException;
import org.example.userservice.exception.ResourceNotFoundException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AdminUserService {

    private static final Logger log = LoggerFactory.getLogger(AdminUserService.class);

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
        return updateUser(id, request, 0L);
    }

    @Transactional
    public AdminUserResponse updateUser(long id, AdminUserUpdateRequest request, long currentUserId) {
        UserModel user = findUser(id);
        boolean isTargetAdmin = user.getRoles() != null && user.getRoles().contains(RoleEnum.ADMIN);
        boolean isSelf = currentUserId > 0 && user.getId() == currentUserId;

        if (isTargetAdmin && request.status() == UserStatus.BANNED) {
            throw new BadRequestException("Нельзя заблокировать учетную запись администратора");
        }
        if (isSelf && request.status() == UserStatus.BANNED) {
            throw new BadRequestException("Нельзя заблокировать собственный аккаунт");
        }
        if (isSelf && isTargetAdmin && request.role() != RoleEnum.ADMIN) {
            throw new BadRequestException("Нельзя понизить роль собственной учетной записи");
        }

        user.setRoles(new HashSet<>(Set.of(request.role())));
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
    public RoleRequestResponse submitRoleRequest(long userId, CreateRoleRequestRequest request) {
        if (request.requestedRole() == RoleEnum.USER) {
            throw new BadRequestException("Заявку можно подать только на роль MODERATOR или ADMIN");
        }
        UserModel user = findUser(userId);
        if (roleRequestRepository.existsByUser_IdAndStatus(userId, RoleRequestStatus.PENDING)) {
            throw new ConflictException("У вас уже есть заявка на рассмотрении");
        }
        RoleRequest roleRequest = new RoleRequest(
                user,
                request.requestedRole(),
                request.note().trim(),
                LocalDateTime.now()
        );
        RoleRequest saved = roleRequestRepository.save(roleRequest);
        log.info("Role request submitted roleRequestId={} applicantId={} requestedRole={}",
                saved.getId(), userId, saved.getRequestedRole());
        return toRoleRequestResponse(saved);
    }

    @Transactional
    public RoleRequestResponse approveRoleRequest(long id) {
        RoleRequest request = findPendingRequest(id);
        UserModel user = request.getUser();
        user.setRoles(EnumSet.of(request.getRequestedRole()));

        request.setStatus(RoleRequestStatus.APPROVED);
        request.setReviewedAt(LocalDateTime.now());
        request.setRejectionReason(null);
        log.info("Role request approved roleRequestId={} applicantId={} grantedRole={}",
                request.getId(), user.getId(), request.getRequestedRole());
        return toRoleRequestResponse(request);
    }

    @Transactional
    public RoleRequestResponse rejectRoleRequest(long id, String reason) {
        RoleRequest request = findPendingRequest(id);
        if (reason == null || reason.isBlank()) {
            throw new BadRequestException("Причина отклонения обязательна");
        }

        request.setStatus(RoleRequestStatus.REJECTED);
        request.setReviewedAt(LocalDateTime.now());
        request.setRejectionReason(reason.trim());
        log.info("Role request rejected roleRequestId={} applicantId={}", request.getId(), request.getUser().getId());
        return toRoleRequestResponse(request);
    }

    private UserModel findUser(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
    }

    private RoleRequest findPendingRequest(long id) {
        RoleRequest request = roleRequestRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Заявка не найдена"));
        if (request.getStatus() != RoleRequestStatus.PENDING) {
            throw new ConflictException("Заявка уже рассмотрена");
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
