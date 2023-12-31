package dev.ueslei.cloakform.processor.flow;

import dev.ueslei.cloakform.model.TerraformObject;
import dev.ueslei.cloakform.util.Helpers;
import dev.ueslei.cloakform.util.RealmNotFoundException;
import jakarta.ws.rs.NotFoundException;
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
public abstract class AuthenticationFlowObjectProcessor<T extends TerraformObject> {

    private final Keycloak keycloak;

    public List<T> generate(String realmName, Optional<String> flowAlias) throws RealmNotFoundException {
        try {
            return keycloak.realms().realm(realmName).flows().getFlows().stream()
                .filter(f -> flowAlias.isEmpty() || f.getAlias().equals(flowAlias.get()))
                .flatMap(flow -> generate(realmName, flow, null, null, null, 0).stream())
                .toList();
        } catch (NotFoundException ex) {
            throw new RealmNotFoundException(ex);
        }
    }

    public List<T> generate(String realmName, AuthenticationFlowRepresentation flow,
        AuthenticationFlowRepresentation parentFlow, AuthenticationExecutionInfoRepresentation flowExecution,
        TerraformObject parentObject, int level) {
        List<T> objects = new ArrayList<>();

        if (parentObject == null) {
            T realmObject = createRealm(realmName);
            if (includeRealm()) {
                objects.add(realmObject);
            }
            parentObject = realmObject;
        }

        T flowObject = createFlow(realmName, parentFlow, flow, flowExecution, parentObject);
        objects.add(flowObject);

        AuthenticationManagementResource flows = keycloak.realms().realm(realmName).flows();
        flows.getExecutions(flow.getAlias()).forEach(execution -> {
            if (execution.getLevel() == 0) { // Listing only top level for each subflow to avoid duplication
                if (execution.getAuthenticationFlow() != null && execution.getAuthenticationFlow()) {
                    AuthenticationFlowRepresentation subflow = flows.getFlow(execution.getFlowId());
                    List<T> subflowObjects = generate(realmName, subflow, flow, execution, flowObject, level + 1);
                    objects.addAll(subflowObjects);
                } else {
                    T executionObject = createExecution(realmName, flow, execution, flowObject);
                    objects.add(executionObject);

                    Optional<AuthenticatorConfigRepresentation> config = execution.getAuthenticationConfig() != null
                        ? Optional.of(flows.getAuthenticatorConfig(execution.getAuthenticationConfig()))
                        : Optional.empty();
                    config.ifPresent(c -> {
                        T executionConfigObject = createExecutionConfig(realmName, getFlowPrefix(flow.getAlias()),
                            execution, c, executionObject);
                        objects.add(executionConfigObject);
                    });
                }
            }
        });
        return objects;
    }

    protected abstract T createExecutionConfig(String realm, String flowPrefix,
        AuthenticationExecutionInfoRepresentation execution, AuthenticatorConfigRepresentation executionConfig,
        TerraformObject parentObject);

    protected abstract T createExecution(String realm, AuthenticationFlowRepresentation flow,
        AuthenticationExecutionInfoRepresentation execution, TerraformObject parentObject);

    protected abstract T createFlow(String realm, AuthenticationFlowRepresentation parentFlow,
        AuthenticationFlowRepresentation flow, AuthenticationExecutionInfoRepresentation flowExecution,
        TerraformObject parentObject);

    protected abstract T createRealm(String realmName);

    protected abstract boolean includeRealm();

    /**
     * Creates a reproducible prefix used to avoid duplication on generated resource names.
     *
     * @param flowAlias The alias of the flow.
     * @return The alias initials together with a hash code of it.
     */
    protected static String getFlowPrefix(String flowAlias) {
        String sanitizedFlowAlias = Helpers.sanitizeName(flowAlias);
        String prefix = sanitizedFlowAlias.split("_").length > 1
            ? WordUtils.initials(sanitizedFlowAlias, '_')
            : sanitizedFlowAlias;
        int hashCode = Math.abs(flowAlias.hashCode());
        int fourDigitHash = hashCode % 10000;
        return StringUtils.left(prefix, 4) + "_" + fourDigitHash + "_";
    }

}
