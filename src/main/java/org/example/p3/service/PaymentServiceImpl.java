package org.example.p3.service;

import org.example.p3.model.PaymentModel;
import org.example.p3.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository PaymentRepository;

    public PaymentServiceImpl(PaymentRepository PaymentRepository) {
        this.PaymentRepository = PaymentRepository;
    }


    @Override
    public List<PaymentModel> findByID(long id) {
        return PaymentRepository.findById(id).stream().toList();
    }

    @Override
    public List<PaymentModel> findAll() {
        return PaymentRepository.findAll();
    }

    @Override
    public PaymentModel addPayment(PaymentModel Payment) {
        return PaymentRepository.save(Payment);
    }

    @Override
    public PaymentModel updatePayment(PaymentModel Payment) {
        return PaymentRepository.save(Payment);
    }

    @Override
    public void deletePayment(long id) {
        PaymentRepository.deleteById(id);
    }
    @Override
    public List<PaymentModel> findPage(int page){
        List<PaymentModel> Payments = PaymentRepository.findAll();
        if (Payments.isEmpty()){
            return new ArrayList<>();
        } else if (Payments.size() <page*5-5 || page < 1) {
            return new ArrayList<>();
        }
        if (page*5 < Payments.size()){
            return Payments.subList(page*5-5, page*5);
        }
        else{
            return Payments.subList(page*5-5, Payments.size());
        }
    }
    @Override
    public int getPages(){
        return (PaymentRepository.findAll().size()-1)/5+1;
    }
}
