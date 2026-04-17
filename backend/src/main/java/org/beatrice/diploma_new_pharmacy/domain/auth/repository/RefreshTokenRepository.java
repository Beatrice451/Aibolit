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

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    Optional<RefreshToken> findRefreshTokenByToken(String token);

    void deleteRefreshTokenByUser(User user);

    List<RefreshToken> findAllByUserAndRevokedFalse(User user);

    @Modifying
    @Query("""
            UPDATE RefreshToken t
            SET t.revoked = true
            WHERE t.token = :token
            AND t.revoked = false
            AND t.expiryDate > :now
            """)
    int consumeToken(String token, Instant now);

    @Modifying
    @Query("""
            UPDATE RefreshToken t SET t.revoked = true WHERE t.token = :token
            """)
    void revokeByToken(String token);
}
