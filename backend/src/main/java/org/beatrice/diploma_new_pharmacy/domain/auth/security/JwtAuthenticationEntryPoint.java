package org.beatrice.diploma_new_pharmacy.domain.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom authentication entry point that returns 401 Unauthorized
 * when authentication is missing or invalid (e.g., expired JWT token).
 * 
 * This ensures proper HTTP semantics:
 * - 401 = Authentication required or failed
 * - 403 = Authenticated but lacks permission
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException {
        
        log.debug("Authentication failed for request to {}: {}", 
                 request.getRequestURI(), 
                 authException.getMessage());
        
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
}
