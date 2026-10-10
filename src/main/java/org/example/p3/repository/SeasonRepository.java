package org.example.p3.repository;

import org.example.p3.model.SeasonModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeasonRepository extends JpaRepository<SeasonModel, Long> {
    List<SeasonModel> findAllById(long id);
}
