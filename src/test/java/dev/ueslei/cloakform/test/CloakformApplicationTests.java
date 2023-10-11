package dev.ueslei.cloakform.test;

import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.shell.test.ShellAssertions;
import org.springframework.shell.test.ShellTestClient;
import org.springframework.shell.test.ShellTestClient.NonInteractiveShellSession;
import org.springframework.shell.test.autoconfigure.AutoConfigureShell;
import org.springframework.shell.test.autoconfigure.AutoConfigureShellTestClient;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@AutoConfigureShell
@AutoConfigureShellTestClient
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class CloakformApplicationTests {

    @Container
    static KeycloakContainer keycloak = new KeycloakContainer().withRealmImportFile("cloakform-realm.json");

    @DynamicPropertySource
    static void keycloakProperties(DynamicPropertyRegistry registry) {
        registry.add("keycloak.serverUrl", keycloak::getAuthServerUrl);
        registry.add("keycloak.username", keycloak::getAdminUsername);
        registry.add("keycloak.password", keycloak::getAdminPassword);
    }

    @Autowired
    ShellTestClient shell;

    @Test
    void realmImportsTest() {
        NonInteractiveShellSession session = shell
            .nonInterative("realm", "imports", "cloakform", "/tmp/realm_imports.tf")
            .run();

        await().atMost(2, TimeUnit.SECONDS)
            .untilAsserted(() -> ShellAssertions.assertThat(session.screen()).containsText("keycloak_realm.cloakform"));
    }

}
