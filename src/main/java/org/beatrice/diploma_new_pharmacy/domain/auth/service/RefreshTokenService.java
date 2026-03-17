package org.beatrice.diploma_new_pharmacy.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.beatrice.diploma_new_pharmacy.domain.auth.exception.InvalidTokenException;
import org.beatrice.diploma_new_pharmacy.domain.auth.exception.RevokedTokenException;
import org.beatrice.diploma_new_pharmacy.domain.auth.exception.TokenNotFoundException;
import org.beatrice.diploma_new_pharmacy.domain.auth.model.RefreshToken;
import org.beatrice.diploma_new_pharmacy.domain.auth.repository.RefreshTokenRepository;
import org.beatrice.diploma_new_pharmacy.config.JwtProperties;
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

    /**
     * @return UUID representing refresh token
     */
//    Why UUID instead of JWT? UUID refresh token is stateful.
//    I can't revoke JWT cause it's stateless but I can revoke UUID (until I store it in DB)
    public RefreshToken create(User user) {
        String token = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plus(jwtProperties.getRefreshTokenTtl(), ChronoUnit.DAYS);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(expiryDate);
        refreshToken.setRevoked(false);
        return refreshTokenRepository.save(refreshToken);

    }


    @Transactional
    public RefreshToken validate(String tokenValue) {

        int updated = refreshTokenRepository.consumeToken(tokenValue, Instant.now());

        if (updated == 0) {
            RefreshToken existing = refreshTokenRepository.findRefreshTokenByToken(tokenValue)
                    .orElseThrow(() -> new TokenNotFoundException("Refresh token not found"));

            if (existing.getRevoked()) {
                tokenRevocationService.revokeAllByUser(existing.getUser());
                throw new RevokedTokenException("Token reuse detected. Every other token will be revoked for security reasons");
            }

            if (existing.getExpiryDate().isBefore(Instant.now())) {
                throw new InvalidTokenException("Token expired");
            }

            throw new InvalidTokenException("Invalid refresh token");
        }

        return refreshTokenRepository.findRefreshTokenByToken(tokenValue).get();
    }

}
