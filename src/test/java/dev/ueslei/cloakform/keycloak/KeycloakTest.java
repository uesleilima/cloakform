package dev.ueslei.cloakform.keycloak;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.AuthenticationManagementResource;
import org.keycloak.representations.idm.AuthenticatorConfigRepresentation;

public class KeycloakTest {

    @Test
    void printAuthenticationFlowsTest() {
        var keycloak = createKeycloak();
        var flows = keycloak.realm("customer").flows();
        flows.getFlows().forEach(f -> printFlow(flows, f.getAlias(), 0));
    }

    static void printFlow(AuthenticationManagementResource flows, String flowAlias, int level) {
        String tab = "\t";
        System.out.println(tab.repeat(level) + flowAlias);
        flows.getExecutions(flowAlias).forEach(execution -> {
            if (execution.getLevel() == 0) {
                if (execution.getAuthenticationFlow() != null && execution.getAuthenticationFlow()) {
                    printFlow(flows, execution.getDisplayName(), level + 1);
                } else {
                    Optional<AuthenticatorConfigRepresentation> config = execution.getAuthenticationConfig() != null
                        ? Optional.of(flows.getAuthenticatorConfig(execution.getAuthenticationConfig()))
                        : Optional.empty();
                    System.out.println(
                        tab.repeat(level) + " * " + execution.getProviderId() +
                            config.map(c -> " [" + c.getAlias() + "]").orElse(""));
                }
            }
        });
    }

    static Keycloak createKeycloak() {
        return KeycloakBuilder.builder()
            .serverUrl("http://localhost:8181/auth")
            .realm("master")
            .clientId("admin-cli")
            .grantType("password")
            .username("admin")
            .password("admin")
            .build();
    }


}
