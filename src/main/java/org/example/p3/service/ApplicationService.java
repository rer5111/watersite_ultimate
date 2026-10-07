package org.example.p3.service;

import org.example.p3.model.AccountModel;
import org.example.p3.model.ApplicationModel;
import org.example.p3.model.ApplicationModel;

import java.util.List;

public interface ApplicationService {
    public List<ApplicationModel> findByID(long id);
    public List<ApplicationModel> findAll();
    public ApplicationModel addApplication(ApplicationModel Application);
    public ApplicationModel updateApplication(ApplicationModel Application);
    public void deleteApplication(long id);
    public List<ApplicationModel> findPage(int page);
    public int getPages();
}
