package com.example.kitchen.data;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name="user_locations",uniqueConstraints = {@UniqueConstraint(name = "UniqueUserAndLocation", columnNames = {"user", "name"})})
public class UserLocations {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid")
    private User user;

    @NotNull
    private String name;

    public UserLocations(User user, String name){
        this.user = user;
        this.name = name;
    }

}
