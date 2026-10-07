package org.example.p3.controller.API;

import org.example.p3.model.SeasonModel;
import org.example.p3.service.SeasonService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.p3.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/season")
public class SeasonController {

    @Autowired
    private SeasonService seasonService;

    public SeasonController(SeasonService service) {
        this.seasonService = service;
    }

    @Tag(name = "Добавление", description = "Добавляет запись")
    @PutMapping(produces = APPLICATION_JSON_VALUE)
    public HttpStatus registerSeason(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate, @RequestParam String desc, @RequestParam String img) {
        try {
            SeasonModel seasonTypeModel = new SeasonModel(0, startDate, endDate, desc, img);
            seasonService.addSeason(seasonTypeModel);
            return HttpStatus.OK;
        } catch (Exception e) {
            return HttpStatus.BAD_REQUEST;
        }
    }

    @Tag(name = "Изменение", description = "Изменяет запись")
    @PostMapping(produces = APPLICATION_JSON_VALUE)
    public HttpStatus updateSeason(@RequestParam int id, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate, @RequestParam String desc, @RequestParam String img) {
        if (seasonService.findByID(id).isEmpty()) return HttpStatus.NOT_FOUND;
        SeasonModel seasonTypeModel = seasonService.findByID(id).get(0);
        seasonTypeModel.setStart_date(startDate);
        seasonTypeModel.setEnd_date(endDate);
        seasonTypeModel.setDescription(desc);
        seasonTypeModel.setImage(img);
        seasonService.updateSeason(seasonTypeModel);
        return HttpStatus.OK;
    }

    @Tag(name = "Удаление", description = "Удаляет запись")
    @DeleteMapping(value = "/{key}", produces = APPLICATION_JSON_VALUE)
    public HttpStatus deleteSeason(@PathVariable int key) {
        try {
            seasonService.deleteSeason(key);
        }catch (Exception e){
            return HttpStatus.NOT_FOUND;
        }
        return HttpStatus.OK;
    }

    @Tag(name = "Нахождение всех страниц", description = "Находит страницы")
    @GetMapping(path = "/pages", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> getPages() {
        return ResponseEntity.ok(seasonService.getPages());
    }

    @Tag(name = "Нахождение всех", description = "Находит все записи")
    @GetMapping(path="/all", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SeasonModel>> getAll() {
        return ResponseEntity.ok(seasonService.findAll());
    }

    @Tag(name = "Нахождение страницы", description = "Находит страницу")
    @GetMapping(path="/findPage/{page}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SeasonModel>> getPage(@PathVariable int page) {
        return ResponseEntity.ok(seasonService.findPage(page));
    }

    @Tag(name = "Нахождение по ID", description = "Находит запись по ID")
    @GetMapping(value="/findID/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SeasonModel>> findById(@PathVariable int id) {
        return ResponseEntity.ok(seasonService.findByID(id));
    }
}
