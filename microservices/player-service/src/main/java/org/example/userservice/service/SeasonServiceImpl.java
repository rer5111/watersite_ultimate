package org.example.userservice.service;

import org.example.userservice.model.SeasonModel;
import org.example.userservice.repository.SeasonRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class SeasonServiceImpl implements SeasonService {

    private final SeasonRepository seasonRepository;

    public SeasonServiceImpl(SeasonRepository seasonRepository) {
        this.seasonRepository = seasonRepository;
    }

    @Override
    public List<SeasonModel> findById(long id) {
        return seasonRepository.findById(id).stream().toList();
    }

    @Override
    public List<SeasonModel> findAll() {
        return seasonRepository.findAll();
    }

    @Override
    public List<SeasonModel> findByTitle(String title) {
        return seasonRepository.findByTitle(title);
    }

    @Override
    public void addSeason(@NonNull SeasonModel season) {
        seasonRepository.save(Objects.requireNonNull(season));
    }

    @Override
    public void updateSeason(@NonNull SeasonModel season) {
        seasonRepository.save(Objects.requireNonNull(season));
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
