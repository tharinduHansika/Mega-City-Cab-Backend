package com.megacitycab.mega_city_cab.entities;


import javax.persistence.*;

@Entity
@Table(name = "user_has_role")
public class UserHasRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name="role_id")
    private int roleId;

    @Column(name="user_id")
    private int userId;
}
