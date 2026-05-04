package org.beatrice.diploma_new_pharmacy.domain.user.dto.response;

import java.time.Instant;

public record UserRoleResponse(
        String roleId,
        String roleName,
        Instant assignedAt
) {
}
