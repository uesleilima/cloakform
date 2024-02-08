package dev.ueslei.cloakform.processor.flow;

import dev.ueslei.cloakform.model.TerraformImport;
import dev.ueslei.cloakform.util.RealmNotFoundException;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.Optional;
import org.keycloak.representations.idm.AuthenticationExecutionInfoRepresentation;
import org.keycloak.representations.idm.AuthenticationFlowRepresentation;
import org.keycloak.representations.idm.AuthenticatorConfigRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFlowImportApiProcessor extends AbstractAuthenticationFlowImportProcessor {

    @Autowired
    public AuthenticationFlowImportApiProcessor(AuthenticationFlowResourceApiProcessor delegate) {
        super(delegate);
    }

    public List<TerraformImport> generate(String realmName, Optional<String> flowAlias)
        throws RealmNotFoundException {
        try {
            RealmRepresentation realm = new RealmRepresentation();
            realm.setRealm(realmName);
            return generate(realm, flowAlias);
        } catch (NotFoundException ex) {
            throw new RealmNotFoundException(ex);
        }
    }

    @Override
    protected AuthenticatorConfigRepresentation getAuthenticatorConfig(
        RealmRepresentation realm, AuthenticationExecutionInfoRepresentation execution) {
        return delegate.getAuthenticatorConfig(realm, execution);
    }

    @Override
    protected AuthenticationFlowRepresentation getFlow(RealmRepresentation realm,
        AuthenticationExecutionInfoRepresentation execution) {
        return delegate.getFlow(realm, execution);
    }

    @Override
    protected List<AuthenticationExecutionInfoRepresentation> getExecutions(RealmRepresentation realm,
        AuthenticationFlowRepresentation flow) {
        return delegate.getExecutions(realm, flow);
    }

    @Override
    protected List<AuthenticationFlowRepresentation> getFlows(RealmRepresentation realm) {
        return delegate.getFlows(realm);
    }
}
