package com.breech.extremity.service.impl;

import com.breech.extremity.core.service.AbstractService;
import com.breech.extremity.mapper.RoleMapper;
import com.breech.extremity.mapper.TeamMapper;
import com.breech.extremity.mapper.UserMapper;
import com.breech.extremity.model.Role;
import com.breech.extremity.model.TeamInfo;
import com.breech.extremity.service.RoleService;
import com.breech.extremity.service.TeamService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class TeamServiceImpl extends AbstractService<TeamInfo> implements TeamService {
    @Resource
    private RoleMapper roleMapper;

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
}
