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
import com.mysql.cj.conf.PropertyKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.exceptions.TooManyResultsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Condition;

import javax.annotation.Resource;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    public boolean register(String email, String password, String code,String message) {
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
                    user.setPassword(Utils.entryptPassword(password));   //
                    user.setCreatedTime(new Date());                     //
                    user.setUpdatedTime(user.getCreatedTime());
                    user.setAvatarUrl(DEFAULT_AVATAR);
                    userMapper.insertSelective(user);
                    user = userMapper.selectByAccount(email);
                    Condition condition = new Condition(Role.class);
                    condition.createCriteria().andEqualTo("name",message);
                    List<Role> role = roleMapper.selectByCondition(condition);
                    try{
                        Role role1 = role.get(0);
                        userMapper.insertUserRole(user.getIdUser(), role1.getIdRole(),message,0);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
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
                redisTemplate.boundValueOps("Refresh_Token:"+tokenUser.getRefreshToken()).set(user.getAccount(), JwtConstants.REFRESH_TOKEN_EXPIRES_HOUR, TimeUnit.HOURS);
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
                redisTemplate.boundValueOps("Refresh_Token:"+tokenUser.getRefreshToken()).set(user.getAccount(), JwtConstants.REFRESH_TOKEN_EXPIRES_HOUR, TimeUnit.HOURS);
                return tokenUser;
            }
        }
        throw new AuthenticationException();
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
        String account = redisTemplate.boundValueOps("Refresh_Token:"+refreshToken).get();
        log.warn(account);

        if (StringUtils.isNotBlank(account)) {
            UserRolesDTO userroles = tokenManager.getRoles(account);
            User nucleicUser = userMapper.selectByAccount(account);
            if (nucleicUser != null) {
                TokenUser tokenUser = new TokenUser();

                if(userroles!=null){
                    tokenManager.deleteToken(account);
                    tokenUser.setToken(tokenManager.createToken(userroles));
                }else{
                    UserRolesDTO roles_res = this.findRolesByAccount(account);
                    log.warn(roles_res.getId());
                    tokenUser.setToken(tokenManager.createToken(roles_res));
                }

                tokenUser.setRefreshToken(UlidCreator.getUlid().toString());

                redisTemplate.boundValueOps("Refresh_Token:"+tokenUser.getRefreshToken()).set(account, JwtConstants.REFRESH_TOKEN_EXPIRES_HOUR, TimeUnit.HOURS);

                redisTemplate.delete("Refresh_Token:"+ refreshToken);
                return tokenUser;
            }
        }

        log.warn("显然没有找到对应用户");
        throw new UnauthenticatedException();
    }

    @Override
    public boolean hasAdminPermission(String account) {
        return userMapper.hasAdminPermission(account);
    }

    @Override
    public Map<String,List<UserDTO>> getGroupedUsersByRoleList(List<Integer> roleIds){
        // Service 负责实现具体逻辑
        // 使用一个 Map 来存储角色ID对应的用户列表
        Map<String, List<UserDTO>> result = new HashMap<>();

        for (Integer roleId : roleIds) {
            List<UserDTO> users = userMapper.getUsersByRoleId(roleId);
            result.put(roleId.toString(), users);
        }
        // 返回所有角色对应的用户数据
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addUser(User user){
        // 账号和邮箱都不能重复
        if(userMapper.selectUserByAccountAndEmail(user.getAccount(), user.getEmail()) != null){
            return null;
        }
        // 生成基于邮箱的密码
        String password = generatePasswordFromEmail(user.getEmail());
        // System.out.println("随机生成的密码：" + password);

        user.setPassword(password);
        // 特殊处理
        String nickname = user.getEmail().split("@")[0];
        user.setNickname(nickname);
        user.setPassword(Utils.entryptPassword(user.getPassword())); // 密码加密
        user.setCreatedTime(new Date());
        user.setUpdatedTime(user.getCreatedTime());
        user.setStatus("0"); // 不在线

        userMapper.addUser(user);
        log.info(user.toString());

        // 授予团队管理员身份
        userMapper.grantUserRole(user.getIdUser(), 2);
        return password;
    }

    // 通过邮箱生成确定性密码
    private String generatePasswordFromEmail(String email) {
        try {
            // 使用SHA-256算法生成哈希值
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(email.getBytes());

            // 取哈希值的前12个字符作为密码
            StringBuilder password = new StringBuilder();
            for (int i = 0; i < 12; i++) {
                password.append(String.format("%02x", hash[i % hash.length]));  // 格式化为十六进制
            }

            return password.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating password from email", e);
        }
    }

    @Override
    public boolean grantUserRole(Long idUser, Integer idRole) throws ServiceException{
        return userMapper.grantUserRole(idUser, idRole);
    }

    @Override
    public boolean revokeUserRole(Long idUser, Integer idRole) throws ServiceException{
        return userMapper.revokeUserRole(idUser, idRole);
    }

    @Override
    public boolean deleteUser(Long idUser) throws ServiceException{
        return userMapper.deleteUser(idUser);
    }

    @Override
    public UserInfoDTO showUserInfo(Long idUser) throws ServiceException{
        return userMapper.showUserInfo(idUser);
    }

    @Override
    public boolean allocateTeamAdminPermission(Long idUser, Integer permission) throws ServiceException{
        return userMapper.allocateTeamAdminPermission(idUser, permission);
    }

    @Override
    public boolean deallocateTeamAdminPermission(Long idUser, Integer permission) throws ServiceException{
        return userMapper.deallocateTeamAdminPermission(idUser, permission);
    }

    @Override
    public List<Integer> getTeamAdminPermissions(Long idUser) throws ServiceException{
        return userMapper.getTeamAdminPermissions(idUser);
    }

    @Override
    public boolean updateUserInfo(UserInfoDTO userInfoDTO) throws ServiceException{
        return userMapper.updateUserInfo(userInfoDTO);
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
