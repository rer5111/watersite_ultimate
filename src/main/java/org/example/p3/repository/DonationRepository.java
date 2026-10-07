package org.example.p3.repository;

import org.example.p3.model.DonationModel;
import org.example.p3.model.DonationTypeModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public interface DonationRepository extends JpaRepository<DonationModel, Long> {
    List<DonationModel> findAllByDonationTypeIs(DonationTypeModel dt);
    List<DonationModel> findAllById(int id);
}
