package dev.ueslei.cloakform.processor;

import dev.ueslei.cloakform.model.TerraformResource;
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
public abstract class AbstractAuthenticationFlowProcessor<T extends TerraformResource> {

    private final Keycloak keycloak;

    public List<T> generate(String realm, String flowAlias) {
        return keycloak.realms().realm(realm).flows().getFlows().stream()
            .filter(f -> f.getAlias().equals(flowAlias))
            .findFirst()
            .map(flow -> generate(realm, flow.getId(), flow.getAlias(), null, 0))
            .orElse(List.of());
    }

    public List<T> generate(String realm, String flowId, String flowAlias, String parentFlowAlias,
        int level) {
        List<T> imports = new ArrayList<>();
        AuthenticationManagementResource flows = keycloak.realms().realm(realm).flows();

        var flowImport = createFlow(realm, flowId, flowAlias, parentFlowAlias);
        imports.add(flowImport);
        System.out.println("\t".repeat(level) + flowImport.getName());

        flows.getExecutions(flowAlias).forEach(execution -> {
            if (execution.getLevel() == 0) { // Listing only top level for each subflow to avoid duplication
                if (execution.getAuthenticationFlow() != null && execution.getAuthenticationFlow()) {
                    var subflowImports = generate(realm, execution.getFlowId(), execution.getDisplayName(),
                        flowAlias, level + 1);
                    imports.addAll(subflowImports);
                } else {
                    String flowPrefix = getFlowPrefix(flowAlias);
                    T executionImport = createExecution(realm, flowAlias, flowPrefix, execution
                    );
                    imports.add(executionImport);
                    System.out.println("\t".repeat(level) + " * " + executionImport.getName());

                    Optional<AuthenticatorConfigRepresentation> config = execution.getAuthenticationConfig() != null
                        ? Optional.of(flows.getAuthenticatorConfig(execution.getAuthenticationConfig()))
                        : Optional.empty();
                    config.ifPresent(c -> {
                        T configImport = createExecutionConfig(realm, flowPrefix, execution, c);
                        imports.add(configImport);
                        System.out.println("\t".repeat(level) + "  \\_ " + configImport.getName());
                    });
                }
            }
        });
        return imports;
    }

    protected abstract T createExecutionConfig(String realm, String flowPrefix,
        AuthenticationExecutionInfoRepresentation execution, AuthenticatorConfigRepresentation executionConfig);

    protected abstract T createExecution(String realm, String parentFlowAlias,
        String flowPrefix, AuthenticationExecutionInfoRepresentation execution);

    protected abstract T createFlow(String realm, String flowId, String flowAlias,
        String parentFlowAlias);

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
