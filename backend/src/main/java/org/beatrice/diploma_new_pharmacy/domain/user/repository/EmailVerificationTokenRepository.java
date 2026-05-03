package org.beatrice.diploma_new_pharmacy.domain.user.repository;

import org.beatrice.diploma_new_pharmacy.domain.user.model.EmailVerificationToken;
import org.beatrice.diploma_new_pharmacy.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Integer> {
    Optional<EmailVerificationToken> findByTokenAndIsUsedFalse(String token);

    @Modifying
    @Query("""
                    DELETE FROM EmailVerificationToken t
                    WHERE t.expiresAt < :now
            """)
    void deleteExpiredTokens(@Param("now") Instant now);

    @Modifying
    @Query("""
                    UPDATE EmailVerificationToken
                    SET isUsed = true
                    WHERE user = :user
            """)
    void invalidateAllUserTokens(@Param("user") User user);

    Optional<EmailVerificationToken> findFirstByUserAndIsUsedFalseOrderByCreatedAtDesc(User user);
}
