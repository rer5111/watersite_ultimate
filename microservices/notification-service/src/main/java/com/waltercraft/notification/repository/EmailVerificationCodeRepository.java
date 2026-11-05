package com.waltercraft.notification.repository;

import com.waltercraft.notification.entity.EmailVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, Long> {
    Optional<EmailVerificationCode> findTopByEmailIgnoreCaseAndPurposeOrderByIdDesc(String email, String purpose);
}
