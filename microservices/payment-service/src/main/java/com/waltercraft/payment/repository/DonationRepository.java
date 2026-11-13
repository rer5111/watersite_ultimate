package com.waltercraft.payment.repository;

import com.waltercraft.payment.entity.Donation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DonationRepository extends JpaRepository<Donation, UUID> {
    Optional<Donation> findByPaymentReference(String paymentReference);

    Page<Donation> findAllByUserId(Long userId, Pageable pageable);
}
