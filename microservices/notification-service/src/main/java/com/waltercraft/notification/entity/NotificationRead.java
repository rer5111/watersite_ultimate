package com.waltercraft.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(name = "notification_reads", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "broadcast_id"}))
public class NotificationRead {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification_read_seq")
    @SequenceGenerator(name = "notification_read_seq", sequenceName = "notification_sequence", allocationSize = 1)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private long userId;

    @Column(name = "broadcast_id", nullable = false)
    private long broadcastId;

    @Column(name = "read_at", nullable = false)
    private Instant readAt;

    public NotificationRead() {
    }

    public NotificationRead(long userId, long broadcastId, Instant readAt) {
        this.userId = userId;
        this.broadcastId = broadcastId;
        this.readAt = readAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getBroadcastId() {
        return broadcastId;
    }

    public void setBroadcastId(long broadcastId) {
        this.broadcastId = broadcastId;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public void setReadAt(Instant readAt) {
        this.readAt = readAt;
    }
}
