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

    public List<T> generate(String realm, String flowAlias) {
        return keycloak.realms().realm(realm).flows().getFlows().stream()
            .filter(f -> f.getAlias().equals(flowAlias))
            .findFirst()
            .map(flow -> generate(realm, flow.getAlias(), flow.getId(), null, null, 0))
            .orElse(List.of());
    }

    public List<T> generate(String realm, String flowAlias, String flowId, String parentFlowAlias,
        TerraformObject parentResource, int level) {
        List<T> resources = new ArrayList<>();
        AuthenticationManagementResource flows = keycloak.realms().realm(realm).flows();

        T flowResource = createFlow(realm, flowId, flowAlias, parentFlowAlias, parentResource);
        resources.add(flowResource);
        System.out.println("\t".repeat(level) + flowResource);

        flows.getExecutions(flowAlias).forEach(execution -> {
            if (execution.getLevel() == 0) { // Listing only top level for each subflow to avoid duplication
                if (execution.getAuthenticationFlow() != null && execution.getAuthenticationFlow()) {
                    var subflowResources = generate(realm, execution.getDisplayName(), execution.getFlowId(),
                        flowAlias, flowResource, level + 1);
                    resources.addAll(subflowResources);
                } else {
                    String flowPrefix = getFlowPrefix(flowAlias);
                    T executionResource = createExecution(realm, flowAlias, flowPrefix, execution, flowResource);
                    resources.add(executionResource);
                    System.out.println("\t".repeat(level) + " * " + executionResource);

                    Optional<AuthenticatorConfigRepresentation> config = execution.getAuthenticationConfig() != null
                        ? Optional.of(flows.getAuthenticatorConfig(execution.getAuthenticationConfig()))
                        : Optional.empty();
                    config.ifPresent(c -> {
                        T executionConfigResource = createExecutionConfig(realm, flowPrefix, execution, c,
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
        TerraformObject parentResource);

    protected abstract T createExecution(String realm, String parentFlowAlias, String flowPrefix,
        AuthenticationExecutionInfoRepresentation execution, TerraformObject parentResource);

    protected abstract T createFlow(String realm, String flowId, String flowAlias, String parentFlowAlias,
        TerraformObject parentResource);

    private static String getFlowPrefix(String flowAlias) {
        String sanitizedFlowAlias = sanitizeAlias(flowAlias);
        String prefix = sanitizedFlowAlias.split("_").length > 1
            ? WordUtils.initials(sanitizedFlowAlias, '_')
            : sanitizedFlowAlias;
        int hashCode = Math.abs(flowAlias.hashCode());
        int fourDigitHash = hashCode % 10000;
        return StringUtils.left(prefix, 4) + "_" + fourDigitHash + "_";
    }

    protected static String sanitizeAlias(String alias) {
        return StringUtils.stripEnd(alias
            .replaceAll("-", "_")
            .replaceAll("[^a-zA-Z0-9]+", "_")
            .toLowerCase(), "_");
    }

}
