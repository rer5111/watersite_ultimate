package org.example.p3.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name= "donationtype")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DonationTypeModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotBlank(message = "Name is required")
    private String name;

    private int cost;

//    @OneToMany(mappedBy = "donationType", fetch = FetchType.EAGER)
//    private Collection<DonationModel> donations;
}
