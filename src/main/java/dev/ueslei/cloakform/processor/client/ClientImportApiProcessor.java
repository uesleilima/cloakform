package dev.ueslei.cloakform.processor.client;

import dev.ueslei.cloakform.model.TerraformImport;
import dev.ueslei.cloakform.util.RealmNotFoundException;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClientImportApiProcessor extends AbstractClientImportProcessor {

    private final Keycloak keycloak;

    public List<TerraformImport> generate(String realmName, Optional<String> clientId) throws RealmNotFoundException {
        try {
            RealmRepresentation realm = new RealmRepresentation();
            realm.setRealm(realmName);
            return generate(realm, clientId);
        } catch (NotFoundException ex) {
            throw new RealmNotFoundException(ex);
        }
    }

    protected Map<String, String> getClientMappings(RealmRepresentation realm, ClientRepresentation client) {
        return keycloak.realm(realm.getRealm()).clients().get(client.getId())
            .getScopeMappings()
            .getAll()
            .getClientMappings()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().getId()));
    }

    protected List<ClientRepresentation> getClientsById(RealmRepresentation realm, String cId) {
        return keycloak.realm(realm.getRealm())
            .clients()
            .findByClientId(cId);
    }

    protected List<ClientRepresentation> getClients(RealmRepresentation realm) {
        return keycloak.realm(realm.getRealm())
            .clients()
            .findAll();
    }
}
