package org.example.userservice.service;

import org.example.userservice.dto.CheckAvailabilityResponse;
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
                return users.get(0);
            }
            UserModel user = new UserModel(0L, username, passwordEncoder.encode(UUID.randomUUID().toString()), Set.of(RoleEnum.USER), account);
            return userRepository.save(user);
        }

        List<AccountModel> existingAccounts = accountRepository.findByUsername(username);
        for (AccountModel acc : existingAccounts) {
            if (acc.getUuid() == null) {
                acc.setUuid(uuid);
                accountRepository.save(acc);
                List<UserModel> users = userRepository.findByAccount(acc);
                if (!users.isEmpty()) {
                    return users.get(0);
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
}
