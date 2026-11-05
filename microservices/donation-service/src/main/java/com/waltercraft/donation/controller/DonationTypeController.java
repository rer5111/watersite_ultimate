package com.waltercraft.donation.controller;

import com.waltercraft.donation.dto.DonationTypeCreateRequest;
import com.waltercraft.donation.dto.DonationTypeResponse;
import com.waltercraft.donation.dto.DonationTypeUpdateRequest;
import com.waltercraft.donation.service.DonationTypeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1")
public class DonationTypeController {

    private final DonationTypeService donationTypeService;

    public DonationTypeController(DonationTypeService donationTypeService) {
        this.donationTypeService = donationTypeService;
    }

    @GetMapping("/donation-types")
    public ResponseEntity<PagedModel<DonationTypeResponse>> listDonationTypes(
            @PageableDefault(size = 50, sort = {"displayOrder", "id"}) Pageable pageable) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)).cachePublic())
                .body(new PagedModel<>(donationTypeService.listPublic(pageable)));
    }

    @GetMapping("/admin/donation-types")
    public PagedModel<DonationTypeResponse> listAllDonationTypes(
            @PageableDefault(size = 50, sort = {"displayOrder", "id"}) Pageable pageable) {
        return new PagedModel<>(donationTypeService.listAll(pageable));
    }

    @GetMapping("/admin/donation-types/{id}")
    public DonationTypeResponse getDonationType(@PathVariable long id) {
        return donationTypeService.get(id);
    }

    @PostMapping("/admin/donation-types")
    @ResponseStatus(HttpStatus.CREATED)
    public DonationTypeResponse createDonationType(@Valid @RequestBody DonationTypeCreateRequest request) {
        return donationTypeService.create(request);
    }

    @PutMapping("/admin/donation-types/{id}")
    public DonationTypeResponse updateDonationType(
            @PathVariable long id, @Valid @RequestBody DonationTypeUpdateRequest request) {
        return donationTypeService.update(id, request);
    }

    @DeleteMapping("/admin/donation-types/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDonationType(@PathVariable long id) {
        donationTypeService.delete(id);
    }
}
