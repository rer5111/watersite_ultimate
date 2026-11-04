package com.waltercraft.payment.service;

import com.waltercraft.payment.dto.PaymentCreateRequest;
import com.waltercraft.payment.dto.PaymentResponse;
import com.waltercraft.payment.dto.PaymentUpdateRequest;
import com.waltercraft.payment.entity.Payment;
import com.waltercraft.payment.exception.ConflictException;
import com.waltercraft.payment.exception.ResourceNotFoundException;
import com.waltercraft.payment.repository.PaymentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Page<PaymentResponse> list(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(PaymentResponse::from);
    }

    public PaymentResponse get(long id) {
        return paymentRepository.findById(id)
                .map(PaymentResponse::from)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Transactional
    public PaymentResponse create(PaymentCreateRequest request) {
        Payment payment = new Payment(request.payerUsername().trim(), request.paymentDate(),
                request.amountRub(), cleanReference(request.externalReference()));
        return PaymentResponse.from(paymentRepository.saveAndFlush(payment));
    }

    @Transactional
    public PaymentResponse update(long id, PaymentUpdateRequest request) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(ResourceNotFoundException::new);
        if (payment.getVersion() != request.version()) {
            throw new ConflictException("Платёж уже изменён. Обновите данные и повторите запрос.");
        }
        payment.replace(request.payerUsername().trim(), request.paymentDate(),
                request.amountRub(), cleanReference(request.externalReference()));
        try {
            return PaymentResponse.from(paymentRepository.saveAndFlush(payment));
        } catch (ObjectOptimisticLockingFailureException exception) {
            throw new ConflictException("Платёж уже изменён. Обновите данные и повторите запрос.");
        }
    }

    @Transactional
    public void delete(long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(ResourceNotFoundException::new);
        paymentRepository.delete(payment);
        paymentRepository.flush();
    }

    private static String cleanReference(String reference) {
        return reference == null || reference.isBlank() ? null : reference.trim();
    }
}
