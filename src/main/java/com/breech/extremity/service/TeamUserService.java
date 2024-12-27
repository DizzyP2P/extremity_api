package com.breech.extremity.service;

import com.breech.extremity.core.service.Service;
import com.breech.extremity.model.TeamMemberInfo;


import java.util.List;
public interface TeamUserService extends Service<TeamMemberInfo> {
    List<TeamMemberInfo> getAllTeamMember();
    TeamMemberInfo getTeamMemberInfo(int id);
    void updateTeamMemberInfo(TeamMemberInfo teamMemberInfo);
}
