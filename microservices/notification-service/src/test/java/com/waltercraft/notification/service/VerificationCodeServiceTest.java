package com.waltercraft.notification.service;

import com.waltercraft.notification.dto.SendCodeResponse;
import com.waltercraft.notification.dto.VerifyCodeResponse;
import com.waltercraft.notification.entity.EmailVerificationCode;
import com.waltercraft.notification.exception.RateLimitExceededException;
import com.waltercraft.notification.repository.EmailVerificationCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationCodeServiceTest {

    @Mock
    private EmailVerificationCodeRepository codeRepository;

    @Mock
    private EmailSenderService emailSenderService;

    private VerificationCodeService verificationCodeService;

    @BeforeEach
    void setUp() {
        verificationCodeService = new VerificationCodeService(codeRepository, emailSenderService, "");
    }

    @Test
    void generateAndSendCode_createsCodeAndDispatchesEmail() {
        when(codeRepository.findTopByEmailIgnoreCaseAndPurposeOrderByIdDesc("player@example.com", "REGISTRATION"))
                .thenReturn(Optional.empty());

        SendCodeResponse response = verificationCodeService.generateAndSendCode("player@example.com", "REGISTRATION");

        assertThat(response.status()).isEqualTo("SENT");
        assertThat(response.cooldownSeconds()).isEqualTo(60);

        ArgumentCaptor<EmailVerificationCode> entityCaptor = ArgumentCaptor.forClass(EmailVerificationCode.class);
        verify(codeRepository).save(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getEmail()).isEqualTo("player@example.com");

        verify(emailSenderService).sendVerificationCode(eq("player@example.com"), any(), eq("REGISTRATION"));
    }

    @Test
    void generateAndSendCode_throwsRateLimit_whenCalledWithinCooldown() {
        EmailVerificationCode recentCode = new EmailVerificationCode(
                "player@example.com",
                "hash",
                "REGISTRATION",
                Instant.now().minusSeconds(20),
                Instant.now().plus(Duration.ofMinutes(10))
        );
        when(codeRepository.findTopByEmailIgnoreCaseAndPurposeOrderByIdDesc("player@example.com", "REGISTRATION"))
                .thenReturn(Optional.of(recentCode));

        assertThatThrownBy(() -> verificationCodeService.generateAndSendCode("player@example.com", "REGISTRATION"))
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    void verifyCode_returnsSuccess_whenCodeMatches() {
        Instant now = Instant.now();
        String expectedHash = verificationCodeService.hash("player@example.com", "REGISTRATION", "123456");
        EmailVerificationCode record = new EmailVerificationCode(
                "player@example.com",
                expectedHash,
                "REGISTRATION",
                now.minusSeconds(30),
                now.plus(Duration.ofMinutes(10))
        );
        when(codeRepository.findTopByEmailIgnoreCaseAndPurposeOrderByIdDesc("player@example.com", "REGISTRATION"))
                .thenReturn(Optional.of(record));

        VerifyCodeResponse response = verificationCodeService.verifyCode("player@example.com", "123456", "REGISTRATION");

        assertThat(response.verified()).isTrue();
        assertThat(record.isVerified()).isTrue();
        verify(codeRepository).save(record);
    }

    @Test
    void verifyCode_returnsFailure_whenCodeDoesNotMatch() {
        Instant now = Instant.now();
        String expectedHash = verificationCodeService.hash("player@example.com", "REGISTRATION", "123456");
        EmailVerificationCode record = new EmailVerificationCode(
                "player@example.com",
                expectedHash,
                "REGISTRATION",
                now.minusSeconds(30),
                now.plus(Duration.ofMinutes(10))
        );
        when(codeRepository.findTopByEmailIgnoreCaseAndPurposeOrderByIdDesc("player@example.com", "REGISTRATION"))
                .thenReturn(Optional.of(record));

        VerifyCodeResponse response = verificationCodeService.verifyCode("player@example.com", "654321", "REGISTRATION");

        assertThat(response.verified()).isFalse();
        assertThat(response.message()).contains("Неверный код");
        assertThat(record.getAttempts()).isEqualTo(1);
    }
}
