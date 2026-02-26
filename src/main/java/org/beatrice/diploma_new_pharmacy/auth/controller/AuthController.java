package org.beatrice.diploma_new_pharmacy.auth.controller;

import org.beatrice.diploma_new_pharmacy.auth.dto.AuthRequest;
import org.beatrice.diploma_new_pharmacy.auth.dto.AuthResponse;
import org.beatrice.diploma_new_pharmacy.auth.exception.UserAlreadyExistsException;
import org.beatrice.diploma_new_pharmacy.auth.service.AuthService;
import org.beatrice.diploma_new_pharmacy.auth.util.RefreshCookieFactory;
import org.beatrice.diploma_new_pharmacy.user.dto.UserRegistrationRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {


    private final AuthService authService;
    private final RefreshCookieFactory refreshCookieFactory;


    public AuthController(
            AuthService authService, RefreshCookieFactory refreshCookieFactory) {
        this.authService = authService;
        this.refreshCookieFactory = refreshCookieFactory;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        AuthResponse authResponse = authService.login(request);

        ResponseCookie cookie = refreshCookieFactory.create(authResponse.refreshToken());

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthResponse(authResponse.accessToken(), null));
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
    public ResponseEntity<?> register(@RequestBody UserRegistrationRequest request) {
        try {
            authService.registerUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }


    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@CookieValue("refreshToken") String refreshToken) {
        AuthResponse token = authService.refresh(refreshToken);

        ResponseCookie cookie = refreshCookieFactory.create(token.refreshToken());

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthResponse(token.accessToken(), null));
    }
}
