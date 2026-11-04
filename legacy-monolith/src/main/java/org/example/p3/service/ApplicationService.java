package org.example.p3.service;

import org.example.p3.model.ApplicationModel;

import java.util.List;

public interface ApplicationService {
    List<ApplicationModel> findById(long id);
    List<ApplicationModel> findAll();
    void addApplication(ApplicationModel Application);
    void updateApplication(ApplicationModel Application);
    void deleteApplication(long id);
    List<ApplicationModel> findPage(int page);
    int getPages();
}
