package dev.ueslei.cloakform.keycloak;

import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.AuthenticationManagementResource;

public class KeycloakTest {

    @Test
    void printAuthenticationFlowsTest() {
        var keycloak = createKeycloak();
        var flows = keycloak.realm("customer").flows();
        flows.getFlows().forEach(f -> {
            System.out.println(f.getAlias() + ": " + f.getProviderId());
            printFlow(flows, f.getAlias(), 1);
        });
    }

    static void printFlow(AuthenticationManagementResource flows, String alias, int level) {
        String tab = "\t";
        flows.getExecutions(alias).forEach(e -> {
            if (e.getLevel() == 0) {
                if (e.getAuthenticationFlow() != null && e.getAuthenticationFlow()) {
                    var subflow = flows.getFlow(e.getFlowId());
                    System.out.println(tab.repeat(level) + subflow.getAlias() + ": " + subflow.getProviderId());
                    printFlow(flows, subflow.getAlias(), level + 1);
                } else {
                    System.out.println(tab.repeat(level) + "* " + e.getProviderId());
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
