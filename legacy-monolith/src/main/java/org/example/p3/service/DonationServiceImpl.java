package org.example.p3.service;

import lombok.RequiredArgsConstructor;
import org.example.p3.model.DonationModel;
import org.example.p3.model.DonationTypeModel;
import org.example.p3.repository.DonationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DonationServiceImpl implements DonationService {

    private final DonationRepository donationRepository;

    @Override
    public List<DonationModel> findById(long id) {
        return donationRepository.findById(id).stream().toList();
    }

    @Override
    public List<DonationModel> findByType(DonationTypeModel dt) {
        return donationRepository.findAllByDonationTypeIs(dt);
    }

    @Override
    public List<DonationModel> findAll() {
        return donationRepository.findAll();
    }

    @Override
    public void addDonation(DonationModel Donation) {
        donationRepository.save(Donation);
    }

    @Override
    public void updateDonation(DonationModel Donation) {
        donationRepository.save(Donation);
    }

    @Override
    public void deleteDonation(long id) {
        donationRepository.deleteById(id);
    }

    @Override
    public List<DonationModel> findPage(int page) {
        if (page < 1) {
            return List.of();
        }
        Pageable pageable = PageRequest.of(page - 1, 5);
        return donationRepository.findAll(pageable).getContent();
    }

    @Override
    public int getPages() {
        Pageable pageable = PageRequest.of(0, 5);
        return donationRepository.findAll(pageable).getTotalPages();
    }
}
