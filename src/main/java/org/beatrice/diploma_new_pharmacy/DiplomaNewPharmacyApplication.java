package org.beatrice.diploma_new_pharmacy;

import org.beatrice.diploma_new_pharmacy.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(JwtProperties.class)
@SpringBootApplication
public class DiplomaNewPharmacyApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiplomaNewPharmacyApplication.class, args);
    }

}
