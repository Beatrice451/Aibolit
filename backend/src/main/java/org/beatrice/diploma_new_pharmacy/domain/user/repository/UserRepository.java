package org.beatrice.diploma_new_pharmacy.domain.user.repository;

import org.beatrice.diploma_new_pharmacy.domain.user.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findUserByEmail(String email);

    // Used by Spring Security.
    // Loads user AND roles to avoid LazyInitializationException
    // Since roles has lazy fetch type (but actually EAGER now), transaction would be closed by the time Security tries to access roles
    // which would cause an exception
    @EntityGraph(attributePaths = {"userRoles"})
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findUserByEmailWithRoles(@Param("email") String email);

    Optional<User> findUserByPhone(String phone);

    boolean existsUserByPhone(String phone);

    boolean existsByEmail(String email);
}

