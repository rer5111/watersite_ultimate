package com.waltercraft.season.repository;

import com.waltercraft.season.entity.Season;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeasonRepository extends JpaRepository<Season, Long> {

    Page<Season> findAllByActiveTrue(Pageable pageable);

    Optional<Season> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
