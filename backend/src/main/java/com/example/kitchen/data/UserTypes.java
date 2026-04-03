package com.example.kitchen.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="user_types", uniqueConstraints = {@UniqueConstraint(name = "UniquerUserAndType", columnNames = {"user", "name"})})
@NoArgsConstructor
public class UserTypes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid")
    private User user;

    @NotNull
    private String name;

    public UserTypes(User user, String name){
        this.user = user;
        this.name = name;
    }


}
