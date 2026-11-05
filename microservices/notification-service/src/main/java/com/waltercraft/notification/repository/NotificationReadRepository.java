package com.waltercraft.notification.repository;

import com.waltercraft.notification.entity.NotificationRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface NotificationReadRepository extends JpaRepository<NotificationRead, Long> {

    @Query("select r.broadcastId from NotificationRead r where r.userId = :userId")
    Set<Long> findBroadcastIdsByUserId(@Param("userId") long userId);

    boolean existsByUserIdAndBroadcastId(long userId, long broadcastId);
}
