package org.example.userservice.controller;

import org.example.userservice.exception.BadRequestException;
import org.example.userservice.model.AccountModel;
import org.example.userservice.model.RoleEnum;
import org.example.userservice.model.SeasonModel;
import org.example.userservice.model.UserModel;
import org.example.userservice.dto.AdminProfileResponse;
import org.example.userservice.dto.AdminUserResponse;
import org.example.userservice.dto.AdminUserUpdateRequest;
import org.example.userservice.dto.CreateRoleRequestRequest;
import org.example.userservice.dto.CreateUserRequest;
import org.example.userservice.dto.RoleRejectRequest;
import org.example.userservice.dto.RoleRequestResponse;
import org.example.userservice.dto.UpdateMinecraftNicknameRequest;
import org.example.userservice.dto.UpdateUserRequest;
import org.example.userservice.dto.UserProfileResponse;
import org.example.userservice.service.AccountService;
import org.example.userservice.service.AdminUserService;
import org.example.userservice.service.SeasonService;
import org.example.userservice.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.util.List;
import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;
    private final SeasonService seasonService;
    private final AdminUserService adminUserService;

    public UserController(
            UserService userService,
            AccountService accountService,
            PasswordEncoder passwordEncoder,
            SeasonService seasonService,
            AdminUserService adminUserService
    ) {
        this.userService = userService;
        this.accountService = accountService;
        this.passwordEncoder = passwordEncoder;
        this.seasonService = seasonService;
        this.adminUserService = adminUserService;
    }

    @PutMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> registerUserJson(@RequestBody @Valid CreateUserRequest request) {
        return registerUser(request.name(), request.password(), request.roles(), request.accountId());
    }

    @PutMapping
    public ResponseEntity<Void> registerUser(
            @RequestParam String name,
            @RequestParam String password,
            @RequestParam Set<RoleEnum> roles,
            @RequestParam long accountId
    ) {
        try {
            List<AccountModel> accounts = accountService.findById(accountId);
            if (accounts.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            UserModel userModel = new UserModel(
                    0,
                    name,
                    passwordEncoder.encode(password),
                    roles,
                    accounts.get(0)
            );
            userService.addUser(userModel);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to register user", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateUserJson(@RequestBody @Valid UpdateUserRequest request) {
        return updateUser(request.id(), request.name(), request.password(), request.roles(), request.accountId());
    }

    @PostMapping
    public ResponseEntity<Void> updateUser(
            @RequestParam long id,
            @RequestParam String name,
            @RequestParam String password,
            @RequestParam Set<RoleEnum> roles,
            @RequestParam long accountId
    ) {
        List<UserModel> users = userService.findById(id);
        List<AccountModel> accounts = accountService.findById(accountId);
        if (users.isEmpty() || accounts.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            UserModel userModel = users.get(0);
            userModel.setName(name);
            userModel.setPassword(passwordEncoder.encode(password));
            userModel.setRoles(roles);
            userModel.setAccount(accounts.get(0));
            userService.updateUser(userModel);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to update user", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable long key,
            @AuthenticationPrincipal Jwt jwt
    ) {
        long currentUserId = extractUserId(jwt);
        if (currentUserId > 0 && key == currentUserId) {
            throw new BadRequestException("Нельзя удалить собственный аккаунт");
        }
        userService.deleteUser(key);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(path = "/pages", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> getPages() {
        return ResponseEntity.ok(userService.getPages());
    }

    @GetMapping(path = "/admins", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AdminProfileResponse>> getAdmins() {
        List<AdminProfileResponse> admins = userService.findAdmins().stream()
                .map(user -> new AdminProfileResponse(
                        user.getAccount() != null ? user.getAccount().getUsername() : user.getName(),
                        user.getAccount() != null ? user.getAccount().getUuid() : null
                ))
                .toList();
        return ResponseEntity.ok(admins);
    }

    @GetMapping(path = "/all", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserModel>> getAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping(path = "/admin/users", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AdminUserResponse>> getAdminUsers() {
        return ResponseEntity.ok(adminUserService.listUsers());
    }

    @PutMapping(path = "/admin/users/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<AdminUserResponse> updateAdminUser(
            @PathVariable long id,
            @Valid @RequestBody AdminUserUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        long currentUserId = extractUserId(jwt);
        return ResponseEntity.ok(adminUserService.updateUser(id, request, currentUserId));
    }

    @GetMapping(path = "/admin/role-requests", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RoleRequestResponse>> getRoleRequests() {
        return ResponseEntity.ok(adminUserService.listRoleRequests());
    }

    @PostMapping(path = "/admin/role-requests/{id}/approve", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<RoleRequestResponse> approveRoleRequest(@PathVariable long id) {
        return ResponseEntity.ok(adminUserService.approveRoleRequest(id));
    }

    @PostMapping(path = "/admin/role-requests/{id}/reject", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<RoleRequestResponse> rejectRoleRequest(
            @PathVariable long id,
            @Valid @RequestBody RoleRejectRequest request
    ) {
        return ResponseEntity.ok(adminUserService.rejectRoleRequest(id, request.reason()));
    }

    @GetMapping(path = "/findPage/{page}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserModel>> getPage(@PathVariable int page) {
        return ResponseEntity.ok(userService.findPage(page));
    }

    @GetMapping(value = "/findID/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserModel>> findById(@PathVariable long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @GetMapping(value = "/findName/{name}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserModel>> findByName(@PathVariable String name) {
        return ResponseEntity.ok(userService.findByName(name));
    }

    @DeleteMapping(value = {"/season/{id}/{s_id}", "/deleteSeason/{id}/{s_id}"})
    public ResponseEntity<Void> deleteFromSeason(@PathVariable long id, @PathVariable long s_id) {
        log.info("Deleting user {} from season {}", id, s_id);
        try {
            List<UserModel> users = userService.findById(id);
            List<SeasonModel> seasons = seasonService.findById(s_id);
            if (users.isEmpty() || seasons.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            UserModel userModel = users.get(0);
            userModel.removeSeason(seasons.get(0));
            userService.updateUser(userModel);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to delete user from season", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(value = {"/season/{id}/{s_id}", "/addSeason/{id}/{s_id}"})
    public ResponseEntity<Void> addToSeason(@PathVariable long id, @PathVariable long s_id) {
        log.info("Adding user {} to season {}", id, s_id);
        try {
            List<UserModel> users = userService.findById(id);
            List<SeasonModel> seasons = seasonService.findById(s_id);
            if (users.isEmpty() || seasons.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            UserModel userModel = users.get(0);
            userModel.addSeason(seasons.get(0));
            userService.updateUser(userModel);
            log.info("User {} added to season {}", id, s_id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to add user to season", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(path = {"/role-requests", "/me/role-requests"}, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<RoleRequestResponse> submitRoleRequest(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateRoleRequestRequest request
    ) {
        long userId = extractUserId(jwt);
        if (userId <= 0) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(adminUserService.submitRoleRequest(userId, request));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping(path = "/me", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile(
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ) {
        long userId = extractUserId(jwt);
        String username = extractUsername(jwt, authentication);
        if (userId <= 0 && (username == null || username.isBlank())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            UserProfileResponse profile = userService.getProfile(userId, username);
            return ResponseEntity.ok(profile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping(path = "/me/minecraft-nickname", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateMinecraftNickname(
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication,
            @Valid @RequestBody UpdateMinecraftNicknameRequest request
    ) {
        long userId = extractUserId(jwt);
        String username = extractUsername(jwt, authentication);
        if (userId <= 0 && (username == null || username.isBlank())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            UserProfileResponse profile = userService.updateMinecraftNickname(userId, username, request.minecraftNickname());
            return ResponseEntity.ok(profile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUserAccount(
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication
    ) {
        long userId = extractUserId(jwt);
        String username = extractUsername(jwt, authentication);
        if (userId <= 0 && (username == null || username.isBlank())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            userService.deleteUserAndAccount(userId, username);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    private long extractUserId(Jwt jwt) {
        if (jwt != null && jwt.getClaim("user_id") != null) {
            Object idVal = jwt.getClaim("user_id");
            if (idVal instanceof Number) {
                return ((Number) idVal).longValue();
            }
        }
        return 0L;
    }

    private String extractUsername(Jwt jwt, Authentication authentication) {
        if (jwt != null && jwt.getSubject() != null && !jwt.getSubject().isBlank()) {
            return jwt.getSubject();
        }
        if (authentication != null && authentication.getName() != null && !authentication.getName().isBlank()) {
            return authentication.getName();
        }
        return null;
    }
}
