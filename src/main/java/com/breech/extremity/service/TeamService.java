package com.breech.extremity.service;

import com.breech.extremity.core.exception.ServiceException;
import com.breech.extremity.dto.TeamMemberInfoDTO;
import com.breech.extremity.model.TeamInfo;
import com.breech.extremity.model.TeamMemberInfo;
import com.breech.extremity.model.User;

import java.util.List;

public interface TeamService {

    TeamInfo selectTeamInfo(Integer teamId) throws ServiceException;

    boolean updateTeamInfo(TeamInfo teamInfo) throws ServiceException;

    List<User> getActivatedNormalUsers()throws ServiceException;

    List<User>getActivatedTeamMembers()throws ServiceException;

    List<User> getDeactivatedNormalUsers()throws ServiceException;

    List<User>getDeactivatedTeamMembers()throws ServiceException;

    List<User>getRefusedNormalUsers()throws ServiceException;

    List<User>getRefusedTeamMembers()throws ServiceException;

    List<User>getDeleteUsers()throws ServiceException;

    TeamMemberInfoDTO getTeamMemberInfo(Integer teamMemberId) throws ServiceException;

    boolean editTeamMemberInfo(TeamMemberInfoDTO teamMemberInfoDTO) throws ServiceException;

    String addTeamMemberAccount(User user);

    boolean insertTeamMemberAdditionalInfo(TeamMemberInfo teamMemberInfo) throws ServiceException;

    boolean markDelete(Long idUser) throws ServiceException;

    List<User>getTeamMembers();
}
