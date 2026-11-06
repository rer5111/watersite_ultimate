package org.example.userservice.repository;

import org.example.userservice.model.RoleRequest;
import org.example.userservice.model.RoleRequestStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRequestRepository extends JpaRepository<RoleRequest, Long> {
    @EntityGraph(attributePaths = "user")
    List<RoleRequest> findAllByOrderBySubmittedAtDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select request from RoleRequest request join fetch request.user where request.id = :id")
    Optional<RoleRequest> findByIdForUpdate(@Param("id") long id);

    boolean existsByUser_IdAndStatus(long userId, RoleRequestStatus status);
}
