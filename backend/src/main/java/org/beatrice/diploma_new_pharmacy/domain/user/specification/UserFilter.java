package org.beatrice.diploma_new_pharmacy.domain.user.specification;

import org.beatrice.diploma_new_pharmacy.domain.user.model.Role;

public record UserFilter(
        String email,
        Boolean isDeleted,
        String role,
        String name
) {
}
