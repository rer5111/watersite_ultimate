package org.example.p3.model;

import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import javax.persistence.*;

import javax.accessibility.AccessibleContext;

@Entity
@Table(name = "account")

public class AccountModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @NotBlank(message = "Username is required")
    private String username;
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate join_date;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    @JsonIgnore()
    private UserModel user;

    public AccountModel(int id, String username, LocalDate join_date) {
        this.id = id;
        this.username = username;
        this.join_date = join_date;
    }

    public AccountModel() {

    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public LocalDate getJoin_date() {
        return join_date;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setJoin_date(LocalDate join_date) {
        this.join_date = join_date;
    }

    public UserModel getUser() {return this.user;}
}
