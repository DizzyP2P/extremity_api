package com.breech.extremity.service;


import com.breech.extremity.core.exception.ServiceException;
import com.breech.extremity.core.service.Service;
import com.breech.extremity.dto.*;
import com.breech.extremity.model.Role;
import com.breech.extremity.model.User;
import org.apache.ibatis.exceptions.TooManyResultsException;

import java.util.List;
import java.util.Map;
import java.util.Set;


public interface UserService extends Service<User> {

    List<Role> findRolesByUserId(Long userId);

    UserRolesDTO findRolesByAccount(String account);
    /**
     * 通过账号查询用户信息
     *
     * @param account
     * @return User
     * @throws TooManyResultsException
     */
    User findByAccount(String account) throws TooManyResultsException;

    /**
     * 注册接口
     *
     * @param email    邮箱
     * @param password 密码
     * @param code     验证码
     * @return Map
     */
    boolean register(String email, String password, String code);

    /**
     * 登录接口
     *
     * @param account  邮箱
     * @param password 密码
     * @return Map
     */
    TokenUser login(String account, String password);

    /**
     * 登录接口
     *
     * @param account  邮箱
     * @param password 密码
     * @return Map
     */

    TokenUser adminLogin(String account, String password);


    /**
     * 找回密码接口
     *
     * @param code     验证码
     * @param password 密码
     * @return Map
     * @throws ServiceException
     */
    boolean forgetPassword(String code, String password) throws ServiceException;

    /**
     * 更新用户状态
     *
     * @param idUser 用户 id
     * @param status 状态
     * @return Map
     * @throws ServiceException
     */
    boolean updateStatus(Long idUser, String status) throws ServiceException;

    /**
     * 获取用户信息
     *
     * @param idUser
     * @return
     */
    UserInfoDTO findUserInfo(Long idUser);

    /**
     * 更新用户信息
     *
     * @param userInfoDTO
     * @return
     * @throws ServiceException
     */
    boolean updateUserInfo(UserInfoDTO userInfoDTO) throws ServiceException;

    /**
     * 验证昵称是否重复
     *
     * @param idUser
     * @param nickname
     * @return
     */
    boolean checkNicknameByIdUser(Long idUser, String nickname);

    /**
     * 获取用户权限
     *
     * @param idUser
     * @return
     */
    Integer findRoleWeightsByUser(Long idUser);

    /**
     * 查询作者信息
     *
     * @param idUser
     * @return
     */
    Author selectAuthor(Long idUser);


    /**
     * 查询用户列表
     *
     * @param searchDTO
     * @return
     */
    List<UserInfoDTO> findUsers(UserSearchDTO searchDTO);

    /**
     * 通过邮箱更新用户最后登录时间
     *
     * @param account
     * @return
     */
    Integer updateLastOnlineTimeByAccount(String account);


    /**
     * 刷新  token
     *
     * @param refreshToken
     * @return
     */
    TokenUser refreshToken(String refreshToken);


    boolean hasAdminPermission(String account);
    /**
     * 根据role_id 返回用户分组
     *
     * @param roleIds
     * @return user_list
     *
    */
    Map<String,List<UserDTO>> getGroupedUsersByRoleList(List<Integer> roleIds);

    boolean addUser(User user);
    boolean grantUserRole(Long idUser, Integer idRole) throws ServiceException;
    boolean revokeUserRole(Long idUser, Integer idRole) throws ServiceException;

    boolean deleteUser(Long idUser) throws ServiceException;

    UserInfoDTO showUserInfo(Long idUser) throws ServiceException;

    void updateUser(User user);
    User getUserByEmail(String email);
}
