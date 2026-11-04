package org.example.userservice.repository;

import org.example.userservice.model.SeasonModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeasonRepository extends JpaRepository<SeasonModel, Long> {
    List<SeasonModel> findByTitle(String title);
    List<SeasonModel> findAllById(long id);
}
