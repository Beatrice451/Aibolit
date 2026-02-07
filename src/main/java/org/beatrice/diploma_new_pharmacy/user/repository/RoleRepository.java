package org.beatrice.diploma_new_pharmacy.user.repository;

import org.beatrice.diploma_new_pharmacy.user.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

interface RoleRepository extends JpaRepository<Role, Integer> {
}
