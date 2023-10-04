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
            .map(flow -> generate(realmName, flow.getAlias(), flow.getId(), null, null, null, 0))
            .orElse(List.of());
    }

    public List<T> generate(String realmName, String flowAlias, String flowId, String parentFlowAlias,
        AuthenticationExecutionInfoRepresentation flowExecution, TerraformObject parentObject, int level) {
        List<T> resources = new ArrayList<>();

        if (parentObject == null) {
            var realmResource = createRealm(realmName);
            if (includeRealm()) {
                resources.add(realmResource);
            }
            parentObject = realmResource;
        }

        AuthenticationManagementResource flows = keycloak.realms().realm(realmName).flows();

        T flowResource = createFlow(realmName, flowId, flowAlias, parentFlowAlias, flowExecution, parentObject);
        resources.add(flowResource);
        System.out.println("\t".repeat(level) + flowResource);

        flows.getExecutions(flowAlias).forEach(execution -> {
            if (execution.getLevel() == 0) { // Listing only top level for each subflow to avoid duplication
                if (execution.getAuthenticationFlow() != null && execution.getAuthenticationFlow()) {
                    var subflowResources = generate(realmName, execution.getDisplayName(), execution.getFlowId(),
                        flowAlias,
                        execution, flowResource, level + 1);
                    resources.addAll(subflowResources);
                } else {
                    String flowPrefix = getFlowPrefix(flowAlias);
                    T executionResource = createExecution(realmName, flowAlias, flowPrefix, execution, flowResource);
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

    protected abstract T createFlow(String realm, String flowId, String flowAlias, String parentFlowAlias,
        AuthenticationExecutionInfoRepresentation flowExecution, TerraformObject parentObject);

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
