package com.breech.extremity.web.api.admin;

import com.breech.extremity.auth.annotation.RolesAllowed;
import com.breech.extremity.dto.admin.RolesDTO;
import com.breech.extremity.dto.admin.UserRoleDTO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.breech.extremity.core.exception.ServiceException;
import com.breech.extremity.core.response.GlobalResult;
import com.breech.extremity.core.response.GlobalResultGenerator;
import com.breech.extremity.dto.*;
import com.breech.extremity.model.*;

import com.breech.extremity.service.*;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author ronger
 */
@RolesAllowed({1})
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Resource
    private UserService userService;
    @Resource
    private RoleService roleService;

    @GetMapping("/users")  // 所有用户 所有角色都返回
    public GlobalResult<PageInfo<UserInfoDTO>> users(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer rows, UserSearchDTO searchDTO) {
        PageHelper.startPage(page, rows);
        List<UserInfoDTO> list = userService.findUsers(searchDTO);
        PageInfo<UserInfoDTO> pageInfo = new PageInfo<>(list);
        return GlobalResultGenerator.genSuccessResult(pageInfo);
    }

    @GetMapping("/user/{idUser}/role")  // 根据用户ID获取用户角色的role
    public GlobalResult<List<Role>> userRole(@PathVariable Long idUser) {
        List<Role> roles = roleService.findByIdUser(idUser);
        return GlobalResultGenerator.genSuccessResult(roles);
    }

    @GetMapping("/roles")  // 获取所有角色列表 就是roles中的数据 甚至可以新增角色
    public GlobalResult<PageInfo<Role>> roles(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "10") Integer rows) {
        PageHelper.startPage(page, rows);
        List<Role> list = roleService.findAll();
        PageInfo<Role> pageInfo = new PageInfo<>(list);
        return GlobalResultGenerator.genSuccessResult(pageInfo);
    }

    @PatchMapping("/user/update-status")  // 更新用户状态
    public GlobalResult<Boolean> updateUserStatus(@RequestBody User user) throws ServiceException {
        boolean flag = userService.updateStatus(user.getIdUser(), user.getStatus());
        return GlobalResultGenerator.genSuccessResult(flag);
    }

    @GetMapping("/user/get-role")  // 没用了
    public GlobalResult<UserRoleDTO> getRole(@RequestParam("idUser") Long idUser) {
        UserRoleDTO userRole = roleService.getUserRoleByUserId(idUser);
        return GlobalResultGenerator.genSuccessResult(userRole);
    }

    @GetMapping("/user/get-deactivate-role")
    public GlobalResult<List<UserRoleDTO>> deactivateRole(@RequestParam("idRole") Integer idRole) {
        List<UserRoleDTO> userRoleDTOList = roleService.getDeactivateUserRoleList(idRole);
        return GlobalResultGenerator.genSuccessResult(userRoleDTOList);
    }

    @GetMapping("/user/get-activate-role")
    public GlobalResult<List<UserRoleDTO>> activateRole(@RequestParam("idRole") Integer idRole) {
        List<UserRoleDTO> userRoleDTOList = roleService.getActivateUserRoleList(idRole);
        return GlobalResultGenerator.genSuccessResult(userRoleDTOList);
    }

    @PostMapping("/role/post")  // 添加角色
    public GlobalResult<Boolean> addRole(@RequestBody Role role) throws ServiceException {
        boolean flag = roleService.saveRole(role);
        return GlobalResultGenerator.genSuccessResult(flag);
    }

    @PutMapping("/role/post")  // 更新角色信息
    public GlobalResult<Boolean> updateRole(@RequestBody Role role) throws Exception {
        boolean flag = roleService.saveRole(role);
        return GlobalResultGenerator.genSuccessResult(flag);
    }

    @GetMapping("/user/get-by-role-ids")  // 根据ID列表返回对应用户
    public GlobalResult<Map<String, List<UserDTO>>> getGroupedUsersByRoleIds(@RequestParam List<Integer> ids) throws Exception {
        Map<String, List<UserDTO>> groupedUsers = userService.getGroupedUsersByRoleList(ids);
        return GlobalResultGenerator.genSuccessResult(groupedUsers);
    }

    @PostMapping("/user/add-user") // 管理员直接创建用户（团队管理员 直接激活，但是没有额外权限）
    public GlobalResult<Boolean> addUser(@RequestBody User user) throws Exception{
        boolean flag = userService.addUser(user);
        if(!flag){
            return GlobalResultGenerator.genResult(false, null, "账户/邮箱已被注册");
        } else {
            return GlobalResultGenerator.genSuccessResult(true);
        }
    }

    @GetMapping("/user/activate-role") // 激活角色
    public GlobalResult<Boolean> activateRoleByUserId(@RequestParam("idUser") Long idUser, @RequestParam("idRole") Integer idRole) throws Exception {
        boolean flag = roleService.activateRoleByUserId(idUser, idRole, 1);
        return GlobalResultGenerator.genSuccessResult(flag);
    }

    @GetMapping("/user/deactivate-role")  //取消激活
    public GlobalResult<Boolean> deactivateRoleByUserId(@RequestParam("idUser") Long idUser, @RequestParam("idRole") Integer idRole) throws Exception {
        boolean flag = roleService.activateRoleByUserId(idUser, idRole, 0);
        return GlobalResultGenerator.genSuccessResult(flag);
    }

//    @GetMapping("/user/manage-permission")  // 管理团队管理员更高级的权限
//    public GlobalResult<Boolean> managePermission(@RequestParam("idUser") Long idUser, @RequestParam("idRole") Integer idRole, @RequestParam("level")Integer level) throws Exception {
//        boolean flag = roleService.activateRoleByUserId(idUser, idRole, level);
//        return GlobalResultGenerator.genSuccessResult(flag);
//    }


    @GetMapping("/simple-roles")  // 获取全部role 的id 和 name
    public GlobalResult<List<RolesDTO>> getAllRoles() throws Exception {
        return GlobalResultGenerator.genSuccessResult(roleService.getAllRoles());
    }

//    @GetMapping("user/grant-role")
//    public GlobalResult<Boolean> grantUserRole(@RequestParam Long idUser, @RequestParam Integer idRole) throws Exception{
//        List<Role> roles = roleService.findByIdUser(idUser);
//        // 只要有一个身份，就移除未认证
//        for (Role role : roles) {
//            if(role.getIdRole() == 5){
//                userService.revokeUserRole(idUser, 5);
//            }
//        }
//        return GlobalResultGenerator.genSuccessResult(userService.grantUserRole(idUser, idRole));
//    }
//
//    @GetMapping("user/revoke-role")
//    public GlobalResult<Boolean> revokeUserRole(@RequestParam Long idUser, @RequestParam Integer idRole) throws Exception{
//        List<Role> roles = roleService.findByIdUser(idUser);
//        // 至少保留一个身份
//        if(roles.size() == 1){
//            userService.revokeUserRole(idUser, idRole);
//            return GlobalResultGenerator.genResult(userService.grantUserRole(idUser, 5), null, "roll back to unauthorized_user");
//        }
//        return GlobalResultGenerator.genSuccessResult(userService.revokeUserRole(idUser, idRole));
//    }

    @GetMapping("user/delete-user")
    public GlobalResult<Boolean> deleteUser(@RequestParam Long idUser) throws Exception{
        boolean flag = userService.deleteUser(idUser);
        return GlobalResultGenerator.genSuccessResult(flag);
    }

    @GetMapping("user/show-user-info")
    public GlobalResult<UserInfoDTO> showUserInfo(@RequestParam Long idUser) throws Exception{
        UserInfoDTO userInfoDTo = userService.showUserInfo(idUser);
        return GlobalResultGenerator.genSuccessResult(userInfoDTo);
    }

    @PostMapping("user/update-user-info")
    public GlobalResult<Boolean> updateUserInfo(@RequestBody UserInfoDTO userInfoDTO) throws Exception{
        boolean flag = userService.updateUserInfo(userInfoDTO);
        return GlobalResultGenerator.genSuccessResult(flag);
    }
}
