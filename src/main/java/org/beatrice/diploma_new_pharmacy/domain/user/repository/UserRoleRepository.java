package org.beatrice.diploma_new_pharmacy.domain.user.repository;

import org.beatrice.diploma_new_pharmacy.domain.user.model.UserRole;
import org.beatrice.diploma_new_pharmacy.domain.user.model.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
}
