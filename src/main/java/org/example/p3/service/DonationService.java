package org.example.p3.service;

import org.example.p3.model.AccountModel;
import org.example.p3.model.DonationModel;
import org.example.p3.model.DonationModel;
import org.example.p3.model.DonationTypeModel;

import java.util.List;

public interface DonationService {
    public List<DonationModel> findByID(long id);
    public List<DonationModel> findByType(DonationTypeModel dt);
    public List<DonationModel> findAll();
    public DonationModel addDonation(DonationModel Donation);
    public DonationModel updateDonation(DonationModel Donation);
    public void deleteDonation(long id);
    public List<DonationModel> findPage(int page);
    public int getPages();
}
