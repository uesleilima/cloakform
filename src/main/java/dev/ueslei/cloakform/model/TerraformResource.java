package dev.ueslei.cloakform.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TerraformResource extends TerraformObject {

    String resource;

    String name;

    @Override
    public String toString() {
        return this.name;
    }
}
