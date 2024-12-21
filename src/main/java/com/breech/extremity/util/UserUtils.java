package com.breech.extremity.util;

import com.breech.extremity.auth.JwtConstants;
import com.breech.extremity.auth.TokenManager;
import com.breech.extremity.core.exception.UnauthenticatedException;
import com.breech.extremity.dto.TokenUser;
import com.breech.extremity.model.User;
import com.breech.extremity.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;

/**
 * @author ronger
 */
public class UserUtils {

    private static final UserMapper userMapper = SpringContextHolder.getBean(UserMapper.class);
    private static final TokenManager tokenManager = SpringContextHolder.getBean(TokenManager.class);
    public static User getCurrentUserByToken() {
        String account = (String)ContextHolderUtils.getRequest().getAttribute("account");
        if (StringUtils.isNotBlank(account)) {
            User user = userMapper.selectByAccount(account.toString());
            if (Objects.nonNull(user)) {
                return user;
            }
        }
        throw new UnauthenticatedException();
    }

    public static boolean isAdmin(String email) {
        return userMapper.hasAdminPermission(email);
    }
}
