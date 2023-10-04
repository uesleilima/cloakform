package dev.ueslei.cloakform.processor;

import dev.ueslei.cloakform.model.TerraformImport;
import dev.ueslei.cloakform.model.TerraformObject;
import dev.ueslei.cloakform.model.TerraformResource;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.AuthenticationExecutionInfoRepresentation;
import org.keycloak.representations.idm.AuthenticationFlowRepresentation;
import org.keycloak.representations.idm.AuthenticatorConfigRepresentation;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ImportAuthenticationFlowProcessor extends AbstractAuthenticationFlowProcessor<TerraformImport> {

    private final ResourceAuthenticationFlowProcessor processor;

    public ImportAuthenticationFlowProcessor(Keycloak keycloak, ResourceAuthenticationFlowProcessor processor) {
        super(keycloak);
        this.processor = processor;
    }

    @Override
    public TerraformImport createExecutionConfig(String realm, String flowPrefix,
        AuthenticationExecutionInfoRepresentation execution, AuthenticatorConfigRepresentation executionConfig,
        TerraformObject parentResource) {
        TerraformResource resource = processor.createExecutionConfig(realm, flowPrefix, execution, executionConfig,
            parentResource);
        String terraformExecutionConfigId = String.format("%s/%s/%s", realm, execution.getId(),
            executionConfig.getId());
        return new TerraformImport(terraformExecutionConfigId, resource.getResource(), resource.getName());
    }

    @Override
    public TerraformImport createExecution(String realm, String parentFlowAlias,
        String flowPrefix, AuthenticationExecutionInfoRepresentation execution, TerraformObject parentResource) {
        TerraformResource resource = processor.createExecution(realm, parentFlowAlias, flowPrefix, execution,
            parentResource);
        String terraformExecutionId = String.format("%s/%s/%s", realm, parentFlowAlias, execution.getId());
        return new TerraformImport(terraformExecutionId, resource.getResource(), resource.getName());
    }

    @Override
    public TerraformImport createFlow(String realm, String parentFlowAlias, AuthenticationFlowRepresentation flow,
        AuthenticationExecutionInfoRepresentation flowExecution, TerraformObject parentResource) {
        TerraformResource resource = processor.createFlow(realm, parentFlowAlias, flow, flowExecution, parentResource);
        String terraformFlowId = parentFlowAlias == null
            ? String.format("%s/%s", realm, flow.getId())
            : String.format("%s/%s/%s", realm, parentFlowAlias, flow.getId());
        return new TerraformImport(terraformFlowId, resource.getResource(), resource.getName());
    }

    @Override
    protected TerraformImport createRealm(String realmName) {
        TerraformResource resource = processor.createRealm(realmName);
        return new TerraformImport(realmName, resource.getResource(), resource.getName());
    }

    @Override
    protected boolean includeRealm() {
        return true;
    }

}
