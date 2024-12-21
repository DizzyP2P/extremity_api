package com.breech.extremity.auth;

import com.breech.extremity.dto.UserRolesDTO;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
public interface TokenManager {
    public String createToken(UserRolesDTO user) ;
    public String createToken(String account);
    public String createToken(String token,UserRolesDTO user,long expirationTimeInMinutes) throws JsonProcessingException;
    public String getToken(String account);
    public Boolean ifExistToken(String token);
    public void deleteToken(String account);
    public UserRolesDTO getRoles(String account);
    public UserRolesDTO getRolesByToken(String token);
}