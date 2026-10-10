package org.example.p3.repository;

import org.example.p3.model.DonationTypeModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonationTypeRepository extends JpaRepository<DonationTypeModel, Long> {
    List<DonationTypeModel> findAllByName(String name);
    List<DonationTypeModel> findAllById(long id);
}
