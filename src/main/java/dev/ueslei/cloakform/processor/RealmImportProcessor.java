package dev.ueslei.cloakform.processor;

import dev.ueslei.cloakform.model.TerraformImport;
import dev.ueslei.cloakform.util.Helpers;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RequiredActionProviderRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RealmImportProcessor {

    public static final String KEYCLOAK_REALM = "keycloak_realm";

    private final Keycloak keycloak;

    @Value("${keycloak.roles.ignored:offline_access,uma_authorization}")
    private List<String> ignoredRoles = List.of("offline_access", "uma_authorization");

    public List<TerraformImport> generate(Optional<String> realmName) {
        return keycloak.realms()
            .findAll()
            .stream()
            .filter(r -> realmName.isEmpty() || r.getRealm().equals(realmName.get()))
            .flatMap(r -> generate(r).stream())
            .toList();
    }

    public List<TerraformImport> generate(RealmRepresentation realm) {
        List<TerraformImport> imports = new ArrayList<>();
        var realmImport = createRealm(realm);
        imports.add(realmImport);

        imports.addAll(keycloak.realm(realm.getRealm())
            .roles()
            .list()
            .stream()
            .filter(r -> !ignoredRoles.contains(r.getName()) && !r.getName().equals(realm.getDefaultRole().getName()))
            .map(r -> createRole(realm.getRealm(), r))
            .toList());

        var defaultRolesImport = createDefaultRoles(realm.getRealm(), realm.getDefaultRole());
        imports.add(defaultRolesImport);

        var groups = keycloak.realm(realm.getRealm())
            .groups()
            .groups();
        if (groups != null) {
            imports.addAll(groups.stream()
                .map(g -> createGroup(realm.getRealm(), g))
                .toList());
        }

        if (!keycloak.realm(realm.getRealm()).getDefaultGroups().isEmpty()) {
            var defaultGroupsImport = createDefaultGroups(realm.getRealm());
            imports.add(defaultGroupsImport);
        }

        imports.addAll(keycloak.realm(realm.getRealm()).flows().getRequiredActions().stream()
            .map(ra -> createRequiredAction(realm.getRealm(), ra))
            .toList());

        return imports;
    }

    private TerraformImport createRequiredAction(String realm, RequiredActionProviderRepresentation requiredAction) {
        return new TerraformImport(String.format("%s/%s", realm, requiredAction.getAlias()), "keycloak_required_action",
            String.format("%s_req_action", Helpers.sanitizeName(requiredAction.getAlias())));
    }

    private TerraformImport createDefaultGroups(String realm) {
        return new TerraformImport(realm, "keycloak_default_groups", "default");
    }

    private TerraformImport createGroup(String realm, GroupRepresentation group) {
        return new TerraformImport(String.format("%s/%s", realm, group.getId()), "keycloak_group",
            Helpers.sanitizeName(group.getName()));
    }

    private TerraformImport createDefaultRoles(String realm, RoleRepresentation defaultRole) {
        return new TerraformImport(String.format("%s/%s", realm, defaultRole.getId()), "keycloak_default_roles",
            Helpers.sanitizeName(defaultRole.getName()));
    }

    private TerraformImport createRole(String realm, RoleRepresentation roleRepresentation) {
        return new TerraformImport(String.format("%s/%s", realm, roleRepresentation.getId()), "keycloak_role",
            Helpers.sanitizeName(roleRepresentation.getName()));
    }

    protected TerraformImport createRealm(RealmRepresentation realm) {
        return new TerraformImport(realm.getRealm(), KEYCLOAK_REALM, Helpers.sanitizeName(realm.getRealm()));
    }
}
