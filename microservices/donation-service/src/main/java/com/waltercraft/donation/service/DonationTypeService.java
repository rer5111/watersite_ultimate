package com.waltercraft.donation.service;

import com.waltercraft.donation.dto.DonationTypeCreateRequest;
import com.waltercraft.donation.dto.DonationTypeResponse;
import com.waltercraft.donation.dto.DonationTypeUpdateRequest;
import com.waltercraft.donation.entity.DonationType;
import com.waltercraft.donation.exception.ConflictException;
import com.waltercraft.donation.exception.ResourceNotFoundException;
import com.waltercraft.donation.repository.DonationRepository;
import com.waltercraft.donation.repository.DonationTypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DonationTypeService {

    private final DonationTypeRepository donationTypeRepository;
    private final DonationRepository donationRepository;

    public DonationTypeService(DonationTypeRepository donationTypeRepository, DonationRepository donationRepository) {
        this.donationTypeRepository = donationTypeRepository;
        this.donationRepository = donationRepository;
    }

    public Page<DonationTypeResponse> listPublic(Pageable pageable) {
        return donationTypeRepository.findAllByActiveTrue(pageable).map(DonationTypeResponse::from);
    }

    public Page<DonationTypeResponse> listAll(Pageable pageable) {
        return donationTypeRepository.findAll(pageable).map(DonationTypeResponse::from);
    }

    public DonationTypeResponse get(long id) {
        return DonationTypeResponse.from(findType(id));
    }

    @Transactional
    public DonationTypeResponse create(DonationTypeCreateRequest request) {
        DonationType donationType = new DonationType(request.code(), request.name(), request.description(),
                request.priceRub(), request.displayOrder(), request.active() == null || request.active());
        return DonationTypeResponse.from(donationTypeRepository.saveAndFlush(donationType));
    }

    @Transactional
    public DonationTypeResponse update(long id, DonationTypeUpdateRequest request) {
        DonationType donationType = findType(id);
        if (donationType.getVersion() != request.version()) {
            throw new ConflictException("Тип доната уже изменён. Обновите данные и повторите запрос.");
        }
        donationType.replace(request.code(), request.name(), request.description(), request.priceRub(),
                request.displayOrder(), request.active());
        try {
            return DonationTypeResponse.from(donationTypeRepository.saveAndFlush(donationType));
        } catch (ObjectOptimisticLockingFailureException exception) {
            throw new ConflictException("Тип доната уже изменён. Обновите данные и повторите запрос.");
        }
    }

    @Transactional
    public void delete(long id) {
        DonationType donationType = findType(id);
        if (donationRepository.countByDonationType_Id(id) > 0) {
            throw new ConflictException("Нельзя удалить тип доната, по которому уже есть записи.");
        }
        donationTypeRepository.delete(donationType);
        donationTypeRepository.flush();
    }

    private DonationType findType(long id) {
        return donationTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Тип доната не найден"));
    }
}
