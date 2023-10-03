package dev.ueslei.cloakform.model;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class TerraformObject {

    private Map<String, Attribute> attributes = new HashMap<>();

    public void addAttribute(String name, String value) {
        addAttribute(name, value, AttributeType.STRING);
    }

    public void addAttribute(String name, Object value, AttributeType type) {
        this.attributes.put(name, new Attribute(type, value));
    }

    public Function<String, Object> handleAttribute() {
        return (key) -> {
            var attribute = attributes.get(key);
            return switch (attribute.type()) {
                case STRING -> String.format("'%s'", attribute.value());
                case REFERENCE -> String.format("%s", attribute.value());
                case MAP -> ((Map<String, String>) attribute.value())
                    .entrySet()
                    .stream()
                    .map(e -> "'" + e.getKey() + "' = '" + e.getValue() + "'")
                    .collect(Collectors.joining(",", " { ", " }"));
            };
        };
    }

}
