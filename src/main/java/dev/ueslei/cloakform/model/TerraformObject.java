package dev.ueslei.cloakform.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class TerraformObject {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Map<String, Attribute> attributes = new HashMap<>();

    public void addAttribute(String name, String value) {
        addAttribute(name, value, AttributeType.STRING);
    }

    public void addAttribute(String name, Object value, AttributeType type) {
        this.attributes.put(name, new Attribute(type, value));
    }

    public Optional<String> getAttribute(String name) {
        return Optional.ofNullable(this.attributes.get(name))
            .filter(a -> !AttributeType.MAP.equals(a.type()))
            .map(Attribute::value)
            .map(String::valueOf);
    }

    public Function<String, Object> handleAttribute() {
        return (key) -> {
            var attribute = attributes.get(key);
            return switch (attribute.type()) {
                case REFERENCE -> attribute.value().toString();
                case STRING -> String.format("\"%s\"", attribute.value());
                case MAP -> {
                    try {
                        yield MAPPER.writeValueAsString(attribute.value());
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        };
    }

}
