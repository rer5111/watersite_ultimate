package org.example.p3.service;

import org.example.p3.model.DonationTypeModel;

import java.util.List;

public interface DonationTypeService {
    public List<DonationTypeModel> findByID(long id);
    public List<DonationTypeModel> findByName(String name);
    public List<DonationTypeModel> findAll();
    public DonationTypeModel addDonationType(DonationTypeModel DonationType);
    public DonationTypeModel updateDonationType(DonationTypeModel DonationType);
    public void deleteDonationType(long id);
    public List<DonationTypeModel> findPage(int page);
    public int getPages();
}
