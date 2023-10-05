package dev.ueslei.cloakform.util;

import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class Helpers {

    public static String sanitizeName(String name) {
        return StringUtils.stripEnd(name
            .replaceAll("-", "_")
            .replaceAll("[^a-zA-Z0-9]+", "_")
            .toLowerCase(), "_");
    }

    public static Optional<String> optional(String value) {
        return StringUtils.isBlank(value)
            ? Optional.empty()
            : Optional.of(value);
    }

}
