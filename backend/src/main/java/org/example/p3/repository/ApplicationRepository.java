package org.example.p3.repository;

import org.example.p3.model.ApplicationModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<ApplicationModel, Long> {
    List<ApplicationModel> findAllById(long id);
}
