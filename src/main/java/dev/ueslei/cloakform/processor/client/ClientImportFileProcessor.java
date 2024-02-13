package dev.ueslei.cloakform.processor.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ueslei.cloakform.model.TerraformImport;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.ScopeMappingRepresentation;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClientImportFileProcessor extends AbstractClientImportProcessor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<TerraformImport> generate(Resource realmFile, Optional<String> clientId)
        throws IOException {
        RealmRepresentation realm = objectMapper.readValue(realmFile.getFile(), RealmRepresentation.class);
        return generate(realm, clientId);
    }

    @Override
    protected Map<String, String> getClientMappings(RealmRepresentation realm, ClientRepresentation client) {
        var clientMappings = realm.getClientScopeMappings().get(client.getClientId());
        return clientMappings == null
            ? Map.of()
            : clientMappings.stream()
                .collect(Collectors.toMap(ScopeMappingRepresentation::getClient,
                    e -> realm.getClients()
                        .stream()
                        .filter(c -> c.getClientId().equals(e.getClient()))
                        .findFirst()
                        .get()
                        .getId()));
    }

    @Override
    protected List<ClientRepresentation> getClientsById(RealmRepresentation realm, String cId) {
        return realm.getClients()
            .stream()
            .filter(c -> c.getClientId().equals(cId))
            .toList();
    }

    @Override
    protected List<ClientRepresentation> getClients(RealmRepresentation realm) {
        return realm.getClients();
    }
}
