package org.example.p3.model;

import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import javax.persistence.*;

@Entity
@Table(name = "account")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "user")
public class AccountModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotBlank(message = "Username is required")
    private String username;

    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate joinDate;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    @JsonIgnore()
    private UserModel user;

    public AccountModel(int id, String username, LocalDate joinDate) {
        this.id = id;
        this.username = username;
        this.joinDate = joinDate;
    }
}
