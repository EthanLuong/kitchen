package com.example.kitchen.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "item_defaults",uniqueConstraints = {@UniqueConstraint(name = "UniqueUserAndItem", columnNames = {"user", "name"})})
public class ItemDefaults {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid")
    private User user;

    @NotNull
    private String name;

    private String foodType;

    private String unit;

    private String location;

    private Integer expirationDays;
}
