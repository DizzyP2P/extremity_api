package com.breech.extremity.mapper;

import com.breech.extremity.core.mapper.Mapper;
import com.breech.extremity.dto.Author;
import com.breech.extremity.dto.UserDTO;
import com.breech.extremity.dto.UserInfoDTO;
import com.breech.extremity.dto.UserSearchDTO;
import com.breech.extremity.model.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author ronger
 */
public interface UserMapper extends Mapper<User> {

    /**
     * 根据账号获取获取用户信息
     *
     * @param account
     * @return
     */
    User selectByAccount(@Param("account") String account);

    /**
     * 根据账号获取获取用户信息
     *
     * @param account
     * @return
     */
    UserInfoDTO findUserInfoByAccount(@Param("account") String account);

    /**
     * 超级管理员创建团队管理员时的检查
     *
     * @param account
     * @Param email
     * @return null/not null
     */
    User selectUserByAccountAndEmail(@Param("account") String account, @Param("email") String email);

    /**
     * 修改用户密码
     *
     * @param email
     * @param password
     * @return
     */
    Integer updatePasswordByEmail(@Param("email") String email, @Param("password") String password);

    /**
     * 获取用户权限权重
     *
     * @param idUser
     * @return
     */
    Integer selectRoleWeightsByUser(@Param("idUser") Long idUser);

    /**
     * 更新用户状态
     *
     * @param idUser
     * @param status
     * @return
     */
    Integer updateStatus(@Param("idUser") Long idUser, @Param("status") String status);

    /**
     * 根据昵称获取重名用户数量
     *
     * @param nickname
     * @return
     */
    Integer selectCountByNickName(@Param("nickname") String nickname);

    /**
     * 获取用户信息
     *
     * @param idUser
     * @return
     */
    UserInfoDTO selectUserInfo(@Param("idUser") Long idUser);

    /**
     * 更新用户信息
     *
     * @param "UserInfoDTO"
     * @return
     */
    Boolean updateUserInfo(@Param("userInfoDTO") UserInfoDTO userInfoDTO);

    /**
     * 验证昵称是否重复
     *
     * @param idUser
     * @param nickname
     * @return
     */
    Integer checkNicknameByIdUser(@Param("idUser") Long idUser, @Param("nickname") String nickname);

    /**
     * 根据用户 ID 获取作者信息
     *
     * @param id
     * @return
     */
    Author selectAuthor(@Param("id") Long id);

    /**
     * 更新用户最后登录时间
     *
     * @param idUser
     * @return
     */
    Integer updateLastLoginTime(@Param("idUser") Long idUser);

    /**
     * 更换邮箱
     *
     * @param idUser
     * @param email
     * @return
     */
    Integer updateEmail(@Param("idUser") Long idUser, @Param("email") String email);

    /**
     * 更新密码
     *
     * @param idUser
     * @param password
     * @return
     */
    Integer updatePasswordById(@Param("idUser") Long idUser, @Param("password") String password);

    /**
     * 查询用户数据
     *
     * @param searchDTO
     * @return
     */
    List<UserInfoDTO> selectUsers(@Param("searchDTO") UserSearchDTO searchDTO);

    /**
     * 更新用户最后在线时间
     *
     * @param account
     * @return
     */
    Integer updateLastOnlineTimeByAccount(@Param("account") String account);

    /**
     * 判断用户是否拥有管理员权限
     *
     * @param email
     * @return
     */
    boolean hasAdminPermission(@Param("email") String email);

    Integer selectCountByAccount(@Param("account") String account);

    List<UserDTO> getUsersByRoleId(@Param("roleId") Integer roleId);

    boolean addUser(@Param("user") User user);

    /**
     * 添加用户权限
     *
     * @param idUser
     * @param idRole
     *
     */
    Integer insertUserRole(@Param("idUser") Long idUser, @Param("idRole") Integer idRole,@Param("message")String message,@Param("activated") int activated);

    /**
     * 为用户授予角色
     *
     * @param idUser 用户ID
     * @param idRole 角色ID
     * @return 操作是否成功
     */
    boolean grantUserRole(@Param("idUser") Long idUser, @Param("idRole") Integer idRole);

    /**
     * 撤销用户角色
     *
     * @param idUser 用户ID
     * @param idRole 角色ID
     * @return 操作是否成功
     */
    boolean revokeUserRole(@Param("idUser") Long idUser, @Param("idRole") Integer idRole);

    /**
     * 注销用户账号
     */
    boolean deleteUser(@Param("idUser") Long idUser);

    UserInfoDTO showUserInfo(@Param("idUser") Long idUser);

    boolean allocateTeamAdminPermission(@Param("idUser") Long idUser, @Param("permission") Integer permission);

    boolean deallocateTeamAdminPermission(@Param("idUser") Long idUser, @Param("permission") Integer permission);

    List<Integer> getTeamAdminPermissions(@Param("idUser") Long idUser);

    void updateByEmail(User user);

    User selectByEmail(String email);
}