package org.example.p3.service;

import org.example.p3.model.SeasonModel;

import java.util.List;

public interface SeasonService {
    List<SeasonModel> findById(long id);
    List<SeasonModel> findAll();
    List<SeasonModel> findByTitle(String title);
    void addSeason(SeasonModel Season);
    void updateSeason(SeasonModel Season);
    void deleteSeason(long id);
    List<SeasonModel> findPage(int page);
    int getPages();
}
