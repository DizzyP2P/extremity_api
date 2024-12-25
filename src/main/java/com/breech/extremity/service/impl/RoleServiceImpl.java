package com.breech.extremity.service.impl;

import com.breech.extremity.core.exception.ServiceException;
import com.breech.extremity.core.service.AbstractService;
import com.breech.extremity.dto.admin.RolesDTO;
import com.breech.extremity.dto.admin.UserRoleDTO;
import com.breech.extremity.mapper.ArticleMapper;
import com.breech.extremity.mapper.RoleMapper;
import com.breech.extremity.model.Role;
import com.breech.extremity.model.User;
import com.breech.extremity.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
@Service
public class RoleServiceImpl extends AbstractService<Role> implements RoleService {
    @Resource
    private RoleMapper roleMapper;

    @Override
    public List<Role> selectRoleByUser(User sysUser) {
        return roleMapper.selectRoleByIdUser(sysUser.getIdUser());
    }

    @Override
    public List<Role> findByIdUser(Long idUser) {
        return roleMapper.selectRoleByIdUser(idUser);
    }

    @Override
    public UserRoleDTO getUserRoleByUserId(Long idUser){
        return roleMapper.getUserRoleByUserId(idUser);
    }

    @Override
    public List<UserRoleDTO> getDeactivateUserRoleList(Integer idRole){
        return roleMapper.getDeactivateUserRoleList(idRole);
    }

    @Override
    public List<UserRoleDTO> getActivateUserRoleList(Integer idRole){
        return roleMapper.getActivateUserRoleList(idRole);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Integer idRole, String status) throws ServiceException {
        Integer result = roleMapper.updateStatus(idRole, status);
        if (result == 0) {
            throw new ServiceException("更新失败");
        }
        return true;
    }

    @Override
    public boolean saveRole(Role role) throws ServiceException {
        Integer result;
        if (role.getIdRole() == null) {
            role.setCreatedTime(new Date());
            role.setUpdatedTime(role.getCreatedTime());
            result = roleMapper.insertSelective(role);
        } else {
            role.setCreatedTime(new Date());
            result = roleMapper.update(role.getIdRole(), role.getName(), role.getInputCode(), role.getWeights());
        }
        if (result == 0) {
            throw new ServiceException("操作失败!");
        }
        return true;
    }

    @Override
    public List<RolesDTO> getAllRoles() throws ServiceException{
        return roleMapper.getAllRoles();
    }

    @Override
    public Integer getRoleIdByAccount(String account) throws ServiceException{
        return roleMapper.getRoleByAccount(account);
    }

    @Override
    public boolean activateRoleByUserId(Long idUser, Integer idRole, Integer activated) throws ServiceException{
        return roleMapper.activateRoleByUserId(idUser, idRole, activated);
    }
}
