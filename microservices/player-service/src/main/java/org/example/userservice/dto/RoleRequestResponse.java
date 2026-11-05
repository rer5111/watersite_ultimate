package org.example.userservice.dto;

import java.time.LocalDateTime;

public record RoleRequestResponse(
        long id,
        long applicantId,
        String applicantName,
        String applicantEmail,
        String requestedRole,
        LocalDateTime submittedAt,
        String note,
        String status,
        LocalDateTime reviewedAt,
        String rejectionReason
) {
}
