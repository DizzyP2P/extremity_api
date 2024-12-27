package com.breech.extremity.service.impl;

import com.breech.extremity.core.service.AbstractService;
import com.breech.extremity.dto.TeamMemberInfoDTO;
import com.breech.extremity.mapper.RoleMapper;
import com.breech.extremity.mapper.TeamMapper;
import com.breech.extremity.mapper.UserMapper;
import com.breech.extremity.model.Role;
import com.breech.extremity.model.TeamInfo;
import com.breech.extremity.model.TeamMemberInfo;
import com.breech.extremity.model.User;
import com.breech.extremity.service.RoleService;
import com.breech.extremity.service.TeamService;
import com.breech.extremity.util.Utils;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class TeamServiceImpl extends AbstractService<TeamInfo> implements TeamService {
//    @Resource
//    private RoleMapper roleMapper;
//
    @Resource
    private UserMapper userMapper;

    @Resource
    private TeamMapper teamMapper;

    @Override
    public TeamInfo selectTeamInfo(Integer teamId){
        return teamMapper.selectTeamInfo(teamId);
    }

    @Override
    public boolean updateTeamInfo(TeamInfo teamInfo){
        return teamMapper.updateTeamInfo(teamInfo);
    }

    @Override
    public List<User> getActivatedNormalUsers(){
        return teamMapper.selectActivatedNormalUsers();
    }

    @Override
    public List<User> getActivatedTeamMembers(){
        return teamMapper.selectActivatedTeamMembers();
    }

    @Override
    public List<User> getDeactivatedNormalUsers(){
        return teamMapper.selectDeactivatedNormalUsers();
    }

    @Override
    public List<User> getDeactivatedTeamMembers(){
        return teamMapper.selectDeactivatedTeamMembers();
    }

    @Override
    public List<User> getRefusedNormalUsers(){
        return teamMapper.selectRefusedNormalUsers();
    }

    @Override
    public List<User> getRefusedTeamMembers(){
        return teamMapper.selectRefusedTeamMembers();
    }

    @Override
    public List<User> getDeleteUsers(){
        return teamMapper.selectDeleteUsers();
    }

    @Override
    public TeamMemberInfoDTO getTeamMemberInfo(Integer teamMemberId){
        return teamMapper.selectTeamMemberInfoById(teamMemberId);
    }

    @Override
    public boolean editTeamMemberInfo(TeamMemberInfoDTO teamMemberInfoDTO){
        return  teamMapper.updateTeamMemberInfoById(teamMemberInfoDTO);
    }

    @Override
    public boolean addTeamMemberAccount(User user){
        // 账号和邮箱都不能重复
        if(userMapper.selectUserByAccountAndEmail(user.getAccount(), user.getEmail()) != null){
            return false;
        }
        // 特殊处理
        String nickname = user.getEmail().split("@")[0];
        user.setNickname(nickname);
        user.setPassword(Utils.entryptPassword(user.getPassword())); // 密码加密
        user.setCreatedTime(new Date());
        user.setUpdatedTime(user.getCreatedTime());
        user.setStatus("0"); // 不在线

        userMapper.addUser(user);

        // 授予团队成员身份
        userMapper.grantUserRole(user.getIdUser(), 3);
        return true;
    }

    @Override
    public List<User>getTeamMembers(){
        return teamMapper.selectTeamMembers();
    }

    @Override
    public boolean markDelete(Long idUser){
        return teamMapper.markDelete(idUser);
    }

    @Override
    public boolean insertTeamMemberAdditionalInfo(TeamMemberInfo teamMemberInfo){
        return teamMapper.insertTeamMemberAdditionalInfo(teamMemberInfo);
    }
}
