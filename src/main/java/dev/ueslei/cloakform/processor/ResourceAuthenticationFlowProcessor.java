package dev.ueslei.cloakform.processor;

import dev.ueslei.cloakform.model.TerraformObject;
import dev.ueslei.cloakform.model.TerraformResource;
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
        return resource;
    }

    @Override
    public TerraformResource createExecution(String realm, String parentFlowAlias,
        String flowPrefix, AuthenticationExecutionInfoRepresentation execution, TerraformObject parentResource) {
        var resource = new TerraformResource(
            "keycloak_authentication_execution",
            flowPrefix + sanitizeAlias(execution.getProviderId()));
        resource.addAttribute("authenticator", execution.getProviderId());
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
        return resource;
    }

}
