package org.beatrice.diploma_new_pharmacy.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.beatrice.diploma_new_pharmacy.domain.cart.resolver.CartIdentityResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final CartIdentityResolver cartIdentityResolver;


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
                log.debug("Request details:");
                log.debug("  Remote Address: {}", request.getRemoteAddr());
                log.debug("  X-Forwarded-For: {}", request.getHeader("X-Forwarded-For"));
                log.debug("  X-Real-IP: {}", request.getHeader("X-Real-IP"));
                log.debug("  Host: {}", request.getHeader("Host"));
                log.debug("  User-Agent: {}", request.getHeader("User-Agent"));
                return true;
            }
        });
    }
}
