package com.waltercraft.notification.service;

import com.waltercraft.notification.dto.SendCodeResponse;
import com.waltercraft.notification.dto.VerifyCodeResponse;
import com.waltercraft.notification.entity.EmailVerificationCode;
import com.waltercraft.notification.exception.RateLimitExceededException;
import com.waltercraft.notification.repository.EmailVerificationCodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
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

    private static final Logger log = LoggerFactory.getLogger(VerificationCodeService.class);

    private static final int COOLDOWN_SECONDS = 60;
    private static final int EXPIRATION_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;
    private final EmailVerificationCodeRepository codeRepository;
    private final EmailSenderService emailSenderService;
    private final String verificationCodePepper;
    private final SecureRandom secureRandom = new SecureRandom();

    public VerificationCodeService(
            EmailVerificationCodeRepository codeRepository,
            EmailSenderService emailSenderService,
            @Value("${security.notification-service.verification-code-pepper}") String verificationCodePepper
    ) {
        this.codeRepository = codeRepository;
        this.emailSenderService = emailSenderService;
        if (verificationCodePepper == null || verificationCodePepper.length() < 32) {
            throw new IllegalArgumentException("Verification code pepper must contain at least 32 characters");
        }
        this.verificationCodePepper = verificationCodePepper;
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
        String codeHash = hash(normalizedEmail, purpose, rawCode);

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
        log.info("Verification code generated codeId={} purpose={} expiresAt={}",
                newCode.getId(), purpose, newCode.getExpiresAt());

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

        String providedHash = hash(normalizedEmail, purpose, code.trim());
        if (!MessageDigest.isEqual(record.getCodeHash().getBytes(StandardCharsets.UTF_8), providedHash.getBytes(StandardCharsets.UTF_8))) {
            record.setAttempts(record.getAttempts() + 1);
            codeRepository.save(record);
            int remainingAttempts = MAX_ATTEMPTS - record.getAttempts();
            log.warn("Verification code rejected codeId={} purpose={} remainingAttempts={}",
                    record.getId(), purpose, remainingAttempts);
            return VerifyCodeResponse.failure("Неверный код. Осталось попыток: " + remainingAttempts);
        }

        record.setVerified(true);
        codeRepository.save(record);
        log.info("Verification code verified codeId={} purpose={}", record.getId(), purpose);
        return VerifyCodeResponse.success();
    }

    String hash(String email, String purpose, String rawCode) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            byte[] keyBytes = verificationCodePepper.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
            mac.init(secretKey);
            String payload = email.trim().toLowerCase() + ":" + purpose.trim().toUpperCase() + ":" + rawCode.trim();
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to calculate HMAC-SHA256 for verification code", e);
        }
    }
}
