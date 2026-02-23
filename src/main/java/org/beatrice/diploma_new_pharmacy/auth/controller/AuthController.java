package org.beatrice.diploma_new_pharmacy.auth.controller;

import org.beatrice.diploma_new_pharmacy.auth.SecurityUser;
import org.beatrice.diploma_new_pharmacy.auth.dto.AuthRequest;
import org.beatrice.diploma_new_pharmacy.auth.dto.AuthResponse;
import org.beatrice.diploma_new_pharmacy.auth.dto.RefreshRequest;
import org.beatrice.diploma_new_pharmacy.auth.exception.UserAlreadyExistsException;
import org.beatrice.diploma_new_pharmacy.auth.model.RefreshToken;
import org.beatrice.diploma_new_pharmacy.auth.repository.RefreshTokenRepository;
import org.beatrice.diploma_new_pharmacy.auth.service.AuthService;
import org.beatrice.diploma_new_pharmacy.auth.service.CustomUserDetailsService;
import org.beatrice.diploma_new_pharmacy.auth.service.JwtService;
import org.beatrice.diploma_new_pharmacy.auth.service.RefreshTokenService;
import org.beatrice.diploma_new_pharmacy.user.dto.UserRegistrationRequest;
import org.beatrice.diploma_new_pharmacy.user.model.User;
import org.beatrice.diploma_new_pharmacy.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {


    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;


    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            CustomUserDetailsService customUserDetailsService,
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            AuthService authService,
            RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.customUserDetailsService = customUserDetailsService;
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(request.email());
        User user = ((SecurityUser) userDetails).getUser();

        String accessToken = jwtService.generateAccessToken(user);

        RefreshToken refreshToken = refreshTokenService.create(user);

        return new AuthResponse(accessToken, refreshToken.getToken());
    }


    /**
     * Registers a new user in the system.
     *
     * <p>
     * Accepts user registration data and delegates registration procces to the authenticaton service.
     * If the user is successfully created, returns HTTP 201 (Created) with empty body.
     * </p>
     * <p>
     * If the user with the same unique credentials (e.g., email or phone) already exists,
     * returns HTTP 409 (Conflict) with an error message.
     * </p>
     *
     * @param request the registration request containing required data
     * @return a {@link ResponseEntity} with HTTP 201 if registration succeeds, HTTP 409 otherwise.
     * @see AuthService
     */
    @PostMapping("/register")
    public ResponseEntity<?> handleRegistration(@RequestBody UserRegistrationRequest request) {
        try {
            authService.registerUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /**
     * Refreshes authentication tokens using a valid refresh token.
     *
     * <p>Accepts a refresh token, revokes it, and generates a new access and refresh token pair.</p>
     * <p>If tokens updated successfully, returns HTTP 200 (OK) with the pair of tokens in the response body.</p>
     *<p>If the provided token already revoked, expired or does not exist,
     * returns HTTP 400 (Bad request) and an error message.</p>
     * @param request the request containing the refresh token
     * @return {@link ResponseEntity} containing a pair of auth tokens, if refreshed successfully. HTTP 400 and an error message
     * otherwise.
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest request) {
            AuthResponse token = authService.refresh(request.refreshToken());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(token);
    }
}
