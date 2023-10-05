package dev.ueslei.cloakform.processor;

import dev.ueslei.cloakform.model.TerraformImport;
import dev.ueslei.cloakform.util.Helpers;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientRepresentation;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClientImportProcessor {

    private final Keycloak keycloak;

    public List<TerraformImport> generate(String realm, Optional<String> clientId) {
        return clientId.map(cId -> keycloak.realm(realm)
                .clients()
                .findByClientId(cId))
            .orElse(keycloak.realm(realm)
                .clients()
                .findAll())
            .stream()
            .map(client -> createClient(realm, client))
            .toList();
    }

    private TerraformImport createClient(String realm, ClientRepresentation client) {
        String terraformId = String.format("%s/%s", realm, client.getId());
        String resource =
            client.getProtocol().equals("openid-connect") ? "keycloak_openid_client" : "keycloak_saml_client";
        String resourceName = Helpers.sanitizeName(client.getClientId());
        System.out.println(resourceName);
        return new TerraformImport(terraformId, resource, resourceName);
    }
}
