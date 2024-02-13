package dev.ueslei.cloakform.processor.realm;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ueslei.cloakform.model.TerraformImport;
import java.io.IOException;
import java.util.List;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RequiredActionProviderRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class RealmImportFileProcessor extends AbstractRealmImportProcessor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<TerraformImport> generate(Resource realmFile) throws IOException {
        RealmRepresentation realm = objectMapper.readValue(realmFile.getInputStream(), RealmRepresentation.class);
        return generate(realm);
    }

    @Override
    protected List<RequiredActionProviderRepresentation> getRequiredActions(RealmRepresentation realm) {
        return realm.getRequiredActions();
    }

    @Override
    protected List<RoleRepresentation> getRoleRepresentations(RealmRepresentation realm) {
        return realm.getRoles().getRealm();
    }

    @Override
    protected boolean hasDefaultGroups(RealmRepresentation realm) {
        return !realm.getDefaultGroups().isEmpty();
    }

    @Override
    protected List<GroupRepresentation> getGroups(RealmRepresentation realm) {
        return realm.getGroups();
    }
}
