package dev.ueslei.cloakform.processor;

import dev.ueslei.cloakform.model.TerraformImport;
import dev.ueslei.cloakform.model.TerraformResource;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.AuthenticationExecutionInfoRepresentation;
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
        AuthenticationExecutionInfoRepresentation execution, AuthenticatorConfigRepresentation executionConfig) {
        TerraformResource resource = processor.createExecutionConfig(realm, flowPrefix, execution, executionConfig);
        String terraformExecutionConfigId = String.format("%s/%s/%s", realm, execution.getId(),
            executionConfig.getId());
        return new TerraformImport(terraformExecutionConfigId, resource.getResource(), resource.getName());
    }

    @Override
    public TerraformImport createExecution(String realm, String parentFlowAlias,
        String flowPrefix, AuthenticationExecutionInfoRepresentation execution) {
        TerraformResource resource = processor.createExecution(realm, parentFlowAlias, flowPrefix, execution);
        String terraformExecutionId = String.format("%s/%s/%s", realm, parentFlowAlias, execution.getId());
        return new TerraformImport(terraformExecutionId, resource.getResource(), resource.getName());
    }

    @Override
    public TerraformImport createFlow(String realm, String flowId, String flowAlias,
        String parentFlowAlias) {
        TerraformResource resource = processor.createFlow(realm, flowId, flowAlias, parentFlowAlias);
        String terraformFlowId = parentFlowAlias == null
            ? String.format("%s/%s", realm, flowId)
            : String.format("%s/%s/%s", realm, parentFlowAlias, flowId);
        return new TerraformImport(terraformFlowId, resource.getResource(), resource.getName());
    }

}
