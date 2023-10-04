package dev.ueslei.cloakform.processor;

import dev.ueslei.cloakform.model.TerraformObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.AuthenticationManagementResource;
import org.keycloak.representations.idm.AuthenticationExecutionInfoRepresentation;
import org.keycloak.representations.idm.AuthenticationFlowRepresentation;
import org.keycloak.representations.idm.AuthenticatorConfigRepresentation;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public abstract class AbstractAuthenticationFlowProcessor<T extends TerraformObject> {

    private final Keycloak keycloak;

    public List<T> generate(String realmName, String flowAlias) {
        return keycloak.realms().realm(realmName).flows().getFlows().stream()
            .filter(f -> f.getAlias().equals(flowAlias))
            .findFirst()
            .map(flow -> generate(realmName, flow, null, null, null, 0))
            .orElse(List.of());
    }

    public List<T> generate(String realmName, AuthenticationFlowRepresentation flow, String parentFlowAlias,
        AuthenticationExecutionInfoRepresentation flowExecution, TerraformObject parentObject, int level) {
        List<T> resources = new ArrayList<>();

        if (parentObject == null) {
            var realmResource = createRealm(realmName);
            if (includeRealm()) {
                resources.add(realmResource);
            }
            parentObject = realmResource;
        }

        T flowResource = createFlow(realmName, parentFlowAlias, flow, flowExecution, parentObject);
        resources.add(flowResource);
        System.out.println("\t".repeat(level) + flowResource);

        AuthenticationManagementResource flows = keycloak.realms().realm(realmName).flows();
        flows.getExecutions(flow.getAlias()).forEach(execution -> {
            if (execution.getLevel() == 0) { // Listing only top level for each subflow to avoid duplication
                if (execution.getAuthenticationFlow() != null && execution.getAuthenticationFlow()) {
                    var subflow = flows.getFlow(execution.getFlowId());
                    var subflowResources = generate(realmName, subflow, flow.getAlias(), execution, flowResource,
                        level + 1);
                    resources.addAll(subflowResources);
                } else {
                    String flowPrefix = getFlowPrefix(flow.getAlias());
                    T executionResource = createExecution(realmName, flow.getAlias(), flowPrefix, execution,
                        flowResource);
                    resources.add(executionResource);
                    System.out.println("\t".repeat(level) + " * " + executionResource);

                    Optional<AuthenticatorConfigRepresentation> config = execution.getAuthenticationConfig() != null
                        ? Optional.of(flows.getAuthenticatorConfig(execution.getAuthenticationConfig()))
                        : Optional.empty();
                    config.ifPresent(c -> {
                        T executionConfigResource = createExecutionConfig(realmName, flowPrefix, execution, c,
                            executionResource);
                        resources.add(executionConfigResource);
                        System.out.println("\t".repeat(level) + "  \\_ " + executionConfigResource);
                    });
                }
            }
        });
        return resources;
    }

    protected abstract T createExecutionConfig(String realm, String flowPrefix,
        AuthenticationExecutionInfoRepresentation execution, AuthenticatorConfigRepresentation executionConfig,
        TerraformObject parentObject);

    protected abstract T createExecution(String realm, String parentFlowAlias, String flowPrefix,
        AuthenticationExecutionInfoRepresentation execution, TerraformObject parentObject);

    protected abstract T createFlow(String realm, String parentFlowAlias,
        AuthenticationFlowRepresentation flow, AuthenticationExecutionInfoRepresentation flowExecution,
        TerraformObject parentObject);

    protected abstract T createRealm(String realmName);

    protected abstract boolean includeRealm();

    private static String getFlowPrefix(String flowAlias) {
        String sanitizedFlowAlias = sanitizeName(flowAlias);
        String prefix = sanitizedFlowAlias.split("_").length > 1
            ? WordUtils.initials(sanitizedFlowAlias, '_')
            : sanitizedFlowAlias;
        int hashCode = Math.abs(flowAlias.hashCode());
        int fourDigitHash = hashCode % 10000;
        return StringUtils.left(prefix, 4) + "_" + fourDigitHash + "_";
    }

    protected static String sanitizeName(String alias) {
        return StringUtils.stripEnd(alias
            .replaceAll("-", "_")
            .replaceAll("[^a-zA-Z0-9]+", "_")
            .toLowerCase(), "_");
    }

}
