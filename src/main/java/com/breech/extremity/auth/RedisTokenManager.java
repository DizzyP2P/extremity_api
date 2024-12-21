package com.breech.extremity.auth;

import com.breech.extremity.core.exception.BusinessException;
import com.breech.extremity.core.exception.UnauthorizedException;
import com.breech.extremity.core.service.redis.RedisService;
import com.breech.extremity.dto.UserRolesDTO;
import com.breech.extremity.handler.event.AccountEvent;
import com.breech.extremity.mapper.RoleMapper;
import com.breech.extremity.model.Role;
import com.breech.extremity.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class RedisTokenManager implements TokenManager {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper; // 用于 JSON 序列化/反序列化


    @Override
    public String createToken(UserRolesDTO user){
        // 生成 JWT Token

        Date now = new Date();

        Date expiration = new Date(now.getTime() +JwtConstants.TOKEN_EXPIRES_MINUTE * 60 * 1000); // 转换为毫秒

        String token = Jwts.builder()
                .setId(user.getId())
                .setSubject(user.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date())
                .signWith(SignatureAlgorithm.HS256, JwtConstants.JWT_SECRET)
                .compact();

        String json;
        try{
            json = objectMapper.writeValueAsString(user);
        }
        catch (JsonProcessingException e){
            throw new BusinessException(e.getMessage());
        }

        redisTemplate.opsForValue().set("token:"+ token, json,JwtConstants.TOKEN_EXPIRES_MINUTE, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set("account:"+ user.getId(), token,JwtConstants.TOKEN_EXPIRES_MINUTE, TimeUnit.MINUTES);
        return token;
    }

    @Override
    public String createToken(String account){
        UserRolesDTO res = getRoles(account);
        if(res!=null) {
            deleteToken(account);
            return createToken(res);
        }
        return null;
    }

    @Override
    public String createToken(String token, UserRolesDTO user,long expirationTimeInMinutes) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(user);
        redisTemplate.opsForValue().set("token:"+ token, json,expirationTimeInMinutes, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set("account:"+ user.getId(), token,expirationTimeInMinutes, TimeUnit.MINUTES);
        return token;
    }

    @Override
    public String getToken(String account) {
        return redisTemplate.opsForValue().get("account:"+ account);
    }

    @Override
    public Boolean ifExistToken(String token) {
        return redisTemplate.hasKey("token:"+token);
    }

    @Override
    public UserRolesDTO getRoles(String account){
        String token = redisTemplate.opsForValue().get("account:"+ account);
        if (token == null) {
            return null;
        }
        String json = redisTemplate.opsForValue().get("token:"+ token);
        try {
           return objectMapper.readValue(json, UserRolesDTO.class);
        }catch (JsonProcessingException e){
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public UserRolesDTO getRolesByToken(String token){
        String json = redisTemplate.opsForValue().get("token:"+ token);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, UserRolesDTO.class);
        }catch (JsonProcessingException e){
            throw new BusinessException(e.getMessage());
        }
    }

    @Override
    public void deleteToken(String account) {
        String token = redisTemplate.opsForValue().get("account:"+ account);
        redisTemplate.delete("token:" + token);
        redisTemplate.delete("account:" + account);
    }

}
