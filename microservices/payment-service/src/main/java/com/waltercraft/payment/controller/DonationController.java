package com.waltercraft.payment.controller;

import com.waltercraft.payment.dto.DonationCreateRequest;
import com.waltercraft.payment.dto.DonationCreateResponse;
import com.waltercraft.payment.dto.DonationStatusResponse;
import com.waltercraft.payment.service.DonationPaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/payments/donations")
public class DonationController {

    private final DonationPaymentService donationPaymentService;

    public DonationController(DonationPaymentService donationPaymentService) {
        this.donationPaymentService = donationPaymentService;
    }

    @PostMapping
    public ResponseEntity<DonationCreateResponse> createDonation(
            @Valid @RequestBody DonationCreateRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = extractUserId(jwt);
        String username = jwt.getSubject() != null ? jwt.getSubject() : "anonymous";
        DonationCreateResponse response = donationPaymentService.createDonation(userId, username, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public DonationStatusResponse getDonation(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = extractUserId(jwt);
        boolean isAdmin = isAdmin(jwt);
        return donationPaymentService.getDonation(id, userId, isAdmin);
    }

    private Long extractUserId(Jwt jwt) {
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Для выполнения запроса требуется авторизация");
        }
        Object value = jwt.getClaim("user_id");
        if (value instanceof Number number && number.longValue() > 0) {
            return number.longValue();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Идентификатор пользователя не найден в токене");
    }

    private boolean isAdmin(Jwt jwt) {
        if (jwt == null) {
            return false;
        }
        List<String> roles = jwt.getClaimAsStringList("roles");
        return roles != null && roles.contains("ADMIN");
    }
}
