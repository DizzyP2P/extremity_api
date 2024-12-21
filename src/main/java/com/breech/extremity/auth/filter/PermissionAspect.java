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

    @Before("@within(com.breech.extremity.auth.annotation.RolesAllowed) || @annotation(com.breech.extremity.auth.annotation.RolesAllowed)")
    public void before(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        if (request == null) {
            throw new UnauthorizedException(); //抛出[权限不足]的异常
        }

        log.debug("开始校验[操作权限]");
        String token = (String) request.getAttribute("token");

        if (token == null) {
            throw new UnauthorizedException(); //抛出[权限不足]的异常
        }

        // 获取类上的 RolesAllowed 注解
        RolesAllowed classRolesAllowed = joinPoint.getTarget().getClass().getAnnotation(RolesAllowed.class);

        // 获取方法上的 RolesAllowed 注解
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        RolesAllowed methodRolesAllowed = methodSignature.getMethod().getAnnotation(RolesAllowed.class);

        // 合并类和方法的权限设置
        // 如果方法上有注解，使用方法上的注解；否则使用类上的注解
        RolesAllowed rolesAllowed = methodRolesAllowed != null ? methodRolesAllowed : classRolesAllowed;

        // 获取合并后的角色数组
        int[] roles = mergeRoles(classRolesAllowed != null ? classRolesAllowed.value() : new int[0],
                methodRolesAllowed != null ? methodRolesAllowed.value() : new int[0]);
        log.debug("校验权限code: {}", Arrays.toString(roles));

        // 获取用户角色信息
        UserRolesDTO userRolesDTO = tokenManager.getRolesByToken(token);
        if (userRolesDTO == null) {
            throw new UnauthorizedException(); //抛出[权限不足]的异常
        }

        List<Integer> myRoles = userRolesDTO.getRoleId();
        log.debug("用户已有权限: {}", myRoles);

        // 判断逻辑运算符
        Logical logical = rolesAllowed != null ? rolesAllowed.logical() : Logical.AND;

        if (logical == Logical.AND) {
            // 必须包含要求的每个权限
            for (int role : roles) {
                if (!myRoles.contains(role)) {
                    log.warn("用户缺少权限 code : {}", role);
                    throw new UnauthorizedException(); //抛出[权限不足]的异常
                }
            }
        } else {
            // 多个权限只需包含其中一种即可
            boolean flag = false;
            for (int role : roles) {
                if (myRoles.contains(role)) {
                    flag = true;
                    break;
                }
            }

            if (!flag) {
                log.warn("用户缺少权限 code= : {} (任意有一种即可)", Arrays.toString(roles));
                throw new UnauthorizedException(); //抛出[权限不足]的异常
            }
        }
    }

    /**
     * 合并类级和方法级的角色数组
     * @param classRoles 类级权限
     * @param methodRoles 方法级权限
     * @return 合并后的权限数组
     */
    private int[] mergeRoles(int[] classRoles, int[] methodRoles) {
        // 合并角色数组
        int[] mergedRoles = Arrays.copyOf(classRoles, classRoles.length + methodRoles.length);
        System.arraycopy(methodRoles, 0, mergedRoles, classRoles.length, methodRoles.length);
        return mergedRoles;
    }
}