package com.breech.extremity.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name = "extremity_user_role")
public class UserRole {
    @Id
    @GeneratedValue(generator = "JDBC")
    @Column(name = "id_user")
    private int idUser;
    @Column(name = "id_role")
    private int idRole;
    @Column(name = "activated")
    private int activated;
    @Column(name = "message")
    private String message;
}
