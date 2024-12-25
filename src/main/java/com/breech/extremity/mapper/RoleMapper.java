package com.breech.extremity.mapper;

import com.breech.extremity.core.mapper.Mapper;
import com.breech.extremity.dto.admin.RolesDTO;
import com.breech.extremity.dto.admin.UserRoleDTO;
import com.breech.extremity.model.Role;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RoleMapper extends Mapper<Role> {
    List<Role> selectRoleByIdUser(@Param("id") Long id);
    Role selectRoleByInputCode(@Param("inputCode") String inputCode);
    Integer updateStatus(@Param("idRole") Integer idRole, @Param("status") String status);
    Integer update(@Param("idRole") Integer idRole, @Param("name") String name, @Param("inputCode") String inputCode, @Param("weights") Integer weights);

    List<RolesDTO> getAllRoles();
    Integer getRoleByAccount(@Param("account") String account);

    Boolean activateRoleByUserId(@Param("idUser") Long idUser, @Param("idRole") Integer idRole, @Param("activated") Integer activated);

    UserRoleDTO getUserRoleByUserId(@Param("idUser") Long idUser);

    List<UserRoleDTO> getDeactivateUserRoleList(@Param("idRole") Integer idRole);

    List<UserRoleDTO> getActivateUserRoleList(@Param("idRole") Integer idRole);
}