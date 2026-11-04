package com.waltercraft.application.dto;

import com.waltercraft.application.entity.ApplicationStatus;
import com.waltercraft.application.entity.ServerApplication;

import java.time.Instant;

public record ApplicationResponse(
        Long id,
        String applicantUsername,
        String text,
        ApplicationStatus status,
        Instant createdAt,
        Instant updatedAt,
        long version
) {
    public static ApplicationResponse from(ServerApplication application) {
        return new ApplicationResponse(
                application.getId(),
                application.getApplicantUsername(),
                application.getText(),
                application.getStatus(),
                application.getCreatedAt(),
                application.getUpdatedAt(),
                application.getVersion());
    }
}
