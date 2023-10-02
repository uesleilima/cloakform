package dev.ueslei.cloakform.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TerraformImport extends TerraformResource {

    String id;

    public TerraformImport(String id, String resource, String name) {
        super(resource, name);
        this.id = id;
    }
}
