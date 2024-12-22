package com.breech.extremity.service.impl;

import com.breech.extremity.auth.TokenManager;
import com.breech.extremity.util.Utils;
import com.github.f4b6a3.ulid.UlidCreator;
import com.breech.extremity.auth.JwtConstants;
import com.breech.extremity.core.exception.*;
import com.breech.extremity.core.service.AbstractService;
import com.breech.extremity.dto.*;
import com.breech.extremity.model.Role;
import com.breech.extremity.model.User;
import com.breech.extremity.mapper.RoleMapper;
import com.breech.extremity.mapper.UserMapper;
import com.breech.extremity.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class UserServiceImpl extends AbstractService<User> implements UserService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private RoleMapper roleMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Resource
    private TokenManager tokenManager;
//    @Resource
//    private LoginRecordService loginRecordService;

    private final static String DEFAULT_AVATAR = "https://tse4-mm.cn.bing.net/th/id/OIP-C._r7lZRECo5odDY5N9ufpQwHaNG?w=724&h=1280&rs=1&pid=ImgDetMain";

    @Override
    public List<Role> findRolesByUserId(Long userId) {
        return roleMapper.selectRoleByIdUser(userId);
    }

    @Override
    public UserRolesDTO findRolesByAccount(String account) {
        UserInfoDTO existence = userMapper.findUserInfoByAccount(account);

        if (existence != null) {
            List<Role> roles = roleMapper.selectRoleByIdUser(existence.getIdUser());
            List<Integer> roleslist = roles.stream().map(Role::getIdRole).collect(java.util.stream.Collectors.toList());
            return new UserRolesDTO(account, roleslist);
        }
        return null;
    }

    @Override
    public User findByAccount(String account) throws TooManyResultsException {
        return userMapper.selectByAccount(account);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean register(String email, String password, String code) {
        String vCode = redisTemplate.boundValueOps(email).get();
        if (StringUtils.isNotBlank(vCode)) {
            if (vCode.equals(code)) {
                User user = userMapper.selectByAccount(email);
                if (user != null) {
                    throw new AccountExistsException("该邮箱已被注册！");
                } else {
                    user = new User();
                    String nickname = email.split("@")[0];
                    user.setNickname(checkNickname(nickname));
                    user.setAccount(checkAccount(nickname));
                    user.setEmail(email);
                    user.setPassword(Utils.entryptPassword(password));
                    user.setCreatedTime(new Date());
                    user.setUpdatedTime(user.getCreatedTime());
                    user.setAvatarUrl(DEFAULT_AVATAR);
                    userMapper.insertSelective(user);
                    user = userMapper.selectByAccount(email);
                    Role role = roleMapper.selectRoleByInputCode("unauthorized_user");
                    userMapper.insertUserRole(user.getIdUser(), role.getIdRole());
                    redisTemplate.delete(email);
                    return true;
                }
            }
        }
        throw new CaptchaException();
    }

    private String checkNickname(String nickname) {
        nickname = formatNickname(nickname);
        Integer result = userMapper.selectCountByNickName(nickname);
        if (result > 0) {
            StringBuilder stringBuilder = new StringBuilder(nickname);
            return checkNickname(stringBuilder.append("_").append(System.currentTimeMillis()).toString());
        }
        return nickname;
    }

    private String checkAccount(String account) {
        account = formatNickname(account);
        Integer result = userMapper.selectCountByAccount(account);
        if (result > 0) {
            StringBuilder stringBuilder = new StringBuilder(account);
            return checkNickname(stringBuilder.append("_").append(System.currentTimeMillis()).toString());
        }
        return account;
    }

    @Override
    public TokenUser login(String account, String password) {
        User user = userMapper.selectByAccount(account);
        log.warn(account+password);
        if (user != null) {
            if (Utils.comparePwd(password, user.getPassword())) {
                redisTemplate.delete("Refresh_Token:"+account);
                tokenManager.deleteToken("token:"+account);

                userMapper.updateLastLoginTime(user.getIdUser());
                userMapper.updateLastOnlineTimeByAccount(user.getAccount());
                TokenUser tokenUser = new TokenUser();

                String token = tokenManager.createToken(user.getAccount());
                if(token==null){
                    UserRolesDTO roles_res = this.findRolesByAccount(user.getAccount());
                    token = tokenManager.createToken(roles_res);
                }
                tokenUser.setToken(token);

                tokenUser.setRefreshToken(UlidCreator.getUlid().toString());
                redisTemplate.boundValueOps(tokenUser.getRefreshToken()).set("Refresh_Token:"+ account, JwtConstants.REFRESH_TOKEN_EXPIRES_HOUR, TimeUnit.HOURS);
//                loginRecordService.saveLoginRecord(user.getIdUser());
                return tokenUser;
            }
        }
        throw new AccountErrorException();
    }

    @Override
    public TokenUser adminLogin(String account, String password) {
        User user = userMapper.selectByAccount(account);
        if (user != null) {
            List<Role> roles = roleMapper.selectRoleByIdUser(user.getIdUser());
            boolean isTopop = false;
            for(Role item:roles){
                if(item.getInputCode().equals("topop")){
                    isTopop = true;
                    break;
                }
            }
            if (isTopop&&Utils.comparePwd(password, user.getPassword())) {
                redisTemplate.delete("Refresh_Token:"+account);
                tokenManager.deleteToken("token:"+account);

                userMapper.updateLastLoginTime(user.getIdUser());
                userMapper.updateLastOnlineTimeByAccount(user.getAccount());
                TokenUser tokenUser = new TokenUser();

                String token = tokenManager.createToken(user.getAccount());
                if(token==null){
                    UserRolesDTO roles_res = this.findRolesByAccount(user.getAccount());
                    token = tokenManager.createToken(roles_res);
                }
                tokenUser.setToken(token);

                tokenUser.setRefreshToken(UlidCreator.getUlid().toString());
                redisTemplate.boundValueOps(tokenUser.getRefreshToken()).set("Refresh_Token:"+account, JwtConstants.REFRESH_TOKEN_EXPIRES_HOUR, TimeUnit.HOURS);
                return tokenUser;
            }
        }
        throw new AuthenticationException();
    }

    @Override
    public UserDTO findUserDTOByAccount(String account) {
        return userMapper.selectUserDTOByAccount(account);
    }

    @Override
    public boolean forgetPassword(String code, String password) throws ServiceException {
        String email = redisTemplate.boundValueOps(code).get();
        if (StringUtils.isBlank(email)) {
            throw new ServiceException("链接已失效");
        } else {
            int result = userMapper.updatePasswordByEmail(email, Utils.entryptPassword(password));
            if (result == 0) {
                throw new ServiceException("密码修改失败!");
            }
            return true;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserRole(Long idUser, Long idRole) throws ServiceException {
        Integer result = userMapper.updateUserRole(idUser, idRole);
        if (result == 0) {
            throw new ServiceException("更新失败!");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long idUser, String status) throws ServiceException {
        Integer result = userMapper.updateStatus(idUser, status);
        if (result == 0) {
            throw new ServiceException("更新失败!");
        }
        return true;
    }

    @Override
    public UserInfoDTO findUserInfo(Long idUser) {
        UserInfoDTO user = userMapper.selectUserInfo(idUser);
        if (user == null) {
            throw new ContentNotExistException("用户不存在!");
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoDTO updateUserInfo(UserInfoDTO user) throws ServiceException {
        String nickname = formatNickname(user.getNickname());
        boolean flag = checkNicknameByIdUser(user.getIdUser(), nickname);
        if (!flag) {
            throw new NicknameOccupyException("该昵称已使用!");
        }
        user.setNickname(nickname);
//        if (FileDataType.BASE64.equals(user.getAvatarType())) {
//            String avatarUrl = UploadController.uploadBase64File(user.getAvatarUrl(), FilePath.AVATAR);
//            user.setAvatarUrl(avatarUrl);
//            user.setAvatarType("0");
//        }
//        Integer result = userMapper.updateUserInfo(user.getIdUser(), user.getNickname(), user.getAvatarType(), user.getAvatarUrl(), user.getSignature(), user.getSex());
//        UserIndexUtil.addIndex(UserLucene.builder()
//                .idUser(user.getIdUser())
//                .nickname(user.getNickname())
//                .signature(user.getSignature())
//                .build());
//        if (result == 0) {
//            throw new ServiceException("操作失败!");
//        }

        return user;
    }

    private String formatNickname(String nickname) {
        return nickname.replaceAll("\\.", "");
    }

    public boolean checkNicknameByIdUser(Long idUser, String nickname) {
        nickname = formatNickname(nickname);
        if (StringUtils.isBlank(nickname)) {
            throw new IllegalArgumentException("昵称不能为空!");
        }
        Integer number = userMapper.checkNicknameByIdUser(idUser, nickname);
        return number <= 0;
    }

    @Override
    public Integer findRoleWeightsByUser(Long idUser) {
        return userMapper.selectRoleWeightsByUser(idUser);
    }

    @Override
    public Author selectAuthor(Long idUser) {
        return userMapper.selectAuthor(idUser);
    }


    @Override
    public List<UserInfoDTO> findUsers(UserSearchDTO searchDTO) {
        List<UserInfoDTO> users = userMapper.selectUsers(searchDTO);
        users.forEach(user -> {
            user.setOnlineStatus(getUserOnlineStatus(user.getAccount()));
        });
        return users;
    }

    private Integer getUserOnlineStatus(String account) {
        String lastOnlineTime = redisTemplate.boundValueOps(JwtConstants.LAST_ONLINE + account).get();
        if (StringUtils.isBlank(lastOnlineTime)) {
            return 0;
        }
        return 1;
    }

    @Override
    public Integer updateLastOnlineTimeByAccount(String account) {
        return userMapper.updateLastOnlineTimeByAccount(account);
    }


    @Override
    public TokenUser refreshToken(String refreshToken) {
        String account = redisTemplate.boundValueOps(refreshToken).get();
        if (StringUtils.isNotBlank(account)) {
            User nucleicUser = userMapper.selectByAccount(account);
            if (nucleicUser != null) {
                TokenUser tokenUser = new TokenUser();
                tokenUser.setToken(tokenManager.createToken(nucleicUser.getAccount()));
                tokenUser.setRefreshToken(UlidCreator.getUlid().toString());
                redisTemplate.boundValueOps("Refresh_Token:"+tokenUser.getRefreshToken()).set(account, JwtConstants.REFRESH_TOKEN_EXPIRES_HOUR, TimeUnit.HOURS);
                redisTemplate.delete(refreshToken);
                return tokenUser;
            }
        }
        throw new UnauthorizedException();
    }

    @Override
    public Set<String> findUserPermissions(User user) {
        Set<String> permissions = new HashSet<>();
        List<Role> roles = roleMapper.selectRoleByIdUser(user.getIdUser());
        for (Role role : roles) {
            if (StringUtils.isNotBlank(role.getInputCode())) {
                permissions.add(role.getInputCode());
            }
        }
        permissions.add("user");
        return permissions;
    }

    @Override
    public boolean hasAdminPermission(String account) {
        return userMapper.hasAdminPermission(account);
    }

    @Override
    public Map<Integer,List<UserDTO>> getGroupedUsersByRoleList(List<Integer> roleIds){
        // Service 负责实现具体逻辑
        // 使用一个 Map 来存储角色ID对应的用户列表
        Map<Integer, List<UserDTO>> result = new HashMap<>();

        for (Integer roleId : roleIds) {
            List<UserDTO> users = userMapper.getUsersByRoleId(roleId);
            result.put(roleId, users);
        }
        // 返回所有角色对应的用户数据
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addUser(User user, Integer roleId){
        try {
            // 1. 插入用户
            userMapper.addUser(user, roleId);

            // 2. 获取用户ID
            Long userId = user.getIdUser();
            if (userId == null) {
                throw new RuntimeException("用户插入失败");
            }

            // 4. 插入用户角色
            int rowsAffected = userMapper.insertUserRole(userId, roleId);
            if (rowsAffected <= 0) {
                // 5. 如果插入用户角色失败，抛出异常
                throw new RuntimeException("插入用户角色失败");
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void updateUser(User user){
         userMapper.updateByEmail(user);
    }
    @Override
    public User getUserByEmail(String email){
        return userMapper.selectByEmail(email);
    }
}
