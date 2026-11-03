package org.example.p3.service;

import org.example.p3.model.DonationTypeModel;

import java.util.List;

public interface DonationTypeService {
    List<DonationTypeModel> findById(long id);
    List<DonationTypeModel> findByName(String name);
    List<DonationTypeModel> findAll();
    void addDonationType(DonationTypeModel DonationType);
    void updateDonationType(DonationTypeModel DonationType);
    void deleteDonationType(long id);
    List<DonationTypeModel> findPage(int page);
    int getPages();
}
