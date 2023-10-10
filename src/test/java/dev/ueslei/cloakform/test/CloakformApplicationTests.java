package dev.ueslei.cloakform.test;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
class CloakformApplicationTests {

    @Container
    static KeycloakContainer keycloak = new KeycloakContainer();

    @DynamicPropertySource
    static void keycloakProperties(DynamicPropertyRegistry registry) {
        registry.add("keycloak.serverUrl", keycloak::getAuthServerUrl);
        registry.add("keycloak.username", keycloak::getAdminUsername);
        registry.add("keycloak.password", keycloak::getAdminPassword);
    }

    @Autowired
    Keycloak client;

    @Test
    void contextLoads() {
        Assertions.assertEquals(client.serverInfo().getInfo().getSystemInfo().getVersion(), "22.0.4", "Wrong version");
    }

}
