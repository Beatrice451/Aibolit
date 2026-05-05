package org.beatrice.diploma_new_pharmacy.domain.user.dto.response;

import java.util.Set;

public record UserResponse(
        Integer id,
        String email,
        String phone,
        Set<RoleResponse> roles,
        String firstName,
        String lastName,
        Boolean isDeleted,
        Boolean emailVerified,
        Integer pharmacyId
) {
}