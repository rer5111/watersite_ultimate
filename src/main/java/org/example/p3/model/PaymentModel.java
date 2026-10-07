package org.example.p3.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import org.apache.catalina.User;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Table(name= "payment")
public class PaymentModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate payment_date;
    private double cost;
    @ManyToOne(optional = false)
    private UserModel user;

    public PaymentModel(int id, LocalDate payment_date, double cost, UserModel user_ID) {
        this.id = id;
        this.payment_date = payment_date;
        this.cost = cost;
        this.user = user_ID;
    }

    public PaymentModel() {

    }

    public long getId() {
        return id;
    }

    public LocalDate getPayment_date() {
        return payment_date;
    }

    public double getCost() {
        return cost;
    }

    public UserModel getUser_ID() {
        return user;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPayment_date(LocalDate payment_date) {
        this.payment_date = payment_date;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public void setUser_ID(UserModel user_ID) {
        this.user = user_ID;
    }
}
