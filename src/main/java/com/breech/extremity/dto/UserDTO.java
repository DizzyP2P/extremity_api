package com.breech.extremity.dto;


import lombok.Data;
// no use
@Data
public class UserDTO {

    private Long idUser;

    private String account;

    private String avatarType;

    private String avatarUrl;

    private String nickname;

    private String signature;

    private String bgImgUrl;
}
