package org.beatrice.diploma_new_pharmacy.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.config.JwtProperties;
import org.beatrice.diploma_new_pharmacy.domain.auth.exception.InvalidTokenException;
import org.beatrice.diploma_new_pharmacy.domain.auth.exception.RevokedTokenException;
import org.beatrice.diploma_new_pharmacy.domain.auth.exception.TokenNotFoundException;
import org.beatrice.diploma_new_pharmacy.domain.auth.model.RefreshToken;
import org.beatrice.diploma_new_pharmacy.domain.auth.repository.RefreshTokenRepository;
import org.beatrice.diploma_new_pharmacy.domain.user.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;
    private final TokenRevocationService tokenRevocationService;

    @Transactional
    public RefreshToken create(User user) {
        return create(user, null);
    }

    @Transactional
    public RefreshToken create(User user, UUID family) {
        String token = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plus(jwtProperties.getRefreshTokenTtl(), ChronoUnit.DAYS);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setTokenFamily(family != null ? family : UUID.randomUUID());
        refreshToken.setExpiryDate(expiryDate);
        refreshToken.setRevoked(false);
        refreshToken.setUser(user);
        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Атомарный refresh токена.
     * consumeToken атомарно помечает токен как revoked.
     * Если updated == 0 - токен уже был использован (race condition).
     */
    @Transactional
    public RefreshToken replaceToken(String previousTokenValue) {
        // АТОМНАЯ операция: сразу помечаем токен revoked
        int updated = refreshTokenRepository.consumeToken(previousTokenValue, "Token replaced", Instant.now());

        if (updated == 0) {
            // Токен уже был использован — race condition detected!
            RefreshToken token = refreshTokenRepository.findByToken(previousTokenValue)
                    .orElseThrow(() -> new TokenNotFoundException("Refresh token not found"));

            // Если токен уже revoked — это reuse атака
            if (token.getRevoked()) {
                tokenRevocationService.revokeFamily(token.getTokenFamily(), "Reuse attack");
                throw new RevokedTokenException("Token reuse detected. All tokens in family are revoked.");
            }

            // Если не истёк — неизвестная ошибка
            if (token.getExpiryDate().isBefore(Instant.now())) {
                throw new InvalidTokenException("Token expired");
            }

            throw new InvalidTokenException("Invalid refresh token");
        }

        // Токен успешно помечен revoked, продолжаем
        RefreshToken previousToken = refreshTokenRepository.findByToken(previousTokenValue).get();
        previousToken.setIsCurrent(false);
        refreshTokenRepository.save(previousToken);

        // Создаём новый токен в той же семье
        RefreshToken newToken = create(previousToken.getUser(), previousToken.getTokenFamily());
        newToken.setIsCurrent(true);
        refreshTokenRepository.save(newToken);

        // Связываем old -> new
        previousToken.setReplacedBy(newToken);
        refreshTokenRepository.save(previousToken);

        return newToken;
    }
}