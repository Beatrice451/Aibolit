package org.beatrice.diploma_new_pharmacy.domain.user.mapper;


import org.beatrice.diploma_new_pharmacy.domain.user.dto.UserResponse;
import org.beatrice.diploma_new_pharmacy.domain.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "roles", source = "userRoles")
    UserResponse toDto(User user);

}

