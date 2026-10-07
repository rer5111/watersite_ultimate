package org.example.p3.controller.API;

import org.example.p3.model.ApplicationModel;
import org.example.p3.service.ApplicationService;
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
@RequestMapping("/api/application")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private UserService userService;

    public ApplicationController(ApplicationService service) {
        this.applicationService = service;
    }

    @Tag(name = "Добавление", description = "Добавляет запись")
    @PutMapping(produces = APPLICATION_JSON_VALUE)
    public HttpStatus registerApplication(@RequestParam String text, @RequestParam LocalDate date, @RequestParam String status, @RequestParam int userID) {
        try {
            ApplicationModel applicationTypeModel = new ApplicationModel(0, text, date, status, userService.findByID(userID).get(0));
            applicationService.addApplication(applicationTypeModel);
            return HttpStatus.OK;
        } catch (Exception e) {
            return HttpStatus.BAD_REQUEST;
        }
    }

    @Tag(name = "Изменение", description = "Изменяет запись")
    @PostMapping(produces = APPLICATION_JSON_VALUE)
    public HttpStatus updateApplication(@RequestParam int id, @RequestParam String text, @RequestParam LocalDate date, @RequestParam String status, @RequestParam int userID) {
        if (applicationService.findByID(id).isEmpty() || userService.findByID(userID).isEmpty()) return HttpStatus.NOT_FOUND;
        ApplicationModel applicationTypeModel = applicationService.findByID(id).get(0);
        applicationTypeModel.setText(text);
        applicationTypeModel.setDate(date);
        applicationTypeModel.setStatus(status);
        applicationTypeModel.setUser_ID(userService.findByID(userID).get(0));
        applicationService.updateApplication(applicationTypeModel);
        return HttpStatus.OK;
    }

    @Tag(name = "Удаление", description = "Удаляет запись")
    @DeleteMapping(value = "/{key}", produces = APPLICATION_JSON_VALUE)
    public HttpStatus deleteDonation(@PathVariable int key) {
        try {
            applicationService.deleteApplication(key);
        }catch (Exception e){
            return HttpStatus.NOT_FOUND;
        }
        return HttpStatus.OK;
    }

    @Tag(name = "Нахождение всех страниц", description = "Находит страницы")
    @GetMapping(path = "/pages", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> getPages() {
        return ResponseEntity.ok(applicationService.getPages());
    }

    @Tag(name = "Нахождение всех", description = "Находит все записи")
    @GetMapping(path="/all", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ApplicationModel>> getAll() {
        return ResponseEntity.ok(applicationService.findAll());
    }

    @Tag(name = "Нахождение страницы", description = "Находит страницу")
    @GetMapping(path="/findPage/{page}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ApplicationModel>> getPage(@PathVariable int page) {
        return ResponseEntity.ok(applicationService.findPage(page));
    }

    @Tag(name = "Нахождение по ID", description = "Находит запись по ID")
    @GetMapping(value="/findID/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ApplicationModel>> findById(@PathVariable int id) {
        return ResponseEntity.ok(applicationService.findByID(id));
    }
}
