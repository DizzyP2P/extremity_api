package com.breech.extremity.web.api.admin;

import com.breech.extremity.auth.annotation.RolesAllowed;
import com.breech.extremity.dto.admin.RolesDTO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.breech.extremity.core.exception.ServiceException;
import com.breech.extremity.core.response.GlobalResult;
import com.breech.extremity.core.response.GlobalResultGenerator;
import com.breech.extremity.dto.*;
import com.breech.extremity.dto.admin.UserRoleDTO;
import com.breech.extremity.model.*;

import com.breech.extremity.service.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.*;
import retrofit2.http.Body;

import javax.annotation.Resource;
import javax.persistence.PostUpdate;
import javax.servlet.http.HttpServletRequest;
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

    @GetMapping("/user/{idUser}/role")  // 根据用户ID获取用户角色的全部role
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

//    @PatchMapping("/role/update-status")  // 更新角色状态
//    public GlobalResult<Boolean> updateRoleStatus(@RequestBody Role role) throws ServiceException {
//        boolean flag = roleService.updateStatus(role.getIdRole(), role.getStatus());
//        return GlobalResultGenerator.genSuccessResult(flag);
//    }

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

    @PostMapping("/user/add-user") // 管理员直接创建用户（不授权）
    public GlobalResult<Boolean> addUser(@RequestBody User user) throws Exception{
        boolean flag = userService.addUser(user);
        if(!flag){
            return GlobalResultGenerator.genResult(false, null, "account is registered!!!");
        } else {
            return GlobalResultGenerator.genSuccessResult(true);
        }
    }

    @GetMapping("/simple-roles")  // 获取全部role 的id 和 name
    public GlobalResult<List<RolesDTO>> getAllRoles() throws Exception {
        return GlobalResultGenerator.genSuccessResult(roleService.getAllRoles());
    }

    @GetMapping("user/grant-role")
    public GlobalResult<Boolean> grantUserRole(@RequestParam Long idUser, @RequestParam Integer idRole) throws Exception{
        List<Role> roles = roleService.findByIdUser(idUser);
        // 只要有一个身份，就移除未认证
        for (Role role : roles) {
            if(role.getIdRole() == 5){
                userService.revokeUserRole(idUser, role.getIdRole());
            }
        }
        return GlobalResultGenerator.genSuccessResult(userService.grantUserRole(idUser, idRole));
    }

    @GetMapping("user/revoke-role")
    public GlobalResult<Boolean> revokeUserRole(@RequestParam Long idUser, @RequestParam Integer idRole) throws Exception{
        List<Role> roles = roleService.findByIdUser(idUser);
        // 至少保留一个身份
        if(roles.size() == 1){
            userService.revokeUserRole(idUser, idRole);
            return GlobalResultGenerator.genResult(userService.grantUserRole(idUser, 5), null, "roll back to unauthorized_user");
        }
        return GlobalResultGenerator.genSuccessResult(userService.revokeUserRole(idUser, idRole));
    }

//    @PatchMapping("/user/update-role")  // 更新用户角色（授权）
//    public GlobalResult<Boolean> updateUserRole(@RequestBody UserRoleDTO userRole) throws ServiceException {
//        boolean flag = userService.updateUserRole(userRole.getIdUser(), userRole.getIdRole());
//        return GlobalResultGenerator.genSuccessResult(flag);
//    }
}
