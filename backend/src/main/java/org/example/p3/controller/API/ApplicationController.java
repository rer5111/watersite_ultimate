package org.example.p3.controller.API;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.p3.model.ApplicationModel;
import org.example.p3.service.ApplicationService;
import org.example.p3.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/application")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final UserService userService;

    @Tag(name = "Добавление", description = "Добавляет запись")
    @PutMapping(produces = APPLICATION_JSON_VALUE)
    public HttpStatus registerApplication(
            @RequestParam String text,
            @RequestParam LocalDate date,
            @RequestParam String status,
            @RequestParam long userId
    ) {
        try {
            ApplicationModel applicationTypeModel = new ApplicationModel(
                    0L,
                    text,
                    date,
                    status,
                    userService.findById(userId).get(0)
            );
            applicationService.addApplication(applicationTypeModel);
            return HttpStatus.OK;
        } catch (Exception e) {
            return HttpStatus.BAD_REQUEST;
        }
    }

    @Tag(name = "Изменение", description = "Изменяет запись")
    @PostMapping(produces = APPLICATION_JSON_VALUE)
    public HttpStatus updateApplication(
            @RequestParam long id,
            @RequestParam String text,
            @RequestParam LocalDate date,
            @RequestParam String status,
            @RequestParam long userId
    ) {
        if (applicationService.findById(id).isEmpty() || userService.findById(userId).isEmpty()) {
            return HttpStatus.NOT_FOUND;
        }

        ApplicationModel applicationModel = applicationService.findById(id).get(0);
        applicationModel.setText(text);
        applicationModel.setDate(date);
        applicationModel.setStatus(status);
        applicationModel.setUser(userService.findById(userId).get(0));

        applicationService.updateApplication(applicationModel);

        return HttpStatus.OK;
    }

    @Tag(name = "Удаление", description = "Удаляет запись")
    @DeleteMapping(value = "/{key}", produces = APPLICATION_JSON_VALUE)
    public HttpStatus deleteDonation(@PathVariable long key) {
        try {
            applicationService.deleteApplication(key);
        } catch (Exception e) {
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
    @GetMapping(path = "/all", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ApplicationModel>> getAll() {
        return ResponseEntity.ok(applicationService.findAll());
    }

    @Tag(name = "Нахождение страницы", description = "Находит страницу")
    @GetMapping(path = "/findPage/{page}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ApplicationModel>> getPage(@PathVariable int page) {
        return ResponseEntity.ok(applicationService.findPage(page));
    }

    @Tag(name = "Нахождение по ID", description = "Находит запись по ID")
    @GetMapping(value = "/findID/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ApplicationModel>> findById(@PathVariable long id) {
        return ResponseEntity.ok(applicationService.findById(id));
    }
}