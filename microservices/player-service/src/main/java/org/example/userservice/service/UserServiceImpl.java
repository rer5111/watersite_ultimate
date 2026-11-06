package org.example.userservice.service;

import org.example.userservice.dto.CheckAvailabilityResponse;
import org.example.userservice.dto.UserProfileResponse;
import org.example.userservice.model.AccountModel;
import org.example.userservice.model.RoleEnum;
import org.example.userservice.model.UserModel;
import org.example.userservice.repository.AccountRepository;
import org.example.userservice.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserRepository userRepository,
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<UserModel> findById(long id) {
        return userRepository.findById(id).stream().toList();
    }

    @Override
    public List<UserModel> findByName(String name) {
        return userRepository.findAllByName(name);
    }

    @Override
    public List<UserModel> findByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return List.of();
        }
        return userRepository.findByIdentifier(identifier.trim());
    }

    @Override
    public List<UserModel> findAll() {
        return userRepository.findAll();
    }

    @Override
    public List<UserModel> findAdmins() {
        return userRepository.findByRolesContainingOrderByAccount_IdAsc(RoleEnum.ADMIN);
    }

    @Override
    public void addUser(@NonNull UserModel user) {
        userRepository.save(Objects.requireNonNull(user));
    }

    @Override
    public void updateUser(@NonNull UserModel user) {
        userRepository.save(Objects.requireNonNull(user));
    }

    @Override
    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<UserModel> findPage(int page) {
        if (page < 1) {
            return List.of();
        }
        Pageable pageable = PageRequest.of(page - 1, 5);
        return userRepository.findAll(pageable).getContent();
    }

    @Override
    public int getPages() {
        Pageable pageable = PageRequest.of(0, 5);
        return userRepository.findAll(pageable).getTotalPages();
    }

    @Override
    @Transactional
    public UserModel loginOrRegisterWithMinecraft(@NonNull UUID uuid, @NonNull String username) {
        Optional<AccountModel> accountOpt = accountRepository.findByUuid(uuid);
        if (accountOpt.isPresent()) {
            AccountModel account = accountOpt.get();
            if (!username.equals(account.getUsername())) {
                account.setUsername(username);
                accountRepository.save(account);
            }
            List<UserModel> users = userRepository.findByAccount(account);
            if (!users.isEmpty()) {
                return syncSiteLogin(users.get(0), username);
            }
            UserModel user = new UserModel(0L, username, passwordEncoder.encode(UUID.randomUUID().toString()), Set.of(RoleEnum.USER), account);
            return userRepository.save(user);
        }

        List<AccountModel> existingAccounts = accountRepository.findByUsername(username);
        for (AccountModel acc : existingAccounts) {
            if (acc.getUuid() == null) {
                List<UserModel> users = userRepository.findByAccount(acc);
                boolean hasAdmin = users.stream()
                        .anyMatch(u -> u.getRoles() != null && u.getRoles().contains(RoleEnum.ADMIN));
                if (hasAdmin) {
                    throw new IllegalStateException("Административный аккаунт не может быть автоматически привязан через вход");
                }
                acc.setUuid(uuid);
                accountRepository.save(acc);
                if (!users.isEmpty()) {
                    return syncSiteLogin(users.get(0), username);
                }
                UserModel user = new UserModel(0L, username, passwordEncoder.encode(UUID.randomUUID().toString()), Set.of(RoleEnum.USER), acc);
                return userRepository.save(user);
            }
        }

        AccountModel newAccount = new AccountModel(0L, username, LocalDate.now());
        newAccount.setUuid(uuid);
        AccountModel savedAccount = accountRepository.save(newAccount);

        UserModel newUser = new UserModel(
                0L,
                username,
                passwordEncoder.encode(UUID.randomUUID().toString()),
                Set.of(RoleEnum.USER),
                savedAccount
        );
        return userRepository.save(newUser);
    }

    @Override
    @Transactional
    public void linkMinecraft(long userId, @NonNull UUID uuid, @NonNull String username) {
        Optional<AccountModel> existingWithUuid = accountRepository.findByUuid(uuid);
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        AccountModel userAccount = user.getAccount();
        if (userAccount == null) {
            throw new IllegalStateException("User has no associated account: " + userId);
        }

        if (existingWithUuid.isPresent() && existingWithUuid.get().getId() != userAccount.getId()) {
            throw new IllegalStateException("Minecraft account is already linked to another user");
        }

        userAccount.setUuid(uuid);
        userAccount.setUsername(username);
        accountRepository.save(userAccount);
        syncSiteLogin(user, username);
    }

    @Override
    public CheckAvailabilityResponse checkAvailability(String email, String username) {
        if (email != null && !email.isBlank()) {
            List<UserModel> withEmail = userRepository.findByIdentifier(email.trim());
            if (!withEmail.isEmpty()) {
                return CheckAvailabilityResponse.taken("Пользователь с таким email уже зарегистрирован", "email");
            }
        }
        if (username != null && !username.isBlank()) {
            List<UserModel> withName = userRepository.findByIdentifier(username.trim());
            if (!withName.isEmpty()) {
                return CheckAvailabilityResponse.taken("Пользователь с таким никнеймом уже зарегистрирован", "nickname");
            }
        }
        return CheckAvailabilityResponse.success();
    }

    @Override
    @Transactional
    public UserModel registerUser(String email, String username, String password) {
        CheckAvailabilityResponse availability = checkAvailability(email, username);
        if (!availability.available()) {
            throw new IllegalStateException(availability.message());
        }

        List<AccountModel> existingAccounts = accountRepository.findByUsername(username.trim());
        AccountModel account = existingAccounts.isEmpty()
                ? accountRepository.save(new AccountModel(0L, username.trim(), LocalDate.now()))
                : existingAccounts.get(0);

        UserModel newUser = new UserModel(
                0L,
                username.trim(),
                passwordEncoder.encode(password),
                Set.of(RoleEnum.USER),
                account
        );
        newUser.setEmail(email.trim().toLowerCase());
        return userRepository.save(newUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(long userId, String fallbackUsername) {
        UserModel user = resolveUser(userId, fallbackUsername);
        return mapToProfile(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateMinecraftNickname(long userId, String fallbackUsername, String newNickname) {
        UserModel user = resolveUser(userId, fallbackUsername);
        String nickname = newNickname.trim();
        for (UserModel other : userRepository.findByIdentifier(nickname)) {
            if (other.getId() != user.getId()) {
                throw new IllegalStateException("Пользователь с таким никнеймом уже зарегистрирован");
            }
        }
        AccountModel account = user.getAccount();
        if (account == null) {
            account = accountRepository.save(new AccountModel(0L, nickname, LocalDate.now()));
            user.setAccount(account);
        } else {
            if (!nickname.equalsIgnoreCase(account.getUsername())) {
                account.setUuid(null);
            }
            account.setUsername(nickname);
            accountRepository.save(account);
        }
        user.setName(nickname);
        userRepository.save(user);
        return mapToProfile(user);
    }

    @Override
    @Transactional
    public void changeEmail(long userId, String newEmail) {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        String normalized = newEmail.trim().toLowerCase();
        List<UserModel> existing = userRepository.findByIdentifier(normalized);
        for (UserModel u : existing) {
            if (u.getId() != userId) {
                throw new IllegalStateException("Email уже используется другим аккаунтом");
            }
        }
        user.setEmail(normalized);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(long userId, String newPassword) {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUserAndAccount(long userId, String fallbackUsername) {
        UserModel user = resolveUser(userId, fallbackUsername);
        AccountModel account = user.getAccount();
        userRepository.delete(user);
        if (account != null) {
            accountRepository.delete(account);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String getUserEmail(long userId) {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        return user.getEmail();
    }

    private UserModel resolveUser(long userId, String fallbackUsername) {
        if (userId > 0) {
            Optional<UserModel> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                return userOpt.get();
            }
        }
        if (fallbackUsername != null && !fallbackUsername.isBlank()) {
            List<UserModel> users = userRepository.findAllByName(fallbackUsername.trim());
            if (!users.isEmpty()) {
                return users.get(0);
            }
        }
        throw new IllegalArgumentException("Пользователь не найден");
    }

    private UserModel syncSiteLogin(UserModel user, String username) {
        if (!username.equals(user.getName())) {
            user.setName(username);
            return userRepository.save(user);
        }
        return user;
    }

    private UserProfileResponse mapToProfile(UserModel user) {
        Set<String> roles = user.getRoles() == null
                ? Set.of()
                : user.getRoles().stream().map(RoleEnum::name).collect(java.util.stream.Collectors.toUnmodifiableSet());
        String mcNickname = user.getAccount() != null ? user.getAccount().getUsername() : user.getName();
        UUID mcUuid = user.getAccount() != null ? user.getAccount().getUuid() : null;
        LocalDate joinDate = user.getAccount() != null ? user.getAccount().getJoinDate() : null;
        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                mcNickname,
                mcUuid,
                joinDate,
                roles
        );
    }
}
