package com.breech.extremity.web.api.teamAdmin;

import com.alibaba.fastjson.JSON;
import com.breech.extremity.auth.annotation.RolesAllowed;
import com.breech.extremity.core.exception.AccountExistsException;
import com.breech.extremity.core.response.GlobalResult;
import com.breech.extremity.core.response.GlobalResultGenerator;
import com.breech.extremity.core.response.NormalResponseMessage;
import com.breech.extremity.core.service.redis.impl.RedisServiceImpl;
import com.breech.extremity.dto.TeamMemberInfoDTO;
import com.breech.extremity.model.TeamInfo;
import com.breech.extremity.model.TeamMemberInfo;
import com.breech.extremity.model.User;
import com.breech.extremity.service.JavaMailService;
import com.breech.extremity.service.RoleService;
import com.breech.extremity.service.TeamService;
import com.breech.extremity.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/api/team-admin")
public class TeamAdminController {
    @Resource
    private UserService userService;
    @Resource
    private RoleService roleService;
    @Resource
    private TeamService teamService;
    @Resource
    private JavaMailService javaMailService;

    @Value("${resource.image-download-url}")
    private String fileDownloadUrl;

    private static final Logger logger = LoggerFactory.getLogger(RedisServiceImpl.class);

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

    @GetMapping("delete-user")  // 注销用户账户
    public GlobalResult<Boolean> deleteUser(@RequestParam Long idUser) throws Exception{
        boolean flag = userService.deleteUser(idUser);
        return GlobalResultGenerator.genSuccessResult(flag);
    }

    @GetMapping("/get-activated-normal-users")  // 获取普通用户（已通过）
    public GlobalResult<List<User>> getActivatedNormalUsers() throws Exception {
        List<User> userList = teamService.getActivatedNormalUsers();
        return GlobalResultGenerator.genSuccessResult(userList);
    }

    @GetMapping("/get-activated-team-members") // 获取团队用户（已通过）
    public GlobalResult<List<User>> getActivatedTeamMembers() throws Exception {
        List<User> userList = teamService.getActivatedTeamMembers();
        return GlobalResultGenerator.genSuccessResult(userList);
    }

    @GetMapping("/get-deactivated-normal-users")  // 获取普通用户（待审核）
    public GlobalResult<List<User>> getDeactivatedNormalUsers() throws Exception {
        List<User> userList = teamService.getDeactivatedNormalUsers();
        return GlobalResultGenerator.genSuccessResult(userList);
    }

    @GetMapping("/get-deactivated-team-members")  // 获取团队成员（待审核）
    public GlobalResult<List<User>> getDeactivatedTeamMembers() throws Exception {
        List<User> userList = teamService.getDeactivatedTeamMembers();
        return GlobalResultGenerator.genSuccessResult(userList);
    }

    @GetMapping("/get-refused-normal-users")  // 获取普通用户（拒绝）
    public GlobalResult<List<User>> getRefusedNormalUsers() throws Exception {
        List<User> userList = teamService.getRefusedNormalUsers();
        return GlobalResultGenerator.genSuccessResult(userList);
    }

    @GetMapping("/get-refused-team-members")  // 获取团队成员（拒绝）
    public GlobalResult<List<User>> getRefusedTeamMembers() throws Exception {
        List<User> userList = teamService.getRefusedTeamMembers();
        return GlobalResultGenerator.genSuccessResult(userList);
    }

    @GetMapping("/get-delete-users")  // 获取想要注销账号的普通用户
    public GlobalResult<List<User>> getDeleteUsers() throws Exception {
        List<User> userList = teamService.getDeleteUsers();
        return GlobalResultGenerator.genSuccessResult(userList);
    }

    @GetMapping("get-member-info") // 获取团队用户（已认证）的详细信息
    public GlobalResult<TeamMemberInfoDTO> getTeamMemberInfo(@RequestParam("teamMemberId") Integer teamMemberId) throws Exception{
        TeamMemberInfoDTO teamMemberInfoDTO = teamService.getTeamMemberInfo(teamMemberId);
        return  GlobalResultGenerator.genSuccessResult(teamMemberInfoDTO);
    }

    @PostMapping("edit-member-info")// 更新团队成员的信息
    public GlobalResult<Boolean> getTeamMemberInfo(@RequestParam String teamMemberInfoDTO, @RequestParam(value = "file", required = false) MultipartFile file, @RequestParam(value = "file2", required = false) MultipartFile file2) throws Exception{
        // System.out.println(teamMemberInfoDTO);
        TeamMemberInfoDTO teamMemberInfoDTO1 = JSON.parseObject(teamMemberInfoDTO, TeamMemberInfoDTO.class);

        // 验证文件名有效性
        if(file != null) {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "文件名无效");
            }

            // 创建上传目录
            File uploadDir = new File(fileDownloadUrl);
            if (!uploadDir.exists() && !uploadDir.mkdirs()) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "无法创建上传目录");
            }

            // 删除旧头像文件
            String oldAvatarUrl = teamMemberInfoDTO1.getAvatarUrl();
            if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
                File oldFile = new File(uploadDir, oldAvatarUrl);
                if (oldFile.exists() && !oldFile.delete()) {
                    logger.error("删除旧头像文件失败: " + oldAvatarUrl);
                }
            }

            // 保存新头像文件
            String newFilename = UUID.randomUUID() + "_" + originalFilename;
            File newFile = new File(uploadDir, newFilename);
            file.transferTo(newFile);

            teamMemberInfoDTO1.setAvatarUrl(newFilename);
        }
        boolean flag = teamService.editTeamMemberInfo(teamMemberInfoDTO1);
        return  GlobalResultGenerator.genSuccessResult(flag);
    }

    @PostMapping("add-team-member") // 创建团队成员账号并编辑信息 直接通过
    public GlobalResult<Boolean> addTeamMember(@RequestBody User user) throws Exception {
        // 创建账号基本信息
        String password = teamService.addTeamMemberAccount(user);
        // 发送密码至邮箱
        Integer result = javaMailService.sendPassword(user.getEmail(), password);
        // 添加附加信息
        TeamMemberInfo teamMemberInfo = new TeamMemberInfo();
        teamMemberInfo.setUserId(user.getIdUser());
        teamMemberInfo.setPosition("暂无职位");
        teamMemberInfo.setPersonalBio("一个平淡的经历...");
        teamMemberInfo.setResearchDirection("暂无研究方向");
        teamService.insertTeamMemberAdditionalInfo(teamMemberInfo);

        if(password == null || password.isEmpty()) {
            return GlobalResultGenerator.genResult(false, null, "账户/邮箱已被注册");
        } else {
            return GlobalResultGenerator.genSuccessResult(true);
        }
    }

    @GetMapping("activate-role") // 激活/同意申请
    public GlobalResult<Boolean> activateRoleByUserId(@RequestParam("idUser") Long idUser, @RequestParam("idRole") Integer idRole) throws Exception {
        boolean flag = roleService.changeRoleByUserId(idUser, idRole, 1, null);
        if(idRole == 3){// 如果是想成为团队成员，还需要添加信息
            TeamMemberInfo teamMemberInfo = new TeamMemberInfo();
            teamMemberInfo.setUserId(idUser);
            teamMemberInfo.setPosition("暂无职位");
            teamMemberInfo.setPersonalBio("一个平淡的经历...");
            teamMemberInfo.setResearchDirection("暂无研究方向");
            teamService.insertTeamMemberAdditionalInfo(teamMemberInfo);
        }
        return GlobalResultGenerator.genSuccessResult(flag);
    }

    @GetMapping("deactivate-role")  //取消激活    这个没用了
    public GlobalResult<Boolean> deactivateRoleByUserId(@RequestParam("idUser") Long idUser, @RequestParam("idRole") Integer idRole) throws Exception {
        boolean flag = roleService.changeRoleByUserId(idUser, idRole, 0, null);
        return GlobalResultGenerator.genSuccessResult(flag);
    }

    // 拒绝 + 拒绝理由
    @PostMapping("reject-user")  // 拒绝
    public GlobalResult<Boolean> reject(@RequestParam Long idUser, @RequestParam Integer idRole, @RequestParam String message) throws Exception {
        boolean flag = roleService.changeRoleByUserId(idUser, idRole, -1, message);
        return GlobalResultGenerator.genSuccessResult(flag);
    }


    @GetMapping("/get-team-members") // 获取团队用户
    public GlobalResult<List<User>> getTeamMembers() throws Exception {
        List<User> userList = teamService.getTeamMembers();
        return GlobalResultGenerator.genSuccessResult(userList);
    }
}
