package org.example.p3.service;

import org.example.p3.model.PaymentModel;

import java.util.List;

public interface PaymentService {
    List<PaymentModel> findById(long id);
    List<PaymentModel> findAll();
    void addPayment(PaymentModel Payment);
    void updatePayment(PaymentModel Payment);
    void deletePayment(long id);
    List<PaymentModel> findPage(int page);
    int getPages();
}
