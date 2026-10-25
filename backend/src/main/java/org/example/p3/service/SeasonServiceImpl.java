package org.example.p3.service;

import lombok.RequiredArgsConstructor;
import org.example.p3.model.SeasonModel;
import org.example.p3.repository.SeasonRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeasonServiceImpl implements SeasonService {

    private final SeasonRepository seasonRepository;

    @Override
    public List<SeasonModel> findById(long id) {
        return seasonRepository.findById(id).stream().toList();
    }

    @Override
    public List<SeasonModel> findAll() {
        return seasonRepository.findAll();
    }

    public List<SeasonModel> findByTitle(String title) {return seasonRepository.findByTitle(title);}

    @Override
    public void addSeason(SeasonModel Season) {
        seasonRepository.save(Season);
    }

    @Override
    public void updateSeason(SeasonModel Season) {
        seasonRepository.save(Season);
    }

    @Override
    public void deleteSeason(long id) {
        seasonRepository.deleteById(id);
    }

    @Override
    public List<SeasonModel> findPage(int page) {
        if (page < 1) {
            return List.of();
        }
        Pageable pageable = PageRequest.of(page - 1, 5);
        return seasonRepository.findAll(pageable).getContent();
    }

    @Override
    public int getPages() {
        Pageable pageable = PageRequest.of(0, 5);
        return seasonRepository.findAll(pageable).getTotalPages();
    }
}
