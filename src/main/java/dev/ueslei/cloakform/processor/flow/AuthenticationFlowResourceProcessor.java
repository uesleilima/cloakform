package dev.ueslei.cloakform.processor.flow;

import static dev.ueslei.cloakform.model.AttributeType.LIST;
import static dev.ueslei.cloakform.model.AttributeType.MAP;
import static dev.ueslei.cloakform.model.AttributeType.REFERENCE;

import dev.ueslei.cloakform.model.Attribute;
import dev.ueslei.cloakform.model.TerraformObject;
import dev.ueslei.cloakform.model.TerraformResource;
import dev.ueslei.cloakform.util.Helpers;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.AbstractAuthenticationExecutionRepresentation;
import org.keycloak.representations.idm.AuthenticationExecutionInfoRepresentation;
import org.keycloak.representations.idm.AuthenticationFlowRepresentation;
import org.keycloak.representations.idm.AuthenticatorConfigRepresentation;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class AuthenticationFlowResourceProcessor extends AuthenticationFlowObjectProcessor<TerraformResource> {

    public static final String KEYCLOAK_REALM = "keycloak_realm";
    public static final String KEYCLOAK_AUTHENTICATION_FLOW = "keycloak_authentication_flow";
    public static final String KEYCLOAK_AUTHENTICATION_SUBFLOW = "keycloak_authentication_subflow";
    public static final String KEYCLOAK_AUTHENTICATION_EXECUTION = "keycloak_authentication_execution";
    public static final String KEYCLOAK_AUTHENTICATION_EXECUTION_CONFIG = "keycloak_authentication_execution_config";

    public AuthenticationFlowResourceProcessor(Keycloak keycloak) {
        super(keycloak);
    }

    @Override
    public TerraformResource createExecutionConfig(String realm, String flowPrefix,
        AuthenticationExecutionInfoRepresentation execution, AuthenticatorConfigRepresentation executionConfig,
        TerraformObject parentResource) {
        var resource = new TerraformResource(KEYCLOAK_AUTHENTICATION_EXECUTION_CONFIG,
            flowPrefix + Helpers.sanitizeName(executionConfig.getAlias()));
        resource.addAttribute("alias", executionConfig.getAlias());
        resource.addAttribute("realm_id", getRealmIdReference(parentResource), REFERENCE);
        resource.addAttribute("config", executionConfig.getConfig(), MAP);
        getParentResourceName(parentResource)
            .ifPresent(name -> resource.addAttribute("execution_id", name + ".id", REFERENCE));
        return resource;
    }

    @Override
    public TerraformResource createExecution(String realm, AuthenticationFlowRepresentation flow,
        AuthenticationExecutionInfoRepresentation execution, TerraformObject parentResource) {
        String flowPrefix = getFlowPrefix(flow.getAlias());
        var resource = new TerraformResource(KEYCLOAK_AUTHENTICATION_EXECUTION,
            flowPrefix + Helpers.sanitizeName(execution.getProviderId()));
        resource.addAttribute("authenticator", execution.getProviderId());
        resource.addAttribute("realm_id", getRealmIdReference(parentResource), REFERENCE);
        resource.addAttribute("requirement", execution.getRequirement());
        getParentResourceName(parentResource)
            .ifPresent(name -> resource.addAttribute("parent_flow_alias", name + ".alias", REFERENCE));
        addDependencyAttribute(flow, flowPrefix, execution.getProviderId(), resource);

        return resource;
    }

    @Override
    public TerraformResource createFlow(String realm, AuthenticationFlowRepresentation parentFlow,
        AuthenticationFlowRepresentation flow, AuthenticationExecutionInfoRepresentation flowExecution,
        TerraformObject parentResource) {
        String terraformResource = parentFlow == null
            ? KEYCLOAK_AUTHENTICATION_FLOW
            : KEYCLOAK_AUTHENTICATION_SUBFLOW;
        var resource = new TerraformResource(terraformResource, Helpers.sanitizeName(flow.getAlias()));
        resource.addAttribute("alias", flow.getAlias());
        resource.addAttribute("realm_id", getRealmIdReference(parentResource), REFERENCE);
        Optional.ofNullable(flow.getProviderId())
            .ifPresent(v -> resource.addAttribute("provider_id", v));
        if (flowExecution != null) {
            Optional.ofNullable(flowExecution.getRequirement())
                .ifPresent(v -> resource.addAttribute("requirement", v));
        }
        if (parentFlow != null) {
            getParentResourceName(parentResource)
                .ifPresent(name -> resource.addAttribute("parent_flow_alias", name + ".alias", REFERENCE));
            addDependencyAttribute(parentFlow, getFlowPrefix(parentFlow.getAlias()), flowExecution.getDisplayName(),
                resource);
        }

        return resource;
    }

    private void addDependencyAttribute(AuthenticationFlowRepresentation flow, String flowPrefix, String name,
        TerraformResource resource) {
        flow.getAuthenticationExecutions().stream()
            .filter(e -> name.equals(e.getAuthenticator()) || name.equals(e.getFlowAlias()))
            .findFirst().flatMap(e -> flow.getAuthenticationExecutions().stream()
                .filter(o -> o.getPriority() < e.getPriority())
                .max(Comparator.comparing(AbstractAuthenticationExecutionRepresentation::getPriority)))
            .ifPresent(
                dependency -> {
                    String reference = StringUtils.hasText(dependency.getFlowAlias())
                        ? String.format("%s.%s", KEYCLOAK_AUTHENTICATION_SUBFLOW,
                        Helpers.sanitizeName(dependency.getFlowAlias()))
                        : String.format("%s.%s%s", KEYCLOAK_AUTHENTICATION_EXECUTION, flowPrefix,
                            Helpers.sanitizeName(dependency.getAuthenticator()));
                    resource.addAttribute("depends_on", List.of(new Attribute(REFERENCE, reference)), LIST);
                });
    }

    @Override
    protected TerraformResource createRealm(String realmName) {
        TerraformResource realmResource = new TerraformResource(KEYCLOAK_REALM, Helpers.sanitizeName(realmName));
        realmResource.addAttribute("realm", realmName);
        return realmResource;
    }

    @Override
    protected boolean includeRealm() {
        return false;
    }

    private String getRealmIdReference(TerraformObject object) {
        if (object instanceof TerraformResource resource && resource.getResource().equals(KEYCLOAK_REALM)) {
            return String.format("%s.%s.id", resource.getResource(), resource.getName());
        }
        return object.getAttribute("realm_id").orElse(null);
    }

    private Optional<Object> getParentResourceName(TerraformObject parentObject) {
        if (parentObject instanceof TerraformResource parentResource) {
            return Optional.of(String.format("%s.%s", parentResource.getResource(), parentResource.getName()));
        }
        return Optional.empty();
    }

}
