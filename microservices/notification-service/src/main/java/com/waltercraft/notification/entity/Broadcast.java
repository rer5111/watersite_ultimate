package com.waltercraft.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "broadcasts")
public class Broadcast {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification_seq")
    @SequenceGenerator(name = "notification_seq", sequenceName = "notification_sequence", allocationSize = 1)
    private Long id;

    @Column(nullable = false, length = 120)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BroadcastCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private BroadcastAudience audience;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    @Column(nullable = false)
    private int recipients;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BroadcastStatus status;

    @Column(name = "recipient_user_id")
    private Long recipientUserId;

    public Broadcast() {
    }

    public Broadcast(String subject, BroadcastCategory category, BroadcastAudience audience, String message,
                     Instant sentAt, int recipients, BroadcastStatus status) {
        this(subject, category, audience, message, sentAt, recipients, status, null);
    }

    public Broadcast(String subject, BroadcastCategory category, BroadcastAudience audience, String message,
                     Instant sentAt, int recipients, BroadcastStatus status, Long recipientUserId) {
        this.subject = subject;
        this.category = category;
        this.audience = audience;
        this.message = message;
        this.sentAt = sentAt;
        this.recipients = recipients;
        this.status = status;
        this.recipientUserId = recipientUserId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public BroadcastCategory getCategory() {
        return category;
    }

    public void setCategory(BroadcastCategory category) {
        this.category = category;
    }

    public BroadcastAudience getAudience() {
        return audience;
    }

    public void setAudience(BroadcastAudience audience) {
        this.audience = audience;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }

    public int getRecipients() {
        return recipients;
    }

    public void setRecipients(int recipients) {
        this.recipients = recipients;
    }

    public BroadcastStatus getStatus() {
        return status;
    }

    public void setStatus(BroadcastStatus status) {
        this.status = status;
    }

    public Long getRecipientUserId() {
        return recipientUserId;
    }

    public void setRecipientUserId(Long recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Broadcast broadcast = (Broadcast) o;
        return Objects.equals(id, broadcast.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
