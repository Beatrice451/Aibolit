package org.beatrice.diploma_new_pharmacy.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.beatrice.diploma_new_pharmacy.config.JwtProperties;
import org.beatrice.diploma_new_pharmacy.user.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    private final Key key;

    private final JwtProperties props;

    public JwtService(JwtProperties props) {
        this.props = props;
        this.key = Keys.hmacShaKeyFor(
                props.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(Date.from(
                        Instant.now().plus(
                                props.getAccessTokenTtl(),
                                ChronoUnit.MINUTES
                        )
                ))
                .claim("roles", user.getUserRoles()
                        .stream()
                        .map(ur -> "ROLE_" + ur.getRole().getRoleName())
                        .toList())
                .signWith(key)
                .compact();
    }


    /**
     * @param token    JWT token to extract claim from
     * @param resolver function to use to extract the claim (e.g., Claims::getExpiration())
     * @return extracted claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }


    /**
     * @param token JWT token to extract all claims from
     * @return extracted Claims object
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * @param token JWT token
     * @return boolean representing token expiration fact
     */
    private boolean isExpired(String token) {
        return extractExpiration(token).before(new Date());
    }


    /**
     * @param token JWT token
     * @return token expiration date
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenValid(String token, UserDetails user) {
        try {
            return !isExpired(token) && user.getUsername().equals(extractUsername(token));
        } catch (JwtException | IllegalArgumentException _) {
            return false;
        }
    }
}
