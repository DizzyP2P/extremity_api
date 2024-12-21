package com.breech.extremity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@Data
public class UserRolesDTO implements Serializable {
    private static final long serialVersionUID = -421321983021901L;
    String Id;
    List<Integer> RoleId;
}
