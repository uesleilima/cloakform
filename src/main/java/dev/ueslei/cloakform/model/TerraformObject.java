package dev.ueslei.cloakform.model;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class TerraformObject {

    private Map<String, Object> attributes = new HashMap<>();

}
