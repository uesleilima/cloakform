package dev.ueslei.cloakform.processor.flow;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ueslei.cloakform.model.TerraformResource;
import dev.ueslei.cloakform.util.RealmNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.AuthenticationExecutionInfoRepresentation;
import org.keycloak.representations.idm.AuthenticationFlowRepresentation;
import org.keycloak.representations.idm.AuthenticatorConfigRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFlowResourceFileProcessor extends AbstractAuthenticationFlowResourceProcessor {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConversionService conversionService;

    public List<TerraformResource> generate(Resource realmFile, Optional<String> flowAlias)
        throws IOException, RealmNotFoundException {
        RealmRepresentation realm = objectMapper.readValue(realmFile.getFile(), RealmRepresentation.class);
        return generate(realm, flowAlias);
    }

    protected List<AuthenticationFlowRepresentation> getTopLevelFlows(RealmRepresentation realm) {
        return realm.getAuthenticationFlows()
            .stream()
            .filter(AuthenticationFlowRepresentation::isTopLevel)
            .toList();
    }

    protected AuthenticationFlowRepresentation getFlow(RealmRepresentation realm,
        AuthenticationExecutionInfoRepresentation execution) {
        return realm.getAuthenticationFlows()
            .stream()
            .filter(f -> StringUtils.hasText(execution.getFlowId())
                ? f.getId().equals(execution.getFlowId())
                : f.getAlias().equals(execution.getDisplayName()))
            .findFirst()
            .get();
    }

    protected List<AuthenticationExecutionInfoRepresentation> getExecutions(RealmRepresentation realm,
        AuthenticationFlowRepresentation flow) {
        return realm.getAuthenticationFlows()
            .stream()
            .filter(f -> f.getId().equals(flow.getId()))
            .findFirst()
            .orElseThrow()
            .getAuthenticationExecutions()
            .stream()
            .map(e -> conversionService.convert(e, AuthenticationExecutionInfoRepresentation.class))
            .toList();
    }

    protected AuthenticatorConfigRepresentation getAuthenticatorConfig(
        RealmRepresentation realm, AuthenticationExecutionInfoRepresentation execution) {
        return realm.getAuthenticatorConfig()
            .stream()
            .filter(c -> c.getAlias().equals(execution.getAuthenticationConfig()))
            .findFirst()
            .get();
    }

}
