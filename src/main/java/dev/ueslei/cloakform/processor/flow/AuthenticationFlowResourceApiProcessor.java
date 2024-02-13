package dev.ueslei.cloakform.processor.flow;

import dev.ueslei.cloakform.model.TerraformResource;
import dev.ueslei.cloakform.util.RealmNotFoundException;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.AuthenticationManagementResource;
import org.keycloak.representations.idm.AuthenticationExecutionInfoRepresentation;
import org.keycloak.representations.idm.AuthenticationFlowRepresentation;
import org.keycloak.representations.idm.AuthenticatorConfigRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFlowResourceApiProcessor extends AbstractAuthenticationFlowResourceProcessor {

    private final Keycloak keycloak;

    public List<TerraformResource> generate(String realmName, Optional<String> flowAlias)
        throws RealmNotFoundException {
        try {
            return generate(keycloak.realm(realmName).toRepresentation(), flowAlias);
        } catch (NotFoundException ex) {
            throw new RealmNotFoundException(ex);
        }
    }

    protected AuthenticatorConfigRepresentation getAuthenticatorConfig(
        RealmRepresentation realm, AuthenticationExecutionInfoRepresentation execution) {
        return getAuthenticationManagement(realm)
            .getAuthenticatorConfig(execution.getAuthenticationConfig());
    }

    protected AuthenticationFlowRepresentation getFlow(RealmRepresentation realm,
        AuthenticationExecutionInfoRepresentation execution) {
        return getAuthenticationManagement(realm).getFlow(execution.getFlowId());
    }

    protected List<AuthenticationExecutionInfoRepresentation> getExecutions(RealmRepresentation realm,
        AuthenticationFlowRepresentation flow) {
        return getAuthenticationManagement(realm).getExecutions(flow.getAlias());
    }

    protected List<AuthenticationFlowRepresentation> getTopLevelFlows(RealmRepresentation realm) {
        return getAuthenticationManagement(realm).getFlows();
    }

    private AuthenticationManagementResource getAuthenticationManagement(RealmRepresentation realm) {
        return keycloak.realms().realm(realm.getRealm()).flows();
    }

}
