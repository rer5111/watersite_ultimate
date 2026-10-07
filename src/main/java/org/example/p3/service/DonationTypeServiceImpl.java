package org.example.p3.service;

import org.example.p3.model.DonationModel;
import org.example.p3.model.DonationTypeModel;
import org.example.p3.model.DonationTypeModel;
import org.example.p3.model.DonationTypeModel;
import org.example.p3.repository.DonationTypeRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DonationTypeServiceImpl implements DonationTypeService {
    private final DonationTypeRepository DonationTypeRepository;

    public DonationTypeServiceImpl(DonationTypeRepository DonationTypeRepository) {
        this.DonationTypeRepository = DonationTypeRepository;
    }

    @Override
    public List<DonationTypeModel> findByID(long id) {
        return DonationTypeRepository.findById(id).stream().toList();
    }

    @Override
    public List<DonationTypeModel> findByName(String name) {
        return DonationTypeRepository.findAllByName(name);
    }

    @Override
    public List<DonationTypeModel> findAll() {
        return DonationTypeRepository.findAll();
    }

    @Override
    public DonationTypeModel addDonationType(DonationTypeModel DonationType) {
        return DonationTypeRepository.save(DonationType);
    }

    @Override
    public DonationTypeModel updateDonationType(DonationTypeModel DonationType) {
        return DonationTypeRepository.save(DonationType);
    }

    @Override
    public void deleteDonationType(long id) {
        DonationTypeRepository.deleteById(id);
    }
    @Override
    public List<DonationTypeModel> findPage(int page){
        List<DonationTypeModel> Donations = DonationTypeRepository.findAll();
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
    public int getPages(){
        return (DonationTypeRepository.findAll().size()-1)/5+1;
    }
}
