package com.breech.extremity.service;

import com.breech.extremity.core.exception.ServiceException;
import com.breech.extremity.core.service.Service;
import com.breech.extremity.dto.admin.RolesDTO;
import com.breech.extremity.model.Role;
import com.breech.extremity.model.User;
import java.util.List;
public interface RoleService extends Service<Role> {
    List<Role> selectRoleByUser(User user);
    List<Role> findByIdUser(Long idUser);
    boolean updateStatus(Integer idRole, String status) throws ServiceException;
    boolean saveRole(Role role) throws ServiceException;

    List<RolesDTO> getAllRoles();

}