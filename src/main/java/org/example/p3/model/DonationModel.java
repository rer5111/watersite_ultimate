package org.example.p3.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

import java.time.LocalDate;

@Entity
@Table(name= "donation")
public class DonationModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private LocalDate date;
    @ManyToOne(optional = false)
    private DonationTypeModel donationType;
    @ManyToOne(optional = false)
    private UserModel user;

    public DonationModel(int id, LocalDate date, UserModel user_ID, DonationTypeModel type_ID) {
        this.id = id;
        this.date = date;
        this.user = user_ID;
        this.donationType = type_ID;
    }

    public DonationModel() {

    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public UserModel getUser_ID() {
        return user;
    }

    public void setUser_ID(UserModel user_ID) {
        this.user = user_ID;
    }

    public DonationTypeModel getType_ID() {
        return donationType;
    }

    public void setType_ID(DonationTypeModel type_ID) {
        this.donationType = type_ID;
    }
}
