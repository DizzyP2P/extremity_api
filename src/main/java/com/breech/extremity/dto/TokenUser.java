package com.breech.extremity.dto;
import lombok.Data;
import java.util.List;
@Data
public class TokenUser {
    private Long idUser;
    private String account;
    private String nickname;
    private String token;
    private String avatarUrl;
    private String refreshToken;
    private List<Integer> scope;
}