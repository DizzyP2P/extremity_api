package com.breech.extremity.web.api.auth;

import com.alibaba.fastjson2.JSONObject;
import com.breech.extremity.auth.TokenManager;
import com.breech.extremity.auth.annotation.RolesAllowed;
import com.breech.extremity.core.exception.*;
import com.breech.extremity.core.response.GlobalResult;
import com.breech.extremity.core.response.GlobalResultGenerator;
import com.breech.extremity.core.response.NormalResponseMessage;
import com.breech.extremity.dto.ForgetPasswordDTO;
import com.breech.extremity.dto.TokenUser;
import com.breech.extremity.dto.UserRegisterInfoDTO;
import com.breech.extremity.model.Role;
import com.breech.extremity.model.User;
import com.breech.extremity.model.UserPermission;
import com.breech.extremity.service.JavaMailService;
import com.breech.extremity.service.RoleService;
import com.breech.extremity.service.UserPermissionService;
import com.breech.extremity.service.UserService;
import com.breech.extremity.util.BeanCopierUtil;
import com.breech.extremity.util.UserUtils;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.mapper.entity.Condition;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private JavaMailService javaMailService;
    @Resource
    private RoleService roleService;
    @Resource
    private UserService userService;
    @Resource
    TokenManager tokenManager;
    @Resource
    UserPermissionService userPermissionService;

    @GetMapping("/get-email-code")
    public GlobalResult<Map<String, String>> getEmailCode(@RequestParam("email") String email) throws MessagingException {
        Map<String, String> map = new HashMap<>(1);
        map.put("message", NormalResponseMessage.SEND_SUCCESS.getMessage());
        User user = userService.findByAccount(email);
        if (user != null) {
            throw new AccountExistsException("该邮箱已被注册!");
        } else {
            Integer result = javaMailService.sendEmailCode(email);
            if (result == 0) {
                map.put("message", NormalResponseMessage.SEND_FAIL.getMessage());
            }
        }
        return GlobalResultGenerator.genSuccessResult(map);
    }

    @PostMapping("/register")
    public GlobalResult<Boolean> register(@RequestBody UserRegisterInfoDTO registerInfo) {
        boolean flag = userService.register(registerInfo.getEmail(), registerInfo.getPassword(), registerInfo.getCode(),registerInfo.getMessage());
        return GlobalResultGenerator.genSuccessResult(flag);
    }

    @PostMapping("/adminlogin")
    public GlobalResult<TokenUser> adminLogin(@RequestBody User user) {
        TokenUser tokenUser = userService.login(user.getAccount(), user.getPassword());
        return GlobalResultGenerator.genSuccessResult(tokenUser);
    }

    @PostMapping("/login")
    public GlobalResult<TokenUser> login(@RequestBody User user) {
        TokenUser tokenUser = userService.login(user.getAccount(), user.getPassword());
        Integer roleId = roleService.getRoleIdByAccount(user.getAccount());
        // return GlobalResultGenerator.genSuccessResult(tokenUser);
        // message充当第二data
        return GlobalResultGenerator.genResult(true, tokenUser, roleId.toString());
    }

    @PostMapping("/refresh-token")
    public GlobalResult<TokenUser> refreshToken(@RequestBody TokenUser tokenUser) {
        tokenUser = userService.refreshToken(tokenUser.getRefreshToken());
        return GlobalResultGenerator.genSuccessResult(tokenUser);
    }

    @PostMapping("/logout")
    @RolesAllowed({5})
    public GlobalResult logout() {
        User user = UserUtils.getCurrentUserByToken();
        if (Objects.nonNull(user)) {
            tokenManager.deleteToken(user.getAccount());
        }
        return GlobalResultGenerator.genSuccessResult();
    }

    @PatchMapping("/forget-password")
    public GlobalResult<Boolean> forgetPassword(@RequestBody ForgetPasswordDTO forgetPassword) throws ServiceException {
        boolean flag = userService.forgetPassword(forgetPassword.getCode(), forgetPassword.getPassword());
        return GlobalResultGenerator.genSuccessResult(flag);
    }

    @GetMapping("/get-forget-password-email")
    public GlobalResult getForgetPasswordEmail(@RequestParam("email") String email) throws MessagingException, ServiceException {
        User user = userService.findByAccount(email);
        if (user != null) {
            Integer result = javaMailService.sendForgetPasswordEmail(email);
            if (result == 0) {
                throw new ServiceException(NormalResponseMessage.SEND_FAIL.getMessage());
            }
        } else {
            throw new UnknownAccountException("未知账号");
        }
        return GlobalResultGenerator.genSuccessResult(NormalResponseMessage.SEND_SUCCESS.getMessage());
    }

    @GetMapping("/getPermissions")
    public GlobalResult getPermissions(){
        User user = UserUtils.getCurrentUserByToken();
        Condition condition  = new Condition(UserPermission.class);
        condition.createCriteria().andEqualTo("UserId",user.getIdUser());
        List<UserPermission> permissions = userPermissionService.findByCondition(condition);
        List<Long> permissionIds = permissions.stream().map(UserPermission::getPermissionId).collect(Collectors.toList());
        return GlobalResultGenerator.genSuccessResult(permissionIds);
    }


    @GetMapping("/user")
    public GlobalResult<JSONObject> user() {
        try{
            User user = UserUtils.getCurrentUserByToken();
            TokenUser tokenUser = new TokenUser();
            BeanCopierUtil.copy(user, tokenUser);
            List<Role> res = userService.findRolesByUserId(user.getIdUser());
            tokenUser.setScope(res.stream().map(Role::getIdRole).collect(Collectors.toList()));
            JSONObject object = new JSONObject();
            object.put("user", tokenUser);
            return GlobalResultGenerator.genSuccessResult(object);
        }catch (UnauthorizedException e){
            return GlobalResultGenerator.genErrorResult("当前用户未登陆");
        }
    }

}
