package dev.ueslei.cloakform.processor.flow;

import java.util.List;
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
    protected List<AuthenticationFlowRepresentation> getFlows(RealmRepresentation realm) {
        return delegate.getFlows(realm);
    }
}
