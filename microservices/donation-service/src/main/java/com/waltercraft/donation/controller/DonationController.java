package com.waltercraft.donation.controller;

import com.waltercraft.donation.dto.DonationCreateRequest;
import com.waltercraft.donation.dto.DonationResponse;
import com.waltercraft.donation.service.DonationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/donations")
public class DonationController {

    private final DonationService donationService;

    public DonationController(DonationService donationService) {
        this.donationService = donationService;
    }

    @GetMapping
    public PagedModel<DonationResponse> listDonations(
            @PageableDefault(size = 25, sort = "donatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(donationService.list(pageable));
    }

    @GetMapping("/{id}")
    public DonationResponse getDonation(@PathVariable long id) {
        return donationService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DonationResponse createDonation(@Valid @RequestBody DonationCreateRequest request) {
        return donationService.create(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDonation(@PathVariable long id) {
        donationService.delete(id);
    }
}
