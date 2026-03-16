package org.beatrice.diploma_new_pharmacy.user.mapper;


import org.beatrice.diploma_new_pharmacy.user.dto.UserResponse;
import org.beatrice.diploma_new_pharmacy.user.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = UserRoleMapper.class)
public interface UserMapper {
    UserResponse toDto(User user);

}

