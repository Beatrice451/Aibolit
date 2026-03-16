package org.beatrice.diploma_new_pharmacy.user.dto;

import java.time.Instant;

public record UserRoleResponse(
        String roleId,
        String roleName,
        Instant assignedAt
) {
}
