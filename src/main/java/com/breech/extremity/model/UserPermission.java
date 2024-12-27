package com.breech.extremity.model;


import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "extremity_user_permissions")
@Data
public class UserPermission {
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "JDBC")
    Long id;
    Long UserId;
    Long permissionId;
}
