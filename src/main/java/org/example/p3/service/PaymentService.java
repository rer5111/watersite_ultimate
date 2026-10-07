package org.example.p3.service;

import org.example.p3.model.PaymentModel;

import java.util.List;

public interface PaymentService {
    public List<PaymentModel> findByID(long id);
    public List<PaymentModel> findAll();
    public PaymentModel addPayment(PaymentModel Payment);
    public PaymentModel updatePayment(PaymentModel Payment);
    public void deletePayment(long id);
    public List<PaymentModel> findPage(int page);
    public int getPages();
}
