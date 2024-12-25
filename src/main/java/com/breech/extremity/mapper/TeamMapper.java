package com.breech.extremity.mapper;

import com.breech.extremity.core.mapper.Mapper;
import com.breech.extremity.model.TeamInfo;
import org.apache.ibatis.annotations.Param;

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
}
