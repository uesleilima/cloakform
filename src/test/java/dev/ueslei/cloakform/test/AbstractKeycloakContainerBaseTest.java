package dev.ueslei.cloakform.test;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public abstract class AbstractKeycloakContainerBaseTest {

    static final KeycloakContainer keycloak = new KeycloakContainer()
        .withRealmImportFile("cloakform-realm.json");

    static {
        keycloak.start();
    }

    @DynamicPropertySource
    static void keycloakProperties(DynamicPropertyRegistry registry) {
        registry.add("keycloak.serverUrl", keycloak::getAuthServerUrl);
        registry.add("keycloak.username", keycloak::getAdminUsername);
        registry.add("keycloak.password", keycloak::getAdminPassword);
    }
}
