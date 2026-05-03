package org.beatrice.diploma_new_pharmacy.domain.user.mapper;


import org.beatrice.diploma_new_pharmacy.domain.user.dto.response.UserResponse;
import org.beatrice.diploma_new_pharmacy.domain.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "roles", source = "userRoles")
    @Mapping(target = "emailVerified", source = "emailVerified")
    UserResponse toDto(User user);

}

