package com.breech.extremity.dto.admin;


import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author ronger
 */
@Data
public class UserRoleDTO {

    private Long idUser;
    private Integer idRole;
    private LocalDateTime createdTime;
    private Integer activated;
    private String message;

    private String realName;
    private String email;
    private String nickName;
    private String avatarUrl;
    private String roleName;
}