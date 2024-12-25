package com.breech.extremity.mapper;

import com.breech.extremity.core.mapper.Mapper;
import com.breech.extremity.dto.TeamMemberInfoDTO;
import com.breech.extremity.model.TeamInfo;
import com.breech.extremity.model.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TeamMapper extends Mapper<TeamInfo> {
    /**
     * 获取团队信息
     * @param teamId
     * @return TeamInfo
     */
    TeamInfo selectTeamInfo(@Param("teamId") Integer teamId);
    /**
     * 更新团队信息
     * @param teamInfo
     * @return boolean
     */
    boolean updateTeamInfo(@Param("teamInfo")TeamInfo teamInfo);

    List<User> selectActivatedNormalUsers();

    List<User> selectActivatedTeamMembers();

    List<User> selectDeactivatedNormalUsers();

    List<User> selectDeactivatedTeamMembers();

    List<User>selectRefusedNormalUsers();

    List<User>selectRefusedTeamMembers();

    TeamMemberInfoDTO selectTeamMemberInfoById(@Param("teamMemberId") Integer teamMemberId);
}
