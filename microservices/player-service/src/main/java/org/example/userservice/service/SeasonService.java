package org.example.userservice.service;

import org.example.userservice.dto.SeasonParticipationResponse;
import org.example.userservice.model.SeasonModel;
import org.springframework.lang.NonNull;

import java.util.List;

public interface SeasonService {
    List<SeasonModel> findById(long id);
    List<SeasonModel> findAll();
    List<SeasonModel> findByTitle(String title);
    void addSeason(@NonNull SeasonModel season);
    void updateSeason(@NonNull SeasonModel season);
    void deleteSeason(long id);
    List<SeasonModel> findPage(int page);
    int getPages();
    SeasonParticipationResponse getParticipation(long playerId);
}
