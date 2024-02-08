package dev.ueslei.cloakform.processor.client;

import dev.ueslei.cloakform.model.TerraformImport;
import dev.ueslei.cloakform.util.Helpers;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;

public abstract class AbstractClientImportProcessor {

    protected List<TerraformImport> generate(RealmRepresentation realm, Optional<String> clientId) {
        return clientId.map(cId -> getClientsById(realm, cId))
            .orElse(getClients(realm))
            .stream()
            .flatMap(client -> generate(realm, client).stream())
            .toList();
    }

    public List<TerraformImport> generate(RealmRepresentation realm, ClientRepresentation client) {
        List<TerraformImport> imports = new ArrayList<>();
        var clientImport = createClient(realm.getRealm(), client);
        imports.add(clientImport);

        if (client.getProtocolMappers() != null) {
            imports.addAll(client.getProtocolMappers()
                .stream()
                .map(m -> createProtocolMapper(realm.getRealm(), client, m))
                .toList());
        }

        var clientMappings = getClientMappings(realm, client);
        imports.addAll(clientMappings
            .entrySet()
            .stream()
            .map(e -> createRoleMapper(realm.getRealm(), client, e))
            .toList());

        return imports;
    }

    protected abstract Map<String, String> getClientMappings(RealmRepresentation realm, ClientRepresentation client);

    protected abstract List<ClientRepresentation> getClientsById(RealmRepresentation realm, String cId);

    protected abstract List<ClientRepresentation> getClients(RealmRepresentation realm);

    private TerraformImport createRoleMapper(String realm, ClientRepresentation client,
        Entry<String, String> entry) {
        // terraformId: {{realmId}}/client/{{clientId}}/scope-mappings/{{roleClientId}}/{{roleId}}
        String terraformId = String.format("%s/client/%s/scope-mappings/%s/%s", realm, client.getId(), entry.getKey(),
            entry.getValue());
        String resourceName = String.format("%s_role_map", Helpers.sanitizeName(client.getClientId()));
        return new TerraformImport(terraformId, "keycloak_generic_role_mapper", resourceName);
    }

    private TerraformImport createProtocolMapper(String realm, ClientRepresentation client,
        ProtocolMapperRepresentation mapper) {
        String terraformId = String.format("%s/client/%s/%s", realm, client.getId(), mapper.getId());
        String resource = "keycloak_generic_protocol_mapper";
        String resourceName = String.format("%s_%s", Helpers.sanitizeName(client.getClientId()),
            Helpers.sanitizeName(mapper.getName()));
        return new TerraformImport(terraformId, resource, resourceName);
    }

    private TerraformImport createClient(String realm, ClientRepresentation client) {
        String terraformId = String.format("%s/%s", realm, client.getId());
        String resource =
            client.getProtocol().equals("openid-connect") ? "keycloak_openid_client" : "keycloak_saml_client";
        String resourceName = Helpers.sanitizeName(client.getClientId());
        return new TerraformImport(terraformId, resource, resourceName);
    }
}
