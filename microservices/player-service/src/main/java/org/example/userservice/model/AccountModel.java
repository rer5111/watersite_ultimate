package org.example.userservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "account")
public class AccountModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotBlank(message = "Username is required")
    private String username;

    @Column(nullable = true, unique = true)
    @Nullable
    private UUID uuid;

    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate joinDate;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    @JsonIgnore
    @Nullable
    private UserModel user;

    public AccountModel() {
    }

    public AccountModel(long id, String username, LocalDate joinDate) {
        this.id = id;
        this.username = username;
        this.joinDate = joinDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Nullable
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(@Nullable UUID uuid) {
        this.uuid = uuid;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    @Nullable
    public UserModel getUser() {
        return user;
    }

    public void setUser(@Nullable UserModel user) {
        this.user = user;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountModel that = (AccountModel) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AccountModel{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", joinDate=" + joinDate +
                '}';
    }
}
