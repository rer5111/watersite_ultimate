package org.example.p3.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name= "donation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DonationModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private LocalDate date;

    @ManyToOne(optional = false)
    private DonationTypeModel donationType;

    @ManyToOne(optional = false)
    private UserModel user;
}
