package org.beatrice.diploma_new_pharmacy.auth.service;

import org.beatrice.diploma_new_pharmacy.auth.exception.InvalidTokenException;
import org.beatrice.diploma_new_pharmacy.auth.exception.RevokedTokenException;
import org.beatrice.diploma_new_pharmacy.auth.exception.TokenNotFoundException;
import org.beatrice.diploma_new_pharmacy.auth.model.RefreshToken;
import org.beatrice.diploma_new_pharmacy.auth.repository.RefreshTokenRepository;
import org.beatrice.diploma_new_pharmacy.config.JwtProperties;
import org.beatrice.diploma_new_pharmacy.user.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class RefreshTokenService {


    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;
    private final TokenRevocationService tokenRevocationService;


    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtProperties jwtProperties, TokenRevocationService tokenRevocationService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProperties = jwtProperties;
        this.tokenRevocationService = tokenRevocationService;
    }

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
        RefreshToken token = refreshTokenRepository.findRefreshTokenByToken(tokenValue)
                .orElseThrow(() -> new TokenNotFoundException("Refresh token not found"));

        if (token.getRevoked()) {
            tokenRevocationService.revokeAllByUser(token.getUser());
            refreshTokenRepository.flush();
            throw new RevokedTokenException("Token revoked. Every other token will be revoked for security reasons");
        }

        if (token.getExpiryDate().isBefore(Instant.now())) {
            throw new InvalidTokenException("Token expired");
        }

        return token;
    }

}
