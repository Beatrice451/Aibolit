package org.beatrice.diploma_new_pharmacy.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.beatrice.diploma_new_pharmacy.domain.cart.resolver.CartIdentityResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final CartIdentityResolver cartIdentityResolver;

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:8080}")
    private String allowedOrigins;

    public WebConfig(CartIdentityResolver cartIdentityResolver) {
        this.cartIdentityResolver = cartIdentityResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(cartIdentityResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                log.debug("""
                                Request details:
                                  Remote Address: {}
                                  X-Forwarded-For: {}
                                  X-Real-IP: {}
                                  Host: {}
                                  Origin: {}
                                  User-Agent: {}
                                  URI: {}
                                  Method: {}
                                """,
                        request.getRemoteAddr(),
                        request.getHeader("X-Forwarded-For"),
                        request.getHeader("X-Real-IP"),
                        request.getHeader("Host"),
                        request.getHeader("Origin"),
                        request.getHeader("User-Agent"),
                        request.getRequestURI(),
                        request.getMethod());
                return true;
            }
        });
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = allowedOrigins.split(",");
        log.info("Configuring CORS with allowed origins: {}", (Object) origins);
        
        registry.addMapping("/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
