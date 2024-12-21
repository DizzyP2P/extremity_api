package com.breech.extremity.web.api.admin;

import com.breech.extremity.auth.annotation.RolesAllowed;
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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

    @GetMapping("/user/{idUser}/role")  // 根据用户ID获取用户角色role
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

    @PatchMapping("/user/update-role")  // 更新用户角色
    public GlobalResult<Boolean> updateUserRole(@RequestBody UserRoleDTO userRole) throws ServiceException {
        boolean flag = userService.updateUserRole(userRole.getIdUser(), userRole.getIdRole());
        return GlobalResultGenerator.genSuccessResult(flag);
    }

    @PatchMapping("/user/update-status")  // 更新用户状态
    public GlobalResult<Boolean> updateUserStatus(@RequestBody User user) throws ServiceException {
        boolean flag = userService.updateStatus(user.getIdUser(), user.getStatus());
        return GlobalResultGenerator.genSuccessResult(flag);
    }

    @PatchMapping("/role/update-status")  // 更新角色状态
    public GlobalResult<Boolean> updateRoleStatus(@RequestBody Role role) throws ServiceException {
        boolean flag = roleService.updateStatus(role.getIdRole(), role.getStatus());
        return GlobalResultGenerator.genSuccessResult(flag);
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

    @GetMapping("/user/get-by-role-list")  // 根据ID列表返回对应角色
    public GlobalResult<Boolean> getUsersByRoleLst(@RequestParam List<Integer> ids) throws Exception {

        return GlobalResultGenerator.genSuccessResult();
    }

}
