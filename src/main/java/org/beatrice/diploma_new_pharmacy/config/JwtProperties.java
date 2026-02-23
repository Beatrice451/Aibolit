package org.beatrice.diploma_new_pharmacy.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;


@ConfigurationProperties(prefix = "jwt")
@Validated
public class JwtProperties {
    @NotBlank
    private String secret;

    @Min(1)
    private long accessTokenTtl;

    @Min(1)
    private long refreshTokenTtl;

    public String getSecret() {
        return secret;
    }

    public long getAccessTokenTtl() {
        return accessTokenTtl;
    }

    public long getRefreshTokenTtl() {
        return refreshTokenTtl;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public void setAccessTokenTtl(long accessTokenTtl) {
        this.accessTokenTtl = accessTokenTtl;
    }

    public void setRefreshTokenTtl(long refreshTokenTtl) {
        this.refreshTokenTtl = refreshTokenTtl;
    }
}
