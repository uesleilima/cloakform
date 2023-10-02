package dev.ueslei.cloakform.processor;

import dev.ueslei.cloakform.model.TerraformImport;
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
public class AuthenticationFlowProcessor {

    private final Keycloak keycloak;

    public List<TerraformImport> generateImports(String realm, String flowAlias) {
        return keycloak.realms().realm(realm).flows().getFlows().stream()
            .filter(f -> f.getAlias().equals(flowAlias))
            .findFirst()
            .map(flow -> generateImports(realm, flow.getId(), flow.getAlias(), null, 0))
            .orElse(List.of());
    }

    public List<TerraformImport> generateImports(String realm, String flowId, String flowAlias, String parentFlowAlias,
        int level) {
        List<TerraformImport> imports = new ArrayList<>();
        AuthenticationManagementResource flows = keycloak.realms().realm(realm).flows();

        var flowImport = createFlowImport(realm, flowId, flowAlias, parentFlowAlias);
        imports.add(flowImport);
        System.out.println("\t".repeat(level) + flowImport.name());

        flows.getExecutions(flowAlias).forEach(execution -> {
            if (execution.getLevel() == 0) { // Listing only top level for each subflow to avoid duplication
                if (execution.getAuthenticationFlow() != null && execution.getAuthenticationFlow()) {
                    var subflowImports = generateImports(realm, execution.getFlowId(), execution.getDisplayName(),
                        flowAlias, level + 1);
                    imports.addAll(subflowImports);
                } else {
                    String flowPrefix = getFlowPrefix(flowAlias);
                    TerraformImport executionImport = createExecutionImport(realm, flowAlias, execution,
                        flowPrefix);
                    imports.add(executionImport);
                    System.out.println("\t".repeat(level) + " * " + executionImport.name());

                    Optional<AuthenticatorConfigRepresentation> config = execution.getAuthenticationConfig() != null
                        ? Optional.of(flows.getAuthenticatorConfig(execution.getAuthenticationConfig()))
                        : Optional.empty();
                    config.ifPresent(c -> {
                        TerraformImport configImport = createExecutionConfigImport(realm, execution, c, flowPrefix);
                        imports.add(configImport);
                        System.out.println("\t".repeat(level) + "  \\_ " + configImport.name());
                    });
                }
            }
        });
        return imports;
    }

    private static TerraformImport createExecutionConfigImport(String realm,
        AuthenticationExecutionInfoRepresentation execution,
        AuthenticatorConfigRepresentation c, String flowPrefix) {
        String terraformExecutionConfigId = String.format("%s/%s/%s", realm, execution.getId(),
            c.getId());
        return new TerraformImport(
            terraformExecutionConfigId, "keycloak_authentication_execution_config",
            flowPrefix + sanitizeAlias(c.getAlias()));
    }

    private static TerraformImport createExecutionImport(String realm, String flowAlias,
        AuthenticationExecutionInfoRepresentation execution, String flowPrefix) {
        String terraformExecutionId = String.format("%s/%s/%s", realm, flowAlias, execution.getId());
        return new TerraformImport(terraformExecutionId,
            "keycloak_authentication_execution",
            flowPrefix + sanitizeAlias(execution.getProviderId()));
    }

    private static TerraformImport createFlowImport(String realm, String flowId, String flowAlias,
        String parentFlowAlias) {
        String terraformFlowId = parentFlowAlias == null
            ? String.format("%s/%s", realm, flowId)
            : String.format("%s/%s/%s", realm, parentFlowAlias, flowId);
        String terraformResource = parentFlowAlias == null
            ? "keycloak_authentication_flow"
            : "keycloak_authentication_subflow";
        return new TerraformImport(terraformFlowId, terraformResource, sanitizeAlias(flowAlias));
    }

    private static String getFlowPrefix(String flowAlias) {
        String sanitizedFlowAlias = sanitizeAlias(flowAlias);
        String prefix = sanitizedFlowAlias.split("_").length > 1
            ? WordUtils.initials(sanitizedFlowAlias, '_')
            : sanitizedFlowAlias;
        int hashCode = Math.abs(flowAlias.hashCode());
        int fourDigitHash = hashCode % 10000;
        return StringUtils.left(prefix, 4) + "_" + fourDigitHash + "_";
    }

    private static String sanitizeAlias(String alias) {
        return StringUtils.stripEnd(alias
            .replaceAll("-", "_")
            .replaceAll("[^a-zA-Z0-9]+", "_")
            .toLowerCase(), "_");
    }

}
