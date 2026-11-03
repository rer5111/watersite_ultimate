package org.example.p3.service;

import lombok.RequiredArgsConstructor;
import org.example.p3.model.*;
import org.example.p3.repository.DonationTypeRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DonationTypeServiceImpl implements DonationTypeService {

    private final DonationTypeRepository donationTypeRepository;

    @Override
    public List<DonationTypeModel> findById(long id) {
        return donationTypeRepository.findById(id).stream().toList();
    }

    @Override
    public List<DonationTypeModel> findByName(String name) {
        return donationTypeRepository.findAllByName(name);
    }

    @Override
    public List<DonationTypeModel> findAll() {
        return donationTypeRepository.findAll();
    }

    @Override
    public void addDonationType(DonationTypeModel DonationType) {
        donationTypeRepository.save(DonationType);
    }

    @Override
    public void updateDonationType(DonationTypeModel DonationType) {
        donationTypeRepository.save(DonationType);
    }

    @Override
    public void deleteDonationType(long id) {
        donationTypeRepository.deleteById(id);
    }

    @Override
    public List<DonationTypeModel> findPage(int page) {
        if (page < 1) {
            return List.of();
        }
        Pageable pageable = PageRequest.of(page - 1, 5);
        return donationTypeRepository.findAll(pageable).getContent();
    }

    @Override
    public int getPages() {
        Pageable pageable = PageRequest.of(0, 5);
        return donationTypeRepository.findAll(pageable).getTotalPages();
    }
}
