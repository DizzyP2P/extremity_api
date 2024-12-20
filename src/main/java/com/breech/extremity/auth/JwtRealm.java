package com.breech.extremity.auth;

import com.breech.extremity.dto.TokenUser;
import com.breech.extremity.model.Role;
import com.breech.extremity.model.User;
import com.breech.extremity.service.RoleService;
import com.breech.extremity.util.UserUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import javax.annotation.Resource;
import java.util.List;

public class JwtRealm extends AuthorizingRealm {
    @Resource
    private RoleService roleService;

    @Resource
    private TokenManager manager;


    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof TokenModel;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String accessToken = (String) principals.getPrimaryPrincipal();
        TokenUser tokenUser = UserUtils.getTokenUser(accessToken);

        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        User user = new User();
        user.setIdUser(tokenUser.getIdUser());
        try {
            List<Role> roles = roleService.selectRoleByUser(user);
            for (Role role : roles) {
                if (StringUtils.isNotBlank(role.getInputCode())) {
                    authorizationInfo.addRole(role.getInputCode());
                    authorizationInfo.addStringPermission(role.getInputCode());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return authorizationInfo;
    }

    /**
     * 认证回调函数, 登录时调用，主要是用来进行身份认证的，也就是说验证用户输入的账号和密码是否正确。
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authToken) throws AuthenticationException {
        TokenModel token = (TokenModel) authToken;
        if (!manager.checkToken(token)) {
            throw new UnauthorizedException();
        }
        return new SimpleAuthenticationInfo(token.getPrincipal(), token.getCredentials(), this.getName());
    }
}