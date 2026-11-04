package org.example.userservice.controller;

import org.example.userservice.model.SeasonModel;
import org.example.userservice.service.SeasonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/season")
public class SeasonController {

    private final SeasonService seasonService;

    public SeasonController(SeasonService seasonService) {
        this.seasonService = seasonService;
    }

    @PutMapping(produces = APPLICATION_JSON_VALUE)
    public HttpStatus registerSeason(
            @RequestParam String title,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam String desc,
            @RequestParam String img
    ) {
        try {
            SeasonModel seasonTypeModel = new SeasonModel(0, title, startDate, endDate, desc, img);
            seasonService.addSeason(seasonTypeModel);
            return HttpStatus.OK;
        } catch (Exception e) {
            return HttpStatus.BAD_REQUEST;
        }
    }

    @PostMapping(produces = APPLICATION_JSON_VALUE)
    public HttpStatus updateSeason(
            @RequestParam int id,
            @RequestParam String title,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam String desc,
            @RequestParam String img
    ) {
        List<SeasonModel> seasons = seasonService.findById(id);
        if (seasons.isEmpty()) {
            return HttpStatus.NOT_FOUND;
        }
        SeasonModel seasonTypeModel = seasons.get(0);
        seasonTypeModel.setTitle(title);
        seasonTypeModel.setStartDate(startDate);
        seasonTypeModel.setEndDate(endDate);
        seasonTypeModel.setDescription(desc);
        seasonTypeModel.setImage(img);
        seasonService.updateSeason(seasonTypeModel);
        return HttpStatus.OK;
    }

    @DeleteMapping(value = "/{key}", produces = APPLICATION_JSON_VALUE)
    public HttpStatus deleteSeason(@PathVariable int key) {
        try {
            seasonService.deleteSeason(key);
            return HttpStatus.OK;
        } catch (Exception e) {
            return HttpStatus.NOT_FOUND;
        }
    }

    @GetMapping(path = "/pages", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> getPages() {
        return ResponseEntity.ok(seasonService.getPages());
    }

    @GetMapping(path = "/all", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SeasonModel>> getAll() {
        return ResponseEntity.ok(seasonService.findAll());
    }

    @GetMapping(path = "/findPage/{page}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SeasonModel>> getPage(@PathVariable int page) {
        return ResponseEntity.ok(seasonService.findPage(page));
    }

    @GetMapping(value = "/findID/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SeasonModel>> findById(@PathVariable int id) {
        return ResponseEntity.ok(seasonService.findById(id));
    }

    @GetMapping(value = "/findTitle/{title}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SeasonModel>> findByTitle(@PathVariable String title) {
        return ResponseEntity.ok(seasonService.findByTitle(title));
    }
}
