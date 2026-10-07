package org.example.p3.repository;

import org.example.p3.model.PaymentModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentModel, Long> {
    List<PaymentModel> findAllById(int id);
}
