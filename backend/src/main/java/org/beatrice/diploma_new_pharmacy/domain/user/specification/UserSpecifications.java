package org.beatrice.diploma_new_pharmacy.domain.user.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.beatrice.diploma_new_pharmacy.domain.user.model.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications {
    public static Specification<User> hasEmail(String email) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + email.toLowerCase() + "%");
    }

    public static Specification<User> hasRole(String roleName) {
        return (root, query, criteriaBuilder) -> {
            Join<Object, Object> rolesJoin = root.join("userRoles", JoinType.INNER);
            return criteriaBuilder.equal(criteriaBuilder.lower(rolesJoin.get("roleName")), roleName.toLowerCase());
        };
    }

    public static Specification<User> hasIsDeleted(Boolean isDeleted) {
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("isDeleted"), isDeleted));
    }

    public static Specification<User> hasNameLike(String name) {
        return((root, query, criteriaBuilder) -> {
            if (name == null || name.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String pattern = "%" + name.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), pattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), pattern)
            );
        });
    }
}
