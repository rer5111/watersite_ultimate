package com.waltercraft.donation.service;

import com.waltercraft.donation.dto.DonationCreateRequest;
import com.waltercraft.donation.dto.DonationResponse;
import com.waltercraft.donation.entity.Donation;
import com.waltercraft.donation.entity.DonationType;
import com.waltercraft.donation.exception.ConflictException;
import com.waltercraft.donation.exception.InvalidReferenceException;
import com.waltercraft.donation.exception.ResourceNotFoundException;
import com.waltercraft.donation.repository.DonationRepository;
import com.waltercraft.donation.repository.DonationTypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DonationService {

    private final DonationRepository donationRepository;
    private final DonationTypeRepository donationTypeRepository;

    public DonationService(DonationRepository donationRepository, DonationTypeRepository donationTypeRepository) {
        this.donationRepository = donationRepository;
        this.donationTypeRepository = donationTypeRepository;
    }

    public Page<DonationResponse> list(Pageable pageable) {
        return donationRepository.findAll(pageable).map(DonationResponse::from);
    }

    public DonationResponse get(long id) {
        return donationRepository.findWithDonationTypeById(id)
                .map(DonationResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Запись о донате не найдена"));
    }

    @Transactional
    public DonationResponse create(DonationCreateRequest request) {
        DonationType donationType = donationTypeRepository.findById(request.donationTypeId())
                .orElseThrow(() -> new InvalidReferenceException("donationTypeId", "Такой тип доната не найден"));
        if (!donationType.isActive()) {
            throw new ConflictException("Выбранный тип доната отключён.");
        }
        String paymentReference = request.paymentReference();
        if (paymentReference != null && paymentReference.isBlank()) {
            paymentReference = null;
        }
        Donation donation = new Donation(request.playerId(), donationType, donationType.getPriceRub(), paymentReference);
        return DonationResponse.from(donationRepository.saveAndFlush(donation));
    }

    @Transactional
    public void delete(long id) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Запись о донате не найдена"));
        donationRepository.delete(donation);
        donationRepository.flush();
    }
}
