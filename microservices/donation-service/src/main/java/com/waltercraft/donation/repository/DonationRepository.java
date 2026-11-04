package com.waltercraft.donation.repository;

import com.waltercraft.donation.entity.Donation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DonationRepository extends JpaRepository<Donation, Long> {

    @Override
    @EntityGraph(attributePaths = "donationType")
    Page<Donation> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "donationType")
    Optional<Donation> findWithDonationTypeById(Long id);

    long countByDonationType_Id(Long donationTypeId);
}
