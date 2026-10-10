package org.example.p3.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"account", "applications", "payments", "donations", "seasons"})
public class UserModel {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Password is required")
    @ToString.Exclude
    private String password;

    @ElementCollection(targetClass = RoleEnum.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private Set<RoleEnum> roles;

    @OneToOne(optional = false)
    @JoinColumn(name = "account_id")
    private AccountModel account;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Collection<ApplicationModel> applications;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Collection<PaymentModel> payments;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Collection<DonationModel> donations;

    @ManyToMany
    @JoinTable(
            name = "season_extra",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "season_id")
    )
    private List<SeasonModel> seasons = new ArrayList<>();

    public UserModel(long id, String name, String password, Set<RoleEnum> roles, AccountModel account) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.roles = roles;
        this.account = account;
    }

    public void addSeason(SeasonModel season) {
        this.seasons.add(season);
    }

    public void removeSeason(SeasonModel season) {
        this.seasons.remove(season);
    }
}