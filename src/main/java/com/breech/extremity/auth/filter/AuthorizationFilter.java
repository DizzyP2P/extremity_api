package com.breech.extremity.auth.filter;

import com.breech.extremity.auth.JwtConstants;
import com.breech.extremity.auth.TokenManager;
import com.breech.extremity.core.exception.UnauthenticatedException;
import com.breech.extremity.dto.UserInfoDTO;
import com.breech.extremity.dto.UserRolesDTO;
import com.breech.extremity.mapper.RoleMapper;
import com.breech.extremity.mapper.UserMapper;
import com.breech.extremity.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component

public class AuthorizationFilter extends OncePerRequestFilter {

    @Resource
    TokenManager tokenManager;
    @Resource
    UserMapper userMapper;
    @Resource
    RoleMapper roleMapper;
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain)
            throws ServletException, IOException {
        // 从请求头中提取 Token
        String token = request.getHeader("Authorization");

        logger.info("处理token {}" + token);

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // 去掉 "Bearer " 前缀
            if(tokenManager.ifExistToken(token)) {
                request.setAttribute("token", token);
                request.setAttribute("account", tokenManager.getRolesByToken(token).getId());
            }else{
                try {
                    Claims claims;
                    try {
                        claims = Jwts.parser().setSigningKey(JwtConstants.JWT_SECRET).parseClaimsJws(token).getBody();
                    } catch (final SignatureException e) {
                        throw new UnauthenticatedException();
                    }
                    String account = claims.getSubject();
                    Date expiration = claims.getExpiration();
                    if (account != null && expiration != null) {
                        UserInfoDTO existence = userMapper.findUserInfoByAccount(account);
                        Instant now = Instant.now();  // 获取当前时间
                        Instant expirationInstant = expiration.toInstant();  // 将 Date 转换为 Instant
                        Duration duration = Duration.between(now, expirationInstant);
                        long minitus = duration.toMinutes();  // 获取差值的分钟数
                        if (existence != null) {
                            List<Role> roles = roleMapper.selectRoleByIdUser(existence.getIdUser());
                            List<Integer> roleslist = roles.stream().map(Role::getIdRole).collect(Collectors.toList());
                            UserRolesDTO data = new UserRolesDTO(account, roleslist);
                            tokenManager.createToken(token, data, minitus);
                            request.setAttribute("token", token);
                            request.setAttribute("account", account);
                        }
                    }else{
                        throw new UnauthenticatedException();
                    }
                }
                catch (Exception e) {
                    throw new UnauthenticatedException();
                }
            }
        }
        // 继续处理下一个 Filter 或 Controller
        logger.info("token处理正常");
        filterChain.doFilter(request, response);
    }
}