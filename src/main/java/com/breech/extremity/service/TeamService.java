package com.breech.extremity.service;

import com.breech.extremity.core.exception.ServiceException;
import com.breech.extremity.model.TeamInfo;

public interface TeamService {

    TeamInfo selectTeamInfo(Integer teamId) throws ServiceException;

    boolean updateTeamInfo(TeamInfo teamInfo) throws ServiceException;
}
