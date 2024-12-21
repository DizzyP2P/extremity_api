package com.breech.extremity.service.impl;

import com.breech.extremity.core.exception.AccountExistsException;
import com.breech.extremity.core.exception.CaptchaException;
import com.breech.extremity.mapper.RoleMapper;
import com.breech.extremity.mapper.UserMapper;
import com.breech.extremity.model.Role;
import com.breech.extremity.model.User;
import com.breech.extremity.service.UserService;
import com.breech.extremity.util.Utils;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class UserServiceImplTest {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Test
    void createUser() {
       String email = "admin@163.com";
       User user = new User();
       String nickname = email.split("@")[0];
       String password = "123";
       user.setNickname(nickname);
       user.setAccount(nickname);
       user.setEmail(email);
       user.setPassword(Utils.entryptPassword(password));
       user.setCreatedTime(new Date());
       user.setUpdatedTime(user.getCreatedTime());
       user.setAvatarUrl("");
       userMapper.insertSelective(user);
       user = userMapper.selectByAccount(email);
       Role role = roleMapper.selectRoleByInputCode("topop");
       userMapper.insertUserRole(user.getIdUser(), role.getIdRole());
    }
}