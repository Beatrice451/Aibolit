package org.beatrice.diploma_new_pharmacy.user.dto;

import java.util.Set;

public record UserResponse(
        Integer id,
        String email,
        String phone,
        Set<UserRoleResponse> userRoles
) {
}
