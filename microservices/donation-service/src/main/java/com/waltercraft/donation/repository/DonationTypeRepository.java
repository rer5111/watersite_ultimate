package com.waltercraft.donation.repository;

import com.waltercraft.donation.entity.DonationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonationTypeRepository extends JpaRepository<DonationType, Long> {

    Page<DonationType> findAllByActiveTrue(Pageable pageable);

    long countByActiveTrue();
}
