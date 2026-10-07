package org.example.p3.service;

import org.example.p3.model.SeasonModel;

import java.util.List;

public interface SeasonService {
    public List<SeasonModel> findByID(long id);
    public List<SeasonModel> findAll();
    public SeasonModel addSeason(SeasonModel Season);
    public SeasonModel updateSeason(SeasonModel Season);
    public void deleteSeason(long id);
    public List<SeasonModel> findPage(int page);
    public int getPages();
}
