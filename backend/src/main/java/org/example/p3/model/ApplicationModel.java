package org.example.p3.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name= "application")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotBlank(message = "Text is required")
    private String text;

    private LocalDate date;

    @NotBlank(message = "Status is required")
    private String status;

    @ManyToOne(optional = false)
    private UserModel user;
}
