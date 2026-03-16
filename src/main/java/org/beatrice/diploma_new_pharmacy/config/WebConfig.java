package org.beatrice.diploma_new_pharmacy.config;

import org.beatrice.diploma_new_pharmacy.cart.resolver.CartIdentityResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

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
}
