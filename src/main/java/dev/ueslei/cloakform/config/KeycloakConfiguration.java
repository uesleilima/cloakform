package dev.ueslei.cloakform.config;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KeycloakProperties.class)
public class KeycloakConfiguration {

    @Bean
    public Keycloak keycloak(KeycloakProperties properties) {
        System.out.printf("You are connecting to Keycloak at %s%n", properties.getServerUrl());
        return KeycloakBuilder.builder()
            .serverUrl(properties.getServerUrl())
            .realm(properties.getRealm())
            .clientId(properties.getClientId())
            .clientSecret(properties.getClientSecret())
            .grantType(properties.getGrantType())
            .username(properties.getUsername())
            .password(properties.getPassword())
            .authorization(properties.getAuthorization())
            .scope(properties.getScope())
            .build();
    }

}
