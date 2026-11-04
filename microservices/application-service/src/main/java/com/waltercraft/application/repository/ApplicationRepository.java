package com.waltercraft.application.repository;

import com.waltercraft.application.entity.ServerApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<ServerApplication, Long> {

    Page<ServerApplication> findAllByApplicantUsername(String applicantUsername, Pageable pageable);

    Optional<ServerApplication> findByIdAndApplicantUsername(Long id, String applicantUsername);
}
