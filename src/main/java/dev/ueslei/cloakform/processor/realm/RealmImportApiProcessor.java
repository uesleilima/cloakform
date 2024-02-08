package dev.ueslei.cloakform.processor.realm;

import dev.ueslei.cloakform.model.TerraformImport;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RequiredActionProviderRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RealmImportApiProcessor extends AbstractRealmImportProcessor {

    private final Keycloak keycloak;

    public List<TerraformImport> generate(Optional<String> realmName) {
        return keycloak.realms()
            .findAll()
            .stream()
            .filter(r -> realmName.isEmpty() || r.getRealm().equals(realmName.get()))
            .flatMap(r -> generate(r).stream())
            .toList();
    }

    protected List<RequiredActionProviderRepresentation> getRequiredActions(RealmRepresentation realm) {
        return keycloak.realm(realm.getRealm()).flows().getRequiredActions();
    }

    protected List<RoleRepresentation> getRoleRepresentations(RealmRepresentation realm) {
        return keycloak.realm(realm.getRealm())
            .roles()
            .list();
    }

    protected boolean hasDefaultGroups(RealmRepresentation realm) {
        return !keycloak.realm(realm.getRealm()).getDefaultGroups().isEmpty();
    }

    protected List<GroupRepresentation> getGroups(RealmRepresentation realm) {
        return keycloak.realm(realm.getRealm())
            .groups()
            .groups();
    }
}
