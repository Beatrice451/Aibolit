package org.beatrice.diploma_new_pharmacy.user.repository;

import org.beatrice.diploma_new_pharmacy.user.model.UserRole;
import org.beatrice.diploma_new_pharmacy.user.model.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
}
