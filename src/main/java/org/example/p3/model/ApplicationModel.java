package org.example.p3.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import org.apache.catalina.User;

import java.time.LocalDate;

@Entity
@Table(name= "application")
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

    public ApplicationModel(int id, String text, LocalDate date, String status, UserModel user_ID) {
        this.id = id;
        this.text = text;
        this.date = date;
        this.status = status;
        this.user = user_ID;
    }

    public ApplicationModel() {

    }

    public long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    public UserModel getUser_ID() {
        return user;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUser_ID(UserModel user_ID) {
        this.user = user_ID;
    }
}
