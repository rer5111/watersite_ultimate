package org.example.p3.service;

import org.example.p3.model.DonationModel;
import org.example.p3.model.DonationTypeModel;

import java.util.List;

public interface DonationService {
    List<DonationModel> findById(long id);
    List<DonationModel> findByType(DonationTypeModel dt);
    List<DonationModel> findAll();
    void addDonation(DonationModel Donation);
    void updateDonation(DonationModel Donation);
    void deleteDonation(long id);
    List<DonationModel> findPage(int page);
    int getPages();
}
