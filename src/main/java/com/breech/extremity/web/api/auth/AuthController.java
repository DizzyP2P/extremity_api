package com.breech.extremity.web.api.auth;

import com.alibaba.fastjson2.JSONObject;
import com.breech.extremity.auth.TokenManager;
import com.breech.extremity.core.exception.AccountExistsException;
import com.breech.extremity.core.exception.ServiceException;
import com.breech.extremity.core.response.GlobalResult;
import com.breech.extremity.core.response.GlobalResultGenerator;
import com.breech.extremity.core.response.NormalResponseMessage;
import com.breech.extremity.dto.ForgetPasswordDTO;
import com.breech.extremity.dto.TokenUser;
import com.breech.extremity.dto.UserRegisterInfoDTO;
import com.breech.extremity.model.User;
import com.breech.extremity.service.JavaMailService;
import com.breech.extremity.service.UserService;
import com.breech.extremity.util.BeanCopierUtil;
import com.breech.extremity.util.UserUtils;
import org.apache.shiro.authc.UnknownAccountException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private JavaMailService javaMailService;
    @Resource
    private UserService userService;
    @Resource
    TokenManager tokenManager;

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
        boolean flag = userService.register(registerInfo.getEmail(), registerInfo.getPassword(), registerInfo.getCode());
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
        return GlobalResultGenerator.genSuccessResult(tokenUser);
    }

    @PostMapping("/refresh-token")
    public GlobalResult<TokenUser> refreshToken(@RequestBody TokenUser tokenUser) {
        tokenUser = userService.refreshToken(tokenUser.getRefreshToken());
        return GlobalResultGenerator.genSuccessResult(tokenUser);
    }

    @PostMapping("/logout")
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

    @GetMapping("/user")
    public GlobalResult<JSONObject> user() {
        User user = UserUtils.getCurrentUserByToken();
        TokenUser tokenUser = new TokenUser();
        BeanCopierUtil.copy(user, tokenUser);
        tokenUser.setScope(userService.findUserPermissions(user));
        JSONObject object = new JSONObject();
        object.put("user", tokenUser);
        return GlobalResultGenerator.genSuccessResult(object);
    }

}
