package org.example.p3.service;

import org.example.p3.model.SeasonModel;
import org.example.p3.model.SeasonModel;
import org.example.p3.model.SeasonModel;
import org.example.p3.repository.SeasonRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SeasonServiceImpl implements SeasonService {
    private final SeasonRepository SeasonRepository;

    public SeasonServiceImpl(SeasonRepository SeasonRepository) {
        this.SeasonRepository = SeasonRepository;
    }

    @Override
    public List<SeasonModel> findByID(long id) {
        return SeasonRepository.findById(id).stream().toList();
    }

    @Override
    public List<SeasonModel> findAll() {
        return SeasonRepository.findAll();
    }

    @Override
    public SeasonModel addSeason(SeasonModel Season) {
        return SeasonRepository.save(Season);
    }

    @Override
    public SeasonModel updateSeason(SeasonModel Season) {
        return SeasonRepository.save(Season);
    }

    @Override
    public void deleteSeason(long id) {
        SeasonRepository.deleteById(id);
    }
    @Override
    public List<SeasonModel> findPage(int page){
        List<SeasonModel> Seasons = SeasonRepository.findAll();
        if (Seasons.isEmpty()){
            return new ArrayList<>();
        } else if (Seasons.size() <page*5-5 || page < 1) {
            return new ArrayList<>();
        }
        if (page*5 < Seasons.size()){
            return Seasons.subList(page*5-5, page*5);
        }
        else{
            return Seasons.subList(page*5-5, Seasons.size());
        }
    }
    @Override
    public int getPages(){
        return (SeasonRepository.findAll().size()-1)/5+1;
    }
}
