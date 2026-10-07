package org.example.p3.controller.API;

import org.example.p3.model.DonationModel;
import org.example.p3.service.DonationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.p3.service.DonationTypeService;
import org.example.p3.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/donation")
public class DonationController {

    @Autowired
    private DonationService donationService;
    @Autowired
    private UserService userService;
    @Autowired
    private DonationTypeService donationTypeService;

    public DonationController(DonationService service) {
        this.donationService = service;
    }

    @Tag(name = "Добавление", description = "Добавляет запись")
    @PutMapping(produces = APPLICATION_JSON_VALUE)
    public HttpStatus registerDonation(@RequestParam LocalDate date, @RequestParam int typeID, @RequestParam int userID) {
        try {
            DonationModel donationTypeModel = new DonationModel(0, date, userService.findByID(userID).get(0), donationTypeService.findByID(typeID).get(0));
            donationService.addDonation(donationTypeModel);
            return HttpStatus.OK;
        } catch (Exception e) {
            return HttpStatus.BAD_REQUEST;
        }
    }

    @Tag(name = "Изменение", description = "Изменяет запись")
    @PostMapping(produces = APPLICATION_JSON_VALUE)
    public HttpStatus updateDonation(@RequestParam int id, @RequestParam LocalDate date, @RequestParam int typeID, @RequestParam int userID) {
        if (donationService.findByID(id).isEmpty() || userService.findByID(userID).isEmpty()) return HttpStatus.NOT_FOUND;
        DonationModel donationModel = donationService.findByID(id).get(0);
        donationModel.setDate(date);
        donationModel.setType_ID(donationTypeService.findByID(typeID).get(0));
        donationModel.setUser_ID(userService.findByID(userID).get(0));
        donationService.updateDonation(donationModel);
        return HttpStatus.OK;
    }

    @Tag(name = "Удаление", description = "Удаляет запись")
    @DeleteMapping(value = "/{key}", produces = APPLICATION_JSON_VALUE)
    public HttpStatus deleteDonation(@PathVariable int key) {
        try {
            donationService.deleteDonation(key);
        }catch (Exception e){
            return HttpStatus.NOT_FOUND;
        }
        return HttpStatus.OK;
    }

    @Tag(name = "Нахождение всех страниц", description = "Находит страницы")
    @GetMapping(path = "/pages", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> getPages() {
        return ResponseEntity.ok(donationService.getPages());
    }

    @Tag(name = "Нахождение всех", description = "Находит все записи")
    @GetMapping(path="/all", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DonationModel>> getAll() {
        return ResponseEntity.ok(donationService.findAll());
    }

    @Tag(name = "Нахождение страницы", description = "Находит страницу")
    @GetMapping(path="/findPage/{page}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DonationModel>> getPage(@PathVariable int page) {
        return ResponseEntity.ok(donationService.findPage(page));
    }

    @Tag(name = "Нахождение по ID", description = "Находит запись по ID")
    @GetMapping(value="/findID/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DonationModel>> findById(@PathVariable int id) {
        return ResponseEntity.ok(donationService.findByID(id));
    }
}
