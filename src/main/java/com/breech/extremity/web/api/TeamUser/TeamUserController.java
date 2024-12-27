package com.breech.extremity.web.api.TeamUser;

import com.breech.extremity.core.response.GlobalResult;
import com.breech.extremity.core.response.GlobalResultGenerator;
import com.breech.extremity.core.service.redis.impl.RedisServiceImpl;
import com.breech.extremity.model.TeamMemberInfo;
import com.breech.extremity.model.User;
import com.breech.extremity.service.TeamUserService;
import com.breech.extremity.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/team-user")
public class TeamUserController {
    @Resource
    private TeamUserService teamUserService;
    @Value("${resource.file-download-url}")
    private String fileDownloadUrl;
    private static final Logger logger = LoggerFactory.getLogger(RedisServiceImpl.class);

    @Resource
    private UserService userService;

    @GetMapping("/all")
    public GlobalResult<List<TeamMemberInfo>> getAllTeamUsers() {
        List<TeamMemberInfo> teamMemberInfos = teamUserService.getAllTeamMember();
        return GlobalResultGenerator.genSuccessResult(teamMemberInfos);

    }

    @GetMapping("/id")
    public GlobalResult<TeamMemberInfo> getTeamUsersById(@RequestParam("id") int id) {
        TeamMemberInfo teamMemberInfo = teamUserService.getTeamMemberInfo(id);
        return GlobalResultGenerator.genSuccessResult(teamMemberInfo);

    }

    @PostMapping("/upload-avatar")
    public GlobalResult<String> uploadAvatar(@RequestBody TeamMemberInfo teamMemberInfo) {
        try {
            logger.warn(String.valueOf(teamMemberInfo));
            teamUserService.update(teamMemberInfo);
            return GlobalResultGenerator.genSuccessResult("ok");
        } catch (Exception e) {
            e.printStackTrace();
            return GlobalResultGenerator.genErrorResult("更新信息失败");
        }
    }
}
