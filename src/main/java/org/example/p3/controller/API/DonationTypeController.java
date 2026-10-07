package org.example.p3.controller.API;

import org.example.p3.model.DonationTypeModel;
import org.example.p3.service.DonationTypeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/donationType")
public class DonationTypeController {

    @Autowired
    private DonationTypeService service;

    public DonationTypeController(DonationTypeService service) {
        this.service = service;
    }

    @Tag(name = "Добавление", description = "Добавляет запись")
    @PutMapping(produces = APPLICATION_JSON_VALUE)
    public HttpStatus registerDonationType(@RequestParam String name, @RequestParam int cost) {
        try {
            DonationTypeModel donationTypeModel = new DonationTypeModel(0, name, cost);
            service.addDonationType(donationTypeModel);
            return HttpStatus.OK;
        } catch (Exception e) {
            return HttpStatus.BAD_REQUEST;
        }
    }

    @Tag(name = "Изменение", description = "Изменяет запись")
    @PostMapping(produces = APPLICATION_JSON_VALUE)
    public HttpStatus updateDonationType(@RequestParam int id, @RequestParam String name, @RequestParam int cost) {
        if (service.findByID(id).isEmpty()) return HttpStatus.NOT_FOUND;
        DonationTypeModel donationTypeModel = service.findByID(id).get(0);
        donationTypeModel.setName(name);
        donationTypeModel.setCost(cost);
        service.updateDonationType(donationTypeModel);
        return HttpStatus.OK;
    }

    @Tag(name = "Удаление", description = "Удаляет запись")
    @DeleteMapping(value = "/{key}", produces = APPLICATION_JSON_VALUE)
    public HttpStatus deleteDonationType(@PathVariable int key) {
        try {
            service.deleteDonationType(key);
        }catch (Exception e){
            return HttpStatus.NOT_FOUND;
        }
        return HttpStatus.OK;
    }

    @Tag(name = "Нахождение всех страниц", description = "Находит страницы")
    @GetMapping(path = "/pages", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> getPages() {
        return ResponseEntity.ok(service.getPages());
    }

    @Tag(name = "Нахождение всех", description = "Находит все записи")
    @GetMapping(path="/all", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DonationTypeModel>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @Tag(name = "Нахождение страницы", description = "Находит страницу")
    @GetMapping(path="/findPage/{page}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DonationTypeModel>> getPage(@PathVariable int page) {
        return ResponseEntity.ok(service.findPage(page));
    }

    @Tag(name = "Нахождение по ID", description = "Находит запись по ID")
    @GetMapping(value="/findID/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DonationTypeModel>> findById(@PathVariable int id) {
        return ResponseEntity.ok(service.findByID(id));
    }

    @Tag(name = "Нахождение по имени", description = "Находит запись по имени")
    @GetMapping(value="/findName/{name}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DonationTypeModel>> findByName(@PathVariable String name) {
        return ResponseEntity.ok(service.findByName(name));
    }

}
