package com.breech.extremity.web.api.teamAdmin;

import com.breech.extremity.auth.annotation.RolesAllowed;
import com.breech.extremity.core.response.GlobalResult;
import com.breech.extremity.core.response.GlobalResultGenerator;
import com.breech.extremity.model.TeamInfo;
import com.breech.extremity.service.RoleService;
import com.breech.extremity.service.TeamService;
import com.breech.extremity.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

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

    @GetMapping("/team/edit")  // 编辑团队信息
    public GlobalResult<Boolean> editTeamInfo(@RequestBody TeamInfo teamInfo) throws Exception {
        boolean flag = teamService.updateTeamInfo(teamInfo);
        return GlobalResultGenerator.genSuccessResult(flag);
    }

    @GetMapping("/team/show-info") // 获取团队信息
    public GlobalResult<TeamInfo> showTeamInfo(@RequestParam("teamId") Integer teamId) throws Exception {
        System.out.println("hhhhhhhhhhhhhh");
        TeamInfo teamInfo = teamService.selectTeamInfo(teamId);
        return GlobalResultGenerator.genSuccessResult(teamInfo);
    }

    @GetMapping("/team/delete-user")  // 注销用户账户
    public GlobalResult<Boolean> deleteUser(@RequestParam Long idUser) throws Exception{
        boolean flag = userService.deleteUser(idUser);
        return GlobalResultGenerator.genSuccessResult(flag);
    }

    @GetMapping("/team/test")
    public GlobalResult<Boolean> test() throws Exception{
        return GlobalResultGenerator.genSuccessResult(true);
    }
}
