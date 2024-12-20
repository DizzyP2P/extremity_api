package com.breech.extremity.auth.filter;

import com.breech.extremity.auth.TokenManager;
import com.breech.extremity.auth.annotation.Logical;
import com.breech.extremity.auth.annotation.RolesAllowed;
import com.breech.extremity.core.exception.UnauthorizedException;
import com.breech.extremity.dto.UserRolesDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.aspectj.lang.reflect.MethodSignature;


import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

@Aspect
@Slf4j
@Component
@Order(3)
public class PermissionAspect {
    @Autowired
    TokenManager tokenManager;

    @Before("@annotation(com.breech.extremity.auth.annotation.RolesAllowed)")
    public void before(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        if (request == null) {
            throw new UnauthorizedException();//抛出[权限不足]的异常
        }

        log.debug("开始校验[操作权限]");
        String token = (String)request.getAttribute("token");

        if(token==null){
            throw new UnauthorizedException();//抛出[权限不足]的异常
        }
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        RolesAllowed rolesAllowed  = methodSignature.getMethod().getAnnotation(RolesAllowed.class);
        int[] roles = rolesAllowed.value();
        log.debug("校验权限code: {}", Arrays.toString(roles));
        UserRolesDTO userRolesDTO;

        userRolesDTO = tokenManager.getRolesByToken(token);
        if(userRolesDTO==null){
            throw new UnauthorizedException();//抛出[权限不足]的异常
        }

        List<Integer> myroles = userRolesDTO.getRoleId();
        log.debug("用户已有权限: {}", myroles);

        if (rolesAllowed.logical() == Logical.AND) {
            //必须包含要求的每个权限
            for (int role: roles) {
                if (!myroles.contains(role)) {
                    log.warn("用户缺少权限 code : {}", role);
                    throw new UnauthorizedException();//抛出[权限不足]的异常
                }
            }
        } else {
            //多个权限只需包含其中一种即可
            boolean flag = false;

            for (int role: roles) {
                if (myroles.contains(role)) {
                    flag = true;
                    break;
                }
            }

            if (!flag) {
                log.warn("用户缺少权限 code= : {} (任意有一种即可)", Arrays.toString(roles));
                throw new UnauthorizedException();//抛出[权限不足]的异常
            }
        }
    }
}
