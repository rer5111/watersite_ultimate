package org.example.p3.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
@Table(name= "users")
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "Password is required")
    private String password;
    @ElementCollection(targetClass = RoleEnum.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private Set<RoleEnum> roles;
    @OneToOne(optional = false)
    @JoinColumn(name="account_id")
    private AccountModel account;
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Collection<ApplicationModel> applications;
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Collection<PaymentModel> payments;
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Collection<DonationModel> donations;
    @ManyToMany
    @JoinTable(name="season_extra",
        joinColumns = @JoinColumn(name="user_id"),
        inverseJoinColumns = @JoinColumn(name="season_id"))
    private List<SeasonModel> seasons;

    public UserModel(int id, String name, String password, Set<RoleEnum> roles, AccountModel account_ID) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.roles = roles;
        this.account = account_ID;
    }

    public UserModel() {

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<RoleEnum> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleEnum> roles) {
        this.roles = roles;
    }

    public AccountModel getAccount_ID() {
        return account;
    }

    public void setAccount_ID(AccountModel account) {
        this.account = account;
    }

    public List<SeasonModel> getSeasons(){
        return seasons;
    }

    public void addSeason(SeasonModel season){
        this.seasons.add(season);
    }

    public void removeSeason(SeasonModel season){
        this.seasons.remove(season);
    }
}
