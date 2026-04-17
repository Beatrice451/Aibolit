package org.beatrice.diploma_new_pharmacy.domain.user.mapper;

import org.beatrice.diploma_new_pharmacy.domain.user.dto.UserRoleResponse;
import org.beatrice.diploma_new_pharmacy.domain.user.model.UserRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserRoleMapper {
    @Mapping(source = "role.id", target = "roleId")
    @Mapping(source = "role.roleName", target = "roleName")
    @Mapping(source = "assignedAt", target = "assignedAt")
    UserRoleResponse toDto(UserRole userRole);
}
