package org.beatrice.diploma_new_pharmacy.auth.repository;

import org.beatrice.diploma_new_pharmacy.auth.model.RefreshToken;
import org.beatrice.diploma_new_pharmacy.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    Optional<RefreshToken> findRefreshTokenByToken(String token);

    void deleteRefreshTokenByUser(User user);

    List<RefreshToken> findAllByUserAndRevokedFalse(User user);
}
