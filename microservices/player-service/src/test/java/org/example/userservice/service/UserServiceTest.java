package org.example.userservice.service;

import org.example.userservice.model.AccountModel;
import org.example.userservice.model.RoleEnum;
import org.example.userservice.model.UserModel;
import org.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);
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
}
