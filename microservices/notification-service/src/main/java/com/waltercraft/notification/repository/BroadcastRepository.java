package com.waltercraft.notification.repository;

import com.waltercraft.notification.entity.Broadcast;
import com.waltercraft.notification.entity.BroadcastAudience;
import com.waltercraft.notification.entity.BroadcastStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface BroadcastRepository extends JpaRepository<Broadcast, Long> {
    List<Broadcast> findAllByOrderBySentAtDesc();

    @Query("""
            select b from Broadcast b
            where b.status = :status and b.audience in :audiences and b.recipientUserId is null
            order by b.sentAt desc
            """)
    List<Broadcast> findVisible(
            @Param("audiences") Collection<BroadcastAudience> audiences,
            @Param("status") BroadcastStatus status
    );

    @Query("""
            select b from Broadcast b
            where b.status = :status
              and ((b.recipientUserId is null and b.audience in :audiences) or b.recipientUserId = :userId)
            order by b.sentAt desc
            """)
    List<Broadcast> findVisibleForUser(
            @Param("userId") long userId,
            @Param("audiences") Collection<BroadcastAudience> audiences,
            @Param("status") BroadcastStatus status
    );
}
