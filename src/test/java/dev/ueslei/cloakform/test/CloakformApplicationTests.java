package dev.ueslei.cloakform.test;

import static org.awaitility.Awaitility.await;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
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

    static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

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
            .nonInterative("realm", "imports", "cloakform", TEMP_DIR + "realm_imports.tf")
            .run();

        await().atMost(2, TimeUnit.SECONDS)
            .untilAsserted(() -> ShellAssertions.assertThat(session.screen()).containsText("keycloak_realm.cloakform"));
    }

    @Test
    void clientImportsTest() {
        NonInteractiveShellSession session = shell
            .nonInterative("client", "imports", "cloakform", "cloakform", TEMP_DIR + "client_imports.tf")
            .run();

        await().atMost(2, TimeUnit.SECONDS)
            .untilAsserted(
                () -> ShellAssertions.assertThat(session.screen()).containsText("keycloak_openid_client.cloakform"));
    }

    @Test
    void clientImportsInvalidRealmTest() {
        NonInteractiveShellSession session = shell
            .nonInterative("client", "imports", "invalid", "cloakform", TEMP_DIR + "client_imports.tf")
            .run();

        await().atMost(2, TimeUnit.SECONDS)
            .untilAsserted(() -> ShellAssertions.assertThat(session.screen()).containsText("Realm invalid not found"));
    }

    @Test
    void flowImportsTest() {
        NonInteractiveShellSession session = shell
            .nonInterative("flow", "imports", "cloakform", "browser", TEMP_DIR + "flow_imports.tf")
            .run();

        await().atMost(2, TimeUnit.SECONDS)
            .untilAsserted(() -> ShellAssertions.assertThat(session.screen())
                .containsText("keycloak_authentication_flow.browser"));
    }

    @Test
    void flowImportsInvalidRealmTest() {
        NonInteractiveShellSession session = shell
            .nonInterative("flow", "imports", "invalid", "browser", TEMP_DIR + "flow_imports.tf")
            .run();

        await().atMost(2, TimeUnit.SECONDS)
            .untilAsserted(() -> ShellAssertions.assertThat(session.screen()).containsText("Realm invalid not found"));
    }

    @Test
    void flowResourcesTest() {
        NonInteractiveShellSession session = shell
            .nonInterative("flow", "resources", "cloakform", "browser", TEMP_DIR + "flow_resources.tf")
            .run();

        await().atMost(2, TimeUnit.SECONDS)
            .untilAsserted(() -> ShellAssertions.assertThat(session.screen())
                .containsText("keycloak_authentication_flow.browser"));
    }

    @AfterEach
    void print() {
        shell.screen().lines().forEach(System.out::println);
    }

}
