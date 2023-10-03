package dev.ueslei.cloakform.processor;

import static dev.ueslei.cloakform.model.AttributeType.MAP;
import static dev.ueslei.cloakform.model.AttributeType.REFERENCE;

import dev.ueslei.cloakform.model.TerraformObject;
import dev.ueslei.cloakform.model.TerraformResource;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.AuthenticationExecutionInfoRepresentation;
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
            flowPrefix + sanitizeAlias(executionConfig.getAlias()));
        resource.addAttribute("alias", executionConfig.getAlias());
        resource.addAttribute("realm_id", realm);
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
            flowPrefix + sanitizeAlias(execution.getProviderId()));
        resource.addAttribute("authenticator", execution.getProviderId());
        resource.addAttribute("realm_id", realm);
        resource.addAttribute("requirement", execution.getRequirement());
        getParentResourceName(parentResource)
            .ifPresent(name -> resource.addAttribute("parent_flow_alias", name + ".alias", REFERENCE));
        return resource;
    }

    @Override
    public TerraformResource createFlow(String realm, String flowId, String flowAlias,
        String parentFlowAlias, TerraformObject parentResource) {
        String terraformResource = parentFlowAlias == null
            ? "keycloak_authentication_flow"
            : "keycloak_authentication_subflow";
        var resource = new TerraformResource(terraformResource, sanitizeAlias(flowAlias));
        resource.addAttribute("alias", flowAlias);
        resource.addAttribute("realm_id", realm);
        getParentResourceName(parentResource)
            .ifPresent(name -> resource.addAttribute("parent_flow_alias", name + ".alias", REFERENCE));
        return resource;
    }

    private Optional<Object> getParentResourceName(TerraformObject parentObject) {
        if (parentObject instanceof TerraformResource parentResource) {
            return Optional.of(String.format("%s.%s", parentResource.getResource(), parentResource.getName()));
        }
        return Optional.empty();
    }

}
