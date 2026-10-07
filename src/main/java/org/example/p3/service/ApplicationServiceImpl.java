package org.example.p3.service;

import org.example.p3.model.ApplicationModel;
import org.example.p3.model.ApplicationModel;
import org.example.p3.repository.ApplicationRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ApplicationServiceImpl implements ApplicationService {
    private final ApplicationRepository ApplicationRepository;

    public ApplicationServiceImpl(ApplicationRepository ApplicationRepository) {
        this.ApplicationRepository = ApplicationRepository;
    }

    @Override
    public List<ApplicationModel> findByID(long id) {
        return ApplicationRepository.findById(id).stream().toList();
    }

    @Override
    public List<ApplicationModel> findAll() {
        return ApplicationRepository.findAll();
    }

    @Override
    public ApplicationModel addApplication(ApplicationModel Application) {
        return ApplicationRepository.save(Application);
    }

    @Override
    public ApplicationModel updateApplication(ApplicationModel Application) {
        return ApplicationRepository.save(Application);
    }

    @Override
    public void deleteApplication(long id) {
        ApplicationRepository.deleteById(id);
    }
    @Override
    public List<ApplicationModel> findPage(int page){
        List<ApplicationModel> Applications = ApplicationRepository.findAll();
        if (Applications.isEmpty()){
            return new ArrayList<>();
        } else if (Applications.size() <page*5-5 || page < 1) {
            return new ArrayList<>();
        }
        if (page*5 < Applications.size()){
            return Applications.subList(page*5-5, page*5);
        }
        else{
            return Applications.subList(page*5-5, Applications.size());
        }
    }
    @Override
    public int getPages(){
        return (ApplicationRepository.findAll().size()-1)/5+1;
    }
}
