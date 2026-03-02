package org.beatrice.diploma_new_pharmacy.auth.service;

import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.auth.model.RefreshToken;
import org.beatrice.diploma_new_pharmacy.auth.repository.RefreshTokenRepository;
import org.beatrice.diploma_new_pharmacy.user.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис, отвечающий за отзыв refresh-токенов. Почему эта логика вынесена в отдельный класс,
 * а не реализована в {@link RefreshTokenService}? Транзакции, прокси, АОП и прочие страшные слова.
 * Так надо.
 *
 */
@RequiredArgsConstructor
@Service
public class TokenRevocationService {
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Отзывает (инвалидирует) указанный токен
     * @param token токен, который требуется отозвать
     */
    public void revoke(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    /**
     * Отзывает все refresh-токены указанного пользователя
     * @param user пользователь, чьи токены требуется отозвать
     */
    @Transactional
    public void revokeAllByUser(User user) {
        refreshTokenRepository.findAllByUserAndRevokedFalse(user)
                .forEach(this::revoke);
        refreshTokenRepository.flush();
    }
}
