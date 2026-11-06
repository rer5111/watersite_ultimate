package com.waltercraft.payment.controller;

import com.waltercraft.payment.dto.PaymentCreateRequest;
import com.waltercraft.payment.dto.PaymentResponse;
import com.waltercraft.payment.dto.PaymentUpdateRequest;
import com.waltercraft.payment.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Validated
@RestController
@RequestMapping("/api/v1/admin/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public PagedModel<PaymentResponse> listPayments(
            @ParameterObject @PageableDefault(size = 25, sort = {"paymentDate", "id"}, direction = Sort.Direction.DESC)
            Pageable pageable) {
        return new PagedModel<>(paymentService.list(pageable));
    }

    @GetMapping("/{id}")
    public PaymentResponse getPayment(@PathVariable @Positive long id) {
        return paymentService.get(id);
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentCreateRequest request) {
        PaymentResponse created = paymentService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public PaymentResponse updatePayment(
            @PathVariable @Positive long id,
            @Valid @RequestBody PaymentUpdateRequest request
    ) {
        return paymentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePayment(@PathVariable @Positive long id) {
        paymentService.delete(id);
    }
}
