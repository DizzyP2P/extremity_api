package com.breech.extremity.service.impl;

import com.breech.extremity.core.exception.AccountExistsException;
import com.breech.extremity.core.exception.CaptchaException;
import com.breech.extremity.mapper.CommentMapper;
import com.breech.extremity.mapper.RoleMapper;
import com.breech.extremity.mapper.UserMapper;
import com.breech.extremity.mapper.UserRoleMapper;
import com.breech.extremity.model.Comment;
import com.breech.extremity.model.Role;
import com.breech.extremity.model.User;
import com.breech.extremity.service.CommentService;
import com.breech.extremity.service.UserService;
import com.breech.extremity.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@Slf4j
@SpringBootTest
class UserServiceImplTest {

    @Resource
    private UserMapper userMapper;
    @Resource
    private RoleMapper roleMapper;
    @Resource
    private CommentMapper commentMapper;
    @Resource
    private CommentService commentService;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Test
    void test() {
        log.warn(String.valueOf(userRoleMapper.selectByPrimaryKey(1)));
    }
}