package org.example.userservice.service;

import org.example.userservice.model.AccountModel;
import org.example.userservice.repository.AccountRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountServiceImpl(accountRepository);
    }

    @Test
    void findByUuid_returnsAccountWhenFound() {
        UUID uuid = UUID.randomUUID();
        AccountModel account = new AccountModel(1L, "Steve", LocalDate.now());
        account.setUuid(uuid);
        when(accountRepository.findByUuid(uuid)).thenReturn(Optional.of(account));

        Optional<AccountModel> result = accountService.findByUuid(uuid);

        assertThat(result).contains(account);
        verify(accountRepository).findByUuid(uuid);
    }

    @Test
    void findByName_returnsMatchingAccounts() {
        AccountModel account = new AccountModel(1L, "Steve", LocalDate.now());
        when(accountRepository.findByUsername("Steve")).thenReturn(List.of(account));

        List<AccountModel> result = accountService.findByName("Steve");

        assertThat(result).containsExactly(account);
        verify(accountRepository).findByUsername("Steve");
    }

    @Test
    void addAccount_savesAccount() {
        AccountModel account = new AccountModel(0L, "Alex", LocalDate.now());

        accountService.addAccount(account);

        verify(accountRepository).save(account);
    }

    @Test
    void deleteAccount_deletesById() {
        accountService.deleteAccount(5L);

        verify(accountRepository).deleteById(5L);
    }

    @Test
    void findPage_returnsEmptyListWhenPageIsZeroOrNegative() {
        List<AccountModel> result = accountService.findPage(0);

        assertThat(result).isEmpty();
    }

    @Test
    void findPage_returnsPagedContent() {
        AccountModel account = new AccountModel(1L, "Steve", LocalDate.now());
        Page<AccountModel> page = new PageImpl<>(List.of(account));
        when(accountRepository.findAll(any(PageRequest.class))).thenReturn(page);

        List<AccountModel> result = accountService.findPage(1);

        assertThat(result).containsExactly(account);
    }
}
