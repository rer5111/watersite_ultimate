package com.waltercraft.notification.service;

import com.waltercraft.notification.dto.SendCodeResponse;
import com.waltercraft.notification.dto.VerifyCodeResponse;
import com.waltercraft.notification.entity.EmailVerificationCode;
import com.waltercraft.notification.exception.RateLimitExceededException;
import com.waltercraft.notification.repository.EmailVerificationCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class VerificationCodeService {

    private static final int COOLDOWN_SECONDS = 60;
    private static final int EXPIRATION_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;

    private final EmailVerificationCodeRepository codeRepository;
    private final EmailSenderService emailSenderService;
    private final SecureRandom secureRandom = new SecureRandom();

    public VerificationCodeService(
            EmailVerificationCodeRepository codeRepository,
            EmailSenderService emailSenderService
    ) {
        this.codeRepository = codeRepository;
        this.emailSenderService = emailSenderService;
    }

    @Transactional
    public SendCodeResponse generateAndSendCode(String email, String purpose) {
        String normalizedEmail = email.trim().toLowerCase();
        Instant now = Instant.now();

        Optional<EmailVerificationCode> lastCode = codeRepository
                .findTopByEmailIgnoreCaseAndPurposeOrderByIdDesc(normalizedEmail, purpose);

        if (lastCode.isPresent()) {
            long secondsSinceLast = Duration.between(lastCode.get().getCreatedAt(), now).toSeconds();
            if (secondsSinceLast < COOLDOWN_SECONDS) {
                int retryAfter = (int) (COOLDOWN_SECONDS - secondsSinceLast);
                throw new RateLimitExceededException(retryAfter);
            }
        }

        String rawCode = String.format("%06d", secureRandom.nextInt(1_000_000));
        String codeHash = hash(rawCode);

        Instant expiresAt = now.plus(Duration.ofMinutes(EXPIRATION_MINUTES));
        EmailVerificationCode newCode = new EmailVerificationCode(
                normalizedEmail,
                codeHash,
                purpose,
                now,
                expiresAt
        );
        codeRepository.save(newCode);

        emailSenderService.sendVerificationCode(normalizedEmail, rawCode, purpose);

        return new SendCodeResponse("SENT", COOLDOWN_SECONDS, (int) Duration.ofMinutes(EXPIRATION_MINUTES).toSeconds());
    }

    @Transactional
    public VerifyCodeResponse verifyCode(String email, String code, String purpose) {
        String normalizedEmail = email.trim().toLowerCase();
        Instant now = Instant.now();

        Optional<EmailVerificationCode> optionalCode = codeRepository
                .findTopByEmailIgnoreCaseAndPurposeOrderByIdDesc(normalizedEmail, purpose);

        if (optionalCode.isEmpty()) {
            return VerifyCodeResponse.failure("Код подтверждения не найден или истек");
        }

        EmailVerificationCode record = optionalCode.get();

        if (record.isVerified()) {
            return VerifyCodeResponse.failure("Данный код уже был использован");
        }

        if (record.getExpiresAt().isBefore(now)) {
            return VerifyCodeResponse.failure("Срок действия кода истек");
        }

        if (record.getAttempts() >= MAX_ATTEMPTS) {
            return VerifyCodeResponse.failure("Превышено количество попыток ввода. Запросите новый код");
        }

        String providedHash = hash(code.trim());
        if (!MessageDigest.isEqual(record.getCodeHash().getBytes(StandardCharsets.UTF_8), providedHash.getBytes(StandardCharsets.UTF_8))) {
            record.setAttempts(record.getAttempts() + 1);
            codeRepository.save(record);
            int remainingAttempts = MAX_ATTEMPTS - record.getAttempts();
            return VerifyCodeResponse.failure("Неверный код. Осталось попыток: " + remainingAttempts);
        }

        record.setVerified(true);
        codeRepository.save(record);
        return VerifyCodeResponse.success();
    }

    private String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
