package dev.ueslei.cloakform.processor.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ueslei.cloakform.model.TerraformImport;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
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
        log.warn("Client mappings import not available");
        return Map.of();
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
