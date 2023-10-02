package dev.ueslei.cloakform.keycloak;

import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.AuthenticationManagementResource;
import org.keycloak.representations.idm.AuthenticatorConfigRepresentation;

public class KeycloakTest {

    @ParameterizedTest
    @ValueSource(strings = {"customer"})
    void printAuthenticationFlowsTest(String realmName) {
        var keycloak = createKeycloak();
        var flows = keycloak.realm(realmName).flows();
        flows.getFlows().forEach(f -> printFlow(flows, realmName, f.getId(), f.getAlias(), null, 0));
    }

    static void printFlow(AuthenticationManagementResource flows, String realm, String flowId,
        String flowAlias, String parentFlowAlias, int level) {

        String flowIdTerraform = parentFlowAlias == null
            ? String.format("%s/%s", realm, flowId)
            : String.format("%s/%s/%s", realm, parentFlowAlias, flowId);
        System.out.println("\t".repeat(level) + flowAlias + " [" + flowIdTerraform + "]");

        flows.getExecutions(flowAlias).forEach(execution -> {
            if (execution.getLevel() == 0) { // Listing only top level for each subflow to avoid duplication
                if (execution.getAuthenticationFlow() != null && execution.getAuthenticationFlow()) {
                    printFlow(flows, realm, execution.getId(), execution.getDisplayName(), flowAlias, level + 1);
                } else {

                    String executionIdTerraform = String.format("%s/%s/%s", realm, flowAlias, execution.getId());
                    System.out.println(
                        "\t".repeat(level) + " * " + execution.getProviderId() + " [" + executionIdTerraform + "]");

                    Optional<AuthenticatorConfigRepresentation> config = execution.getAuthenticationConfig() != null
                        ? Optional.of(flows.getAuthenticatorConfig(execution.getAuthenticationConfig()))
                        : Optional.empty();

                    config.ifPresent(c -> {
                        String configExecutionIdTerraform = String.format("%s/%s/%s", realm, execution.getId(), c.getId());
                        System.out.println("\t".repeat(level) + "  \\_ " + c.getAlias() + " [" + configExecutionIdTerraform + "]");
                    });
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
