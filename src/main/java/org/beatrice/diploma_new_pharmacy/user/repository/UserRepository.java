package org.beatrice.diploma_new_pharmacy.user.repository;

import org.beatrice.diploma_new_pharmacy.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
}

