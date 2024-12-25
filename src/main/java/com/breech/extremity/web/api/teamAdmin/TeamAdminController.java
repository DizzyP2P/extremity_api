package com.breech.extremity.web.api.teamAdmin;

import com.breech.extremity.auth.annotation.RolesAllowed;
import com.breech.extremity.core.response.GlobalResult;
import com.breech.extremity.core.response.GlobalResultGenerator;
import com.breech.extremity.dto.TeamMemberInfoDTO;
import com.breech.extremity.model.TeamInfo;
import com.breech.extremity.model.User;
import com.breech.extremity.service.RoleService;
import com.breech.extremity.service.TeamService;
import com.breech.extremity.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static net.sf.jsqlparser.util.validation.metadata.NamedObject.user;

// @RolesAllowed({2})
@RestController
@RequestMapping("/api/team-admin")
public class TeamAdminController {
    @Resource
    private UserService userService;
    @Resource
    private RoleService roleService;
    @Resource
    private TeamService teamService;

    @PostMapping("/team/edit")  // 编辑团队信息
    public GlobalResult<Boolean> editTeamInfo(@RequestBody TeamInfo teamInfo) throws Exception {
        System.out.print(teamInfo.toString());
        boolean flag = teamService.updateTeamInfo(teamInfo);
        return GlobalResultGenerator.genSuccessResult(flag);
    }

    @GetMapping("/team/show-info") // 获取团队信息
    public GlobalResult<TeamInfo> showTeamInfo(@RequestParam("teamId") Integer teamId) throws Exception {
        TeamInfo teamInfo = teamService.selectTeamInfo(teamId);
        return GlobalResultGenerator.genSuccessResult(teamInfo);
    }

//    @GetMapping("/team/delete-user")  // 注销用户账户
//    public GlobalResult<Boolean> deleteUser(@RequestParam Long idUser) throws Exception{
//        boolean flag = userService.deleteUser(idUser);
//        return GlobalResultGenerator.genSuccessResult(flag);
//    }

    @GetMapping("/get-activated-normal-users")  // 获取普通用户
    public GlobalResult<List<User>> getActivatedNormalUsers() throws Exception {
        List<User> userList = teamService.getActivatedNormalUsers();
        return GlobalResultGenerator.genSuccessResult(userList);
    }

    @GetMapping("/get-activated-team-members") // 获取团队用户
    public GlobalResult<List<User>> getActivatedTeamMembers() throws Exception {
        List<User> userList = teamService.getActivatedTeamMembers();
        return GlobalResultGenerator.genSuccessResult(userList);
    }

    @GetMapping("/get-deactivated-normal-users")  // 获取普通用户（未认证）
    public GlobalResult<List<User>> getDeactivatedNormalUsers() throws Exception {
        List<User> userList = teamService.getDeactivatedNormalUsers();
        return GlobalResultGenerator.genSuccessResult(userList);
    }

    @GetMapping("/get-deactivated-team-members")  // 获取团队成员（未认证）
    public GlobalResult<List<User>> getDeactivatedTeamMembers() throws Exception {
        List<User> userList = teamService.getDeactivatedTeamMembers();
        return GlobalResultGenerator.genSuccessResult(userList);
    }

    @GetMapping("get-member-info") // 获取团队用户（已认证）的详细信息
    public GlobalResult<TeamMemberInfoDTO> getTeamMemberInfo(@RequestParam("teamMemberId") Integer teamMemberId) throws Exception{
        TeamMemberInfoDTO teamMemberInfoDTO = teamService.getTeamMemberInfo(teamMemberId);
        return  GlobalResultGenerator.genSuccessResult(teamMemberInfoDTO);
    }

    @PostMapping("add-team-member") // 创建团队成员账号并编辑信息 直接激活
    public GlobalResult<Boolean> addTeamMember(@RequestBody User user) throws Exception {
        boolean flag = teamService.addTeamMemberAccount(user);
        if(!flag){
            return GlobalResultGenerator.genResult(false, null, "账户/邮箱已被注册");
        } else {
            return GlobalResultGenerator.genSuccessResult(true);
        }
    }
}
