package org.beatrice.diploma_new_pharmacy.auth.service;

import org.beatrice.diploma_new_pharmacy.auth.model.RefreshToken;
import org.beatrice.diploma_new_pharmacy.auth.repository.RefreshTokenRepository;
import org.beatrice.diploma_new_pharmacy.user.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenRevocationService {
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenRevocationService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public void revoke(RefreshToken token) {
        token.setRevoked(true);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeAllByUser(User user) {
        refreshTokenRepository.findAllByUserAndRevokedFalse(user)
                .forEach(this::revoke);
        refreshTokenRepository.flush();
    }
}
