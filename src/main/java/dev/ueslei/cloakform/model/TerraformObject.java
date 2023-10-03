package dev.ueslei.cloakform.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.map.HashedMap;


@Getter
@Setter
public class TerraformObject {

    private HashedMap attributes = new HashedMap();

    public void addAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

}
