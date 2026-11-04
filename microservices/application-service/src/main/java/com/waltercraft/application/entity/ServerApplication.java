package com.waltercraft.application.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "applications")
public class ServerApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "applicant_username", nullable = false, length = 120)
    private String applicantUsername;

    @Column(name = "application_text", nullable = false, length = 4000)
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ApplicationStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected ServerApplication() {
    }

    public ServerApplication(String applicantUsername, String text) {
        this.applicantUsername = applicantUsername;
        this.text = text;
        this.status = ApplicationStatus.PENDING;
    }

    public void replace(String text, ApplicationStatus status) {
        this.text = text;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getApplicantUsername() { return applicantUsername; }
    public String getText() { return text; }
    public ApplicationStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }
}
