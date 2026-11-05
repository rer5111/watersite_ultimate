package org.example.userservice.repository;

import org.example.userservice.model.RoleRequest;
import org.example.userservice.model.RoleRequestStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRequestRepository extends JpaRepository<RoleRequest, Long> {
    @EntityGraph(attributePaths = "user")
    List<RoleRequest> findAllByOrderBySubmittedAtDesc();

    boolean existsByUser_IdAndStatus(long userId, RoleRequestStatus status);
}
