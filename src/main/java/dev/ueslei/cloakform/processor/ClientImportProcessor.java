package dev.ueslei.cloakform.processor;

import dev.ueslei.cloakform.model.TerraformImport;
import dev.ueslei.cloakform.util.Helpers;
import jakarta.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jline.terminal.Terminal;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientMappingsRepresentation;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClientImportProcessor {

    private final Keycloak keycloak;
    private final Terminal terminal;

    public List<TerraformImport> generate(String realm, Optional<String> clientId) {
        try {
            return clientId.map(cId -> keycloak.realm(realm)
                    .clients()
                    .findByClientId(cId))
                .orElse(keycloak.realm(realm)
                    .clients()
                    .findAll())
                .stream()
                .flatMap(client -> generate(realm, client).stream())
                .toList();
        } catch (NotFoundException ex) {
            terminal.writer().printf("Realm %s not found%n", realm);
            return List.of();
        }
    }

    public List<TerraformImport> generate(String realm, ClientRepresentation client) {
        List<TerraformImport> imports = new ArrayList<>();
        var clientImport = createClient(realm, client);
        imports.add(clientImport);
        terminal.writer().println(clientImport);

        if (client.getProtocolMappers() != null) {
            imports.addAll(client.getProtocolMappers()
                .stream()
                .map(m -> createProtocolMapper(realm, client, m))
                .peek(terminal.writer()::println)
                .toList());
        }

        var clientMappings = keycloak.realm(realm).clients().get(client.getId())
            .getScopeMappings()
            .getAll()
            .getClientMappings();
        if (clientMappings != null) {
            imports.addAll(clientMappings
                .entrySet()
                .stream()
                .map(m -> createRoleMapper(realm, client, m))
                .peek(terminal.writer()::println)
                .toList());
        }

        return imports;
    }

    private TerraformImport createRoleMapper(String realm, ClientRepresentation client,
        Entry<String, ClientMappingsRepresentation> m) {
        // terraformId: {{realmId}}/client/{{clientId}}/scope-mappings/{{roleClientId}}/{{roleId}}
        String terraformId = String.format("%s/client/%s/scope-mappings/%s/%s", realm, client.getId(), m.getKey(),
            m.getValue().getId());
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
