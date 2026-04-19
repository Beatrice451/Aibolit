package org.beatrice.diploma_new_pharmacy.domain.auth.repository;

import org.beatrice.diploma_new_pharmacy.domain.auth.model.RefreshToken;
import org.beatrice.diploma_new_pharmacy.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    Optional<RefreshToken> findByToken(String token);

    void deleteRefreshTokenByUser(User user);

    List<RefreshToken> findAllByUserAndRevokedFalse(User user);

    @Modifying
    @Query("""
            UPDATE RefreshToken t
            SET t.revoked = true, t.revokeReason = :reason
            WHERE t.token = :token
            AND t.revoked = false
            AND t.expiryDate > :now
            """)
    int consumeToken(String token, String reason, Instant now);

    @Query("""
            SELECT t from RefreshToken t
            WHERE t.tokenFamily = :family
            AND t.expiryDate > :now
            AND t.revoked = false
            """)
    List<RefreshToken> fintActiveByFamliy(UUID family, Instant now);

    RefreshToken findByIsCurrentTrue();

}
