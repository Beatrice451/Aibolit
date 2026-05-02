package org.beatrice.diploma_new_pharmacy.domain.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom access denied handler that returns 403 Forbidden
 * when an authenticated user lacks the required permissions.
 * 
 * This ensures proper HTTP semantics:
 * - 401 = Authentication required or failed (handled by AuthenticationEntryPoint)
 * - 403 = Authenticated but lacks permission (handled by this AccessDeniedHandler)
 */
@Slf4j
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                      HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException {
        
        log.debug("Access denied for request to {}: {}", 
                 request.getRequestURI(), 
                 accessDeniedException.getMessage());
        
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
    }
}
