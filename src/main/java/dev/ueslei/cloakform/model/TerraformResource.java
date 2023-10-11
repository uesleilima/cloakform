package dev.ueslei.cloakform.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TerraformResource extends TerraformObject {

    String resource;

    String name;

    @Override
    public String toString() {
        return String.format("%s.%s", this.resource, this.name);
    }
}
