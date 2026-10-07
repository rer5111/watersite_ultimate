package org.example.p3.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

import java.util.Collection;

@Entity
@Table(name= "donationtype")
public class DonationTypeModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @NotBlank(message = "Name is required")
    private String name;
    private int cost;
    @OneToMany(mappedBy = "donationType", fetch = FetchType.EAGER)
    private Collection<DonationModel> donations;


    public DonationTypeModel(int id, String name, int cost) {
        this.id = id;
        this.name = name;
        this.cost = cost;
    }

    public DonationTypeModel() {

    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }
}
