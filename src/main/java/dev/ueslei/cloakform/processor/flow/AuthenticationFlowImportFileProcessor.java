package dev.ueslei.cloakform.processor.flow;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ueslei.cloakform.model.TerraformImport;
import dev.ueslei.cloakform.util.RealmNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.keycloak.representations.idm.AuthenticationExecutionInfoRepresentation;
import org.keycloak.representations.idm.AuthenticationFlowRepresentation;
import org.keycloak.representations.idm.AuthenticatorConfigRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFlowImportFileProcessor extends AbstractAuthenticationFlowImportProcessor {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public AuthenticationFlowImportFileProcessor(AuthenticationFlowResourceFileProcessor delegate) {
        super(delegate);
    }

    public List<TerraformImport> generate(Resource realmFile, Optional<String> flowAlias)
        throws IOException, RealmNotFoundException {
        RealmRepresentation realm = objectMapper.readValue(realmFile.getFile(), RealmRepresentation.class);
        return generate(realm, flowAlias);
    }

    @Override
    protected AuthenticatorConfigRepresentation getAuthenticatorConfig(
        RealmRepresentation realm, AuthenticationExecutionInfoRepresentation execution) {
        return delegate.getAuthenticatorConfig(realm, execution);
    }

    @Override
    protected AuthenticationFlowRepresentation getFlow(RealmRepresentation realm, AuthenticationExecutionInfoRepresentation execution) {
        return delegate.getFlow(realm, execution);
    }

    @Override
    protected List<AuthenticationExecutionInfoRepresentation> getExecutions(RealmRepresentation realm,
        AuthenticationFlowRepresentation flow) {
        return delegate.getExecutions(realm, flow);
    }

    @Override
    protected List<AuthenticationFlowRepresentation> getTopLevelFlows(RealmRepresentation realm) {
        return delegate.getTopLevelFlows(realm);
    }
}
