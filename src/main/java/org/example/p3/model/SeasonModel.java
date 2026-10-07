package org.example.p3.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name= "season")
public class SeasonModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private LocalDate start_date;
    private LocalDate end_date;
    @NotBlank(message = "Description is required")
    private String description;
    private String image;
    @JsonIgnore
    @ManyToMany
    @JoinTable(name="season_extra",
            joinColumns = @JoinColumn(name="season_id"),
            inverseJoinColumns = @JoinColumn(name="user_id"))
    private List<UserModel> users;

    public SeasonModel(int id, LocalDate start_date, LocalDate end_date, String description, String image) {
        this.id = id;
        this.start_date = start_date;
        this.end_date = end_date;
        this.description = description;
        this.image = image;
    }

    public SeasonModel() {

    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getStart_date() {
        return start_date;
    }

    public void setStart_date(LocalDate start_date) {
        this.start_date = start_date;
    }

    public LocalDate getEnd_date() {
        return end_date;
    }

    public void setEnd_date(LocalDate end_date) {
        this.end_date = end_date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<UserModel> getUsers() {return users;}

    public void addUser(UserModel user) {this.users.add(user);}

    public void removeUser(UserModel user) {this.users.remove(user);}
}

