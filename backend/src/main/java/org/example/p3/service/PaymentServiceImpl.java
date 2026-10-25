package org.example.p3.service;

import lombok.RequiredArgsConstructor;
import org.example.p3.model.PaymentModel;
import org.example.p3.repository.PaymentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    @Override
    public List<PaymentModel> findById(long id) {
        return paymentRepository.findById(id).stream().toList();
    }

    @Override
    public List<PaymentModel> findAll() {
        return paymentRepository.findAll();
    }

    @Override
    public void addPayment(PaymentModel Payment) {
        paymentRepository.save(Payment);
    }

    @Override
    public void updatePayment(PaymentModel Payment) {
        paymentRepository.save(Payment);
    }

    @Override
    public void deletePayment(long id) {
        paymentRepository.deleteById(id);
    }

    @Override
    public List<PaymentModel> findPage(int page) {
        if (page < 1) {
            return List.of();
        }
        Pageable pageable = PageRequest.of(page - 1, 5);
        return paymentRepository.findAll(pageable).getContent();
    }

    @Override
    public int getPages() {
        Pageable pageable = PageRequest.of(0, 5);
        return paymentRepository.findAll(pageable).getTotalPages();
    }
}
