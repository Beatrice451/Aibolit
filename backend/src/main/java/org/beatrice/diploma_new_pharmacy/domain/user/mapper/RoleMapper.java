package org.beatrice.diploma_new_pharmacy.domain.user.mapper;

import org.beatrice.diploma_new_pharmacy.domain.user.dto.RoleResponse;
import org.beatrice.diploma_new_pharmacy.domain.user.model.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleResponse toDto(Role role);
}