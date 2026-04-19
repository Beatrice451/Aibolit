package org.beatrice.diploma_new_pharmacy.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.auth.exception.TokenNotFoundException;
import org.beatrice.diploma_new_pharmacy.domain.auth.model.RefreshToken;
import org.beatrice.diploma_new_pharmacy.domain.auth.repository.RefreshTokenRepository;
import org.beatrice.diploma_new_pharmacy.domain.user.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Сервис, отвечающий за отзыв refresh-токенов. Почему эта логика вынесена в отдельный класс,
 * а не реализована в {@link RefreshTokenService}? Транзакции, прокси, АОП и прочие страшные слова.
 * Так надо.
 *
 */
@RequiredArgsConstructor
@Transactional
@Service
public class TokenRevocationService {
    private final RefreshTokenRepository refreshTokenRepository;

    public void revoke(RefreshToken token) {
        revoke(token, null);
    }


    public void revoke(RefreshToken token, String reason) {
        revoke(token, reason, null);
    }

    public void revoke(RefreshToken token, String reason, RefreshToken replacedBy) {
        token.setRevokeReason(reason);
        token.setRevoked(true);
        token.setReplacedBy(replacedBy);
        refreshTokenRepository.save(token);
    }

    public void revokeByTokenValue(String tokenValue, String reason) {
        var token = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new TokenNotFoundException("Refresh token not found"));

        revoke(token, reason);
    }


    public void revokeAllByUser(User user) {
        revokeAllByUser(user, null);
    }

    public void revokeAllByUser(User user, String reason) {
        refreshTokenRepository.findAllByUserAndRevokedFalse(user)
                .forEach(t -> revoke(t, reason));
    }



    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeFamily(UUID family, String reason) {
        refreshTokenRepository.fintActiveByFamliy(family, Instant.now())
                .forEach(t -> {

                    IO.println(t.getRevoked());
                    revoke(t, reason);
                    IO.println(t.getRevoked());
                });
    }
}
