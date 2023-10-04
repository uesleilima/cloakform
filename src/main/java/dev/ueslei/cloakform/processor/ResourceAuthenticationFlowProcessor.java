package dev.ueslei.cloakform.processor;

import static dev.ueslei.cloakform.model.AttributeType.MAP;
import static dev.ueslei.cloakform.model.AttributeType.REFERENCE;

import dev.ueslei.cloakform.model.TerraformObject;
import dev.ueslei.cloakform.model.TerraformResource;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.AuthenticationExecutionInfoRepresentation;
import org.keycloak.representations.idm.AuthenticationFlowRepresentation;
import org.keycloak.representations.idm.AuthenticatorConfigRepresentation;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ResourceAuthenticationFlowProcessor extends AbstractAuthenticationFlowProcessor<TerraformResource> {

    public ResourceAuthenticationFlowProcessor(Keycloak keycloak) {
        super(keycloak);
    }

    @Override
    public TerraformResource createExecutionConfig(String realm, String flowPrefix,
        AuthenticationExecutionInfoRepresentation execution, AuthenticatorConfigRepresentation executionConfig,
        TerraformObject parentResource) {
        var resource = new TerraformResource("keycloak_authentication_execution_config",
            flowPrefix + sanitizeName(executionConfig.getAlias()));
        resource.addAttribute("alias", executionConfig.getAlias());
        resource.addAttribute("realm_id", getRealmIdReference(parentResource), REFERENCE);
        resource.addAttribute("config", executionConfig.getConfig(), MAP);
        getParentResourceName(parentResource)
            .ifPresent(name -> resource.addAttribute("execution_id", name + ".id", REFERENCE));
        return resource;
    }

    @Override
    public TerraformResource createExecution(String realm, String parentFlowAlias,
        String flowPrefix, AuthenticationExecutionInfoRepresentation execution, TerraformObject parentResource) {
        var resource = new TerraformResource(
            "keycloak_authentication_execution",
            flowPrefix + sanitizeName(execution.getProviderId()));
        resource.addAttribute("authenticator", execution.getProviderId());
        resource.addAttribute("realm_id", getRealmIdReference(parentResource), REFERENCE);
        resource.addAttribute("requirement", execution.getRequirement());
        getParentResourceName(parentResource)
            .ifPresent(name -> resource.addAttribute("parent_flow_alias", name + ".alias", REFERENCE));
        return resource;
    }

    @Override
    public TerraformResource createFlow(String realm, String parentFlowAlias, AuthenticationFlowRepresentation flow,
        AuthenticationExecutionInfoRepresentation flowExecution, TerraformObject parentResource) {
        String terraformResource = parentFlowAlias == null
            ? "keycloak_authentication_flow"
            : "keycloak_authentication_subflow";
        var resource = new TerraformResource(terraformResource, sanitizeName(flow.getAlias()));
        resource.addAttribute("alias", flow.getAlias());
        resource.addAttribute("realm_id", getRealmIdReference(parentResource), REFERENCE);
        Optional.ofNullable(flow.getProviderId())
            .ifPresent(v -> resource.addAttribute("provider_id", v));
        if (flowExecution != null) {
            Optional.ofNullable(flowExecution.getRequirement())
                .ifPresent(v -> resource.addAttribute("requirement", v));
        }
        if (parentFlowAlias != null) {
            getParentResourceName(parentResource)
                .ifPresent(name -> resource.addAttribute("parent_flow_alias", name + ".alias", REFERENCE));
        }
        return resource;
    }

    @Override
    protected TerraformResource createRealm(String realmName) {
        TerraformResource realmResource = new TerraformResource("keycloak_realm", sanitizeName(realmName));
        realmResource.addAttribute("realm", realmName);
        return realmResource;
    }

    @Override
    protected boolean includeRealm() {
        return false;
    }

    private String getRealmIdReference(TerraformObject object) {
        if (object instanceof TerraformResource resource && resource.getResource().equals("keycloak_realm")) {
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
