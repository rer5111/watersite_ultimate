package org.example.p3.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "season")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "users")
public class SeasonModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotBlank(message =  "Title is required")
    private String title;

    private LocalDate startDate;

    private LocalDate endDate;

    @NotBlank(message = "Description is required")
    private String description;

    private String image;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "season_extra",
            joinColumns = @JoinColumn(name = "season_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<UserModel> users = new ArrayList<>();

    public SeasonModel(long id, String title, LocalDate startDate, LocalDate endDate, String description, String image) {
        this.id = id;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.image = image;
    }

    public void addUser(UserModel user) {
        this.users.add(user);
    }

    public void removeUser(UserModel user) {
        this.users.remove(user);
    }
}