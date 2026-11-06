package org.example.userservice.repository;

import org.example.userservice.model.AccountModel;
import org.example.userservice.model.RoleEnum;
import org.example.userservice.model.UserModel;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {
    @EntityGraph(attributePaths = {"account", "roles"})
    @Query("SELECT u FROM UserModel u ORDER BY u.id")
    List<UserModel> findAllForAdmin();

    List<UserModel> findAllById(long id);
    List<UserModel> findAllByName(String name);
    List<UserModel> findByAccount(AccountModel account);
    List<UserModel> findByRolesContainingOrderByAccount_IdAsc(RoleEnum role);

    @EntityGraph(attributePaths = "seasons")
    java.util.Optional<UserModel> findWithSeasonsById(long id);

    @Query("SELECT u FROM UserModel u LEFT JOIN u.account a WHERE LOWER(u.name) = LOWER(:identifier) OR LOWER(u.email) = LOWER(:identifier) OR LOWER(a.username) = LOWER(:identifier)")
    List<UserModel> findByIdentifier(@Param("identifier") String identifier);
}
