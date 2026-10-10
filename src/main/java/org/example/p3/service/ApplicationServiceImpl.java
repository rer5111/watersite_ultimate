package org.example.p3.service;

import lombok.RequiredArgsConstructor;
import org.example.p3.model.ApplicationModel;
import org.example.p3.repository.ApplicationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;

    @Override
    public List<ApplicationModel> findById(long id) {
        return applicationRepository.findById(id).stream().toList();
    }

    @Override
    public List<ApplicationModel> findAll() {
        return applicationRepository.findAll();
    }

    @Override
    public void addApplication(ApplicationModel Application) {
        applicationRepository.save(Application);
    }

    @Override
    public void updateApplication(ApplicationModel Application) {
        applicationRepository.save(Application);
    }

    @Override
    public void deleteApplication(long id) {
        applicationRepository.deleteById(id);
    }

    @Override
    public List<ApplicationModel> findPage(int page) {
        if (page < 1) {
            return List.of();
        }
        Pageable pageable = PageRequest.of(page - 1, 5);
        return applicationRepository.findAll(pageable).getContent();
    }

    @Override
    public int getPages() {
        Pageable pageable = PageRequest.of(0, 5);
        return applicationRepository.findAll(pageable).getTotalPages();
    }
}
