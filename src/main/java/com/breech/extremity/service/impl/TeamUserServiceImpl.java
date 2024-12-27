package com.breech.extremity.service.impl;

import com.breech.extremity.core.service.AbstractService;
import com.breech.extremity.mapper.TeamUserMapper;
import com.breech.extremity.model.TeamMemberInfo;
import com.breech.extremity.service.TeamUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
@Service
public class TeamUserServiceImpl extends AbstractService<TeamMemberInfo> implements TeamUserService {

    @Resource
    TeamUserMapper teamUserMapper;

    @Override
    public List<TeamMemberInfo> getAllTeamMember(){
        return teamUserMapper.selectAll();
    }
    @Override
    public TeamMemberInfo getTeamMemberInfo(int id){
        return teamUserMapper.selectByPrimaryKey(id);
    }
    @Override
    public  void updateTeamMemberInfo(TeamMemberInfo teamMemberInfo){
        teamUserMapper.updateByPrimaryKeySelective(teamMemberInfo);
    }
}
