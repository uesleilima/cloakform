package dev.ueslei.cloakform.processor.flow;

import dev.ueslei.cloakform.model.TerraformImport;
import dev.ueslei.cloakform.model.TerraformObject;
import dev.ueslei.cloakform.model.TerraformResource;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.AuthenticationExecutionInfoRepresentation;
import org.keycloak.representations.idm.AuthenticationFlowRepresentation;
import org.keycloak.representations.idm.AuthenticatorConfigRepresentation;

@Slf4j
public abstract class AbstractAuthenticationFlowImportProcessor extends
    AbstractAuthenticationFlowObjectProcessor<TerraformImport> {

    protected final AbstractAuthenticationFlowResourceProcessor delegate;

    public AbstractAuthenticationFlowImportProcessor(AbstractAuthenticationFlowResourceProcessor delegate) {
        this.delegate = delegate;
    }

    @Override
    public TerraformImport createExecutionConfig(String realm, String flowPrefix,
        AuthenticationExecutionInfoRepresentation execution, AuthenticatorConfigRepresentation executionConfig,
        TerraformObject parentResource) {
        TerraformResource resource = delegate.createExecutionConfig(realm, flowPrefix, execution, executionConfig,
            parentResource);
        String terraformExecutionConfigId = String.format("%s/%s/%s", realm, execution.getId(),
            executionConfig.getId());
        return new TerraformImport(terraformExecutionConfigId, resource.getResource(), resource.getName());
    }

    @Override
    public TerraformImport createExecution(String realm, AuthenticationFlowRepresentation flow,
        AuthenticationExecutionInfoRepresentation execution, TerraformObject parentResource) {
        TerraformResource resource = delegate.createExecution(realm, flow, execution, parentResource);
        String terraformExecutionId = String.format("%s/%s/%s", realm, flow.getAlias(), execution.getId());
        return new TerraformImport(terraformExecutionId, resource.getResource(), resource.getName());
    }

    @Override
    public TerraformImport createFlow(String realm, AuthenticationFlowRepresentation parentFlow,
        AuthenticationFlowRepresentation flow, AuthenticationExecutionInfoRepresentation flowExecution,
        TerraformObject parentResource) {
        TerraformResource resource = delegate.createFlow(realm, parentFlow, flow, flowExecution, parentResource);
        String terraformFlowId = parentFlow == null
            ? String.format("%s/%s", realm, flow.getId())
            : String.format("%s/%s/%s", realm, parentFlow.getAlias(), flow.getId());
        return new TerraformImport(terraformFlowId, resource.getResource(), resource.getName());
    }

    @Override
    protected TerraformImport createRealm(String realmName) {
        TerraformResource resource = delegate.createRealm(realmName);
        return new TerraformImport(realmName, resource.getResource(), resource.getName());
    }

    @Override
    protected boolean includeRealm() {
        return true;
    }

}
