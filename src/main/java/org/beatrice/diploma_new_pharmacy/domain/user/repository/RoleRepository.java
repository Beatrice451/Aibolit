package org.beatrice.diploma_new_pharmacy.domain.user.repository;

import org.beatrice.diploma_new_pharmacy.domain.user.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleName(String roleName);
}
