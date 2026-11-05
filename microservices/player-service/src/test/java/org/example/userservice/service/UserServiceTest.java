package org.example.userservice.service;

import org.example.userservice.model.AccountModel;
import org.example.userservice.model.RoleEnum;
import org.example.userservice.model.UserModel;
import org.example.userservice.repository.AccountRepository;
import org.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, accountRepository, passwordEncoder);
    }

    @Test
    void findByName_returnsMatchingUsers() {
        AccountModel account = new AccountModel(1L, "Steve", LocalDate.now());
        UserModel user = new UserModel(1L, "Steve", "encodedPass", Set.of(RoleEnum.USER), account);
        when(userRepository.findAllByName("Steve")).thenReturn(List.of(user));

        List<UserModel> result = userService.findByName("Steve");

        assertThat(result).containsExactly(user);
        verify(userRepository).findAllByName("Steve");
    }

    @Test
    void findByIdentifier_returnsMatchingUsers() {
        AccountModel account = new AccountModel(1L, "Steve", LocalDate.now());
        UserModel user = new UserModel(1L, "Steve", "encodedPass", Set.of(RoleEnum.USER), account);
        user.setEmail("steve@example.com");
        when(userRepository.findByIdentifier("steve@example.com")).thenReturn(List.of(user));

        List<UserModel> result = userService.findByIdentifier("steve@example.com");

        assertThat(result).containsExactly(user);
        verify(userRepository).findByIdentifier("steve@example.com");
    }

    @Test
    void addUser_savesUser() {
        AccountModel account = new AccountModel(1L, "Steve", LocalDate.now());
        UserModel user = new UserModel(0L, "Steve", "encodedPass", Set.of(RoleEnum.USER), account);

        userService.addUser(user);

        verify(userRepository).save(user);
    }

    @Test
    void deleteUser_deletesById() {
        userService.deleteUser(10L);

        verify(userRepository).deleteById(10L);
    }

    @Test
    void findPage_returnsEmptyWhenPageInvalid() {
        List<UserModel> result = userService.findPage(0);

        assertThat(result).isEmpty();
    }

    @Test
    void findPage_returnsPageContent() {
        AccountModel account = new AccountModel(1L, "Steve", LocalDate.now());
        UserModel user = new UserModel(1L, "Steve", "encodedPass", Set.of(RoleEnum.USER), account);
        Page<UserModel> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(PageRequest.class))).thenReturn(page);

        List<UserModel> result = userService.findPage(1);

        assertThat(result).containsExactly(user);
    }

    @Test
    void loginOrRegisterWithMinecraft_createsNewUserAndAccountWhenNotFound() {
        UUID uuid = UUID.randomUUID();
        when(accountRepository.findByUuid(uuid)).thenReturn(Optional.empty());
        when(accountRepository.findByUsername("Steve")).thenReturn(List.of());
        when(passwordEncoder.encode(any())).thenReturn("hashed-random");

        AccountModel savedAccount = new AccountModel(10L, "Steve", LocalDate.now());
        savedAccount.setUuid(uuid);
        when(accountRepository.save(any(AccountModel.class))).thenReturn(savedAccount);

        UserModel savedUser = new UserModel(20L, "Steve", "hashed-random", Set.of(RoleEnum.USER), savedAccount);
        when(userRepository.save(any(UserModel.class))).thenReturn(savedUser);

        UserModel result = userService.loginOrRegisterWithMinecraft(uuid, "Steve");

        assertThat(result.getId()).isEqualTo(20L);
        assertThat(result.getName()).isEqualTo("Steve");
        assertThat(result.getAccount().getUuid()).isEqualTo(uuid);
    }

    @Test
    void loginOrRegisterWithMinecraft_returnsExistingWhenFound() {
        UUID uuid = UUID.randomUUID();
        AccountModel account = new AccountModel(10L, "Steve", LocalDate.now());
        account.setUuid(uuid);
        UserModel user = new UserModel(20L, "Steve", "hashed", Set.of(RoleEnum.USER), account);

        when(accountRepository.findByUuid(uuid)).thenReturn(Optional.of(account));
        when(userRepository.findByAccount(account)).thenReturn(List.of(user));

        UserModel result = userService.loginOrRegisterWithMinecraft(uuid, "Steve");

        assertThat(result).isEqualTo(user);
    }

    @Test
    void linkMinecraft_updatesUserAccount() {
        UUID uuid = UUID.randomUUID();
        AccountModel account = new AccountModel(10L, "OldName", LocalDate.now());
        UserModel user = new UserModel(20L, "OldName", "pass", Set.of(RoleEnum.USER), account);

        when(accountRepository.findByUuid(uuid)).thenReturn(Optional.empty());
        when(userRepository.findById(20L)).thenReturn(Optional.of(user));

        userService.linkMinecraft(20L, uuid, "NewMinecraftName");

        assertThat(account.getUuid()).isEqualTo(uuid);
        assertThat(account.getUsername()).isEqualTo("NewMinecraftName");
        verify(accountRepository).save(account);
    }
}
