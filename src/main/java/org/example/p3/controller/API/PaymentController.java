package org.example.p3.controller.API;

import org.example.p3.model.PaymentModel;
import org.example.p3.service.PaymentService;
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
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private UserService userService;

    public PaymentController(PaymentService service) {
        this.paymentService = service;
    }

    @Tag(name = "Добавление", description = "Добавляет запись")
    @PutMapping(produces = APPLICATION_JSON_VALUE)
    public HttpStatus registerPayment(@RequestParam LocalDate date, @RequestParam Double cost, @RequestParam int userID) {
        try {
            PaymentModel paymentTypeModel = new PaymentModel(0, date, cost, userService.findByID(userID).get(0));
            paymentService.addPayment(paymentTypeModel);
            return HttpStatus.OK;
        } catch (Exception e) {
            System.out.println(e);
            return HttpStatus.BAD_REQUEST;
        }
    }

    @Tag(name = "Изменение", description = "Изменяет запись")
    @PostMapping(produces = APPLICATION_JSON_VALUE)
    public HttpStatus updatePayment(@RequestParam int id, @RequestParam LocalDate date, @RequestParam Double cost, @RequestParam int userID) {
        if (paymentService.findByID(id).isEmpty() || userService.findByID(userID).isEmpty()) return HttpStatus.NOT_FOUND;
        PaymentModel paymentTypeModel = paymentService.findByID(id).get(0);
        paymentTypeModel.setPayment_date(date);
        paymentTypeModel.setCost(cost);
        paymentTypeModel.setUser_ID(userService.findByID(userID).get(0));
        paymentService.updatePayment(paymentTypeModel);
        return HttpStatus.OK;
    }

    @Tag(name = "Удаление", description = "Удаляет запись")
    @DeleteMapping(value = "/{key}", produces = APPLICATION_JSON_VALUE)
    public HttpStatus deletePayment(@PathVariable int key) {
        try {
            paymentService.deletePayment(key);
        }catch (Exception e){
            return HttpStatus.NOT_FOUND;
        }
        return HttpStatus.OK;
    }

    @Tag(name = "Нахождение всех страниц", description = "Находит страницы")
    @GetMapping(path = "/pages", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Integer> getPages() {
        return ResponseEntity.ok(paymentService.getPages());
    }

    @Tag(name = "Нахождение всех", description = "Находит все записи")
    @GetMapping(path="/all", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PaymentModel>> getAll() {
        return ResponseEntity.ok(paymentService.findAll());
    }

    @Tag(name = "Нахождение страницы", description = "Находит страницу")
    @GetMapping(path="/findPage/{page}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PaymentModel>> getPage(@PathVariable int page) {
        return ResponseEntity.ok(paymentService.findPage(page));
    }

    @Tag(name = "Нахождение по ID", description = "Находит запись по ID")
    @GetMapping(value="/findID/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PaymentModel>> findById(@PathVariable int id) {
        return ResponseEntity.ok(paymentService.findByID(id));
    }
}
