package org.example.p3.service;

import org.example.p3.model.DonationModel;
import org.example.p3.model.DonationTypeModel;
import org.example.p3.repository.DonationRepository;
import org.springframework.stereotype.Service;
import org.example.p3.service.DonationTypeService;

import java.util.ArrayList;
import java.util.List;

@Service
public class DonationServiceImpl implements DonationService {
    private final DonationRepository DonationRepository;

    public DonationServiceImpl(DonationRepository DonationRepository) {
        this.DonationRepository = DonationRepository;
    }

    @Override
    public List<DonationModel> findByID(long id) {
        return DonationRepository.findById(id).stream().toList();
    }

    @Override
    public List<DonationModel> findByType(DonationTypeModel dt) {
        return DonationRepository.findAllByDonationTypeIs(dt);
    }

    @Override
    public List<DonationModel> findAll() {
        return DonationRepository.findAll();
    }

    @Override
    public DonationModel addDonation(DonationModel Donation) {
        return DonationRepository.save(Donation);
    }

    @Override
    public DonationModel updateDonation(DonationModel Donation) {
        return DonationRepository.save(Donation);
    }

    @Override
    public void deleteDonation(long id) {
        DonationRepository.deleteById(id);
    }
    @Override
    public List<DonationModel> findPage(int page){
        List<DonationModel> Donations = DonationRepository.findAll();
        if (Donations.isEmpty()){
            return new ArrayList<>();
        } else if (Donations.size() <page*5-5 || page < 1) {
            return new ArrayList<>();
        }
        if (page*5 < Donations.size()){
            return Donations.subList(page*5-5, page*5);
        }
        else{
            return Donations.subList(page*5-5, Donations.size());
        }
    }
    @Override
    public int getPages() {
        return (DonationRepository.findAll().size()-1)/5+1;
    }
}
