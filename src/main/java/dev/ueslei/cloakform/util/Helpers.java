package dev.ueslei.cloakform.util;

import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class Helpers {

    public static String sanitizeName(String name) {
        var sanitized = StringUtils.stripEnd(name
            .replaceAll("-", "_")
            .replaceAll("[^a-zA-Z0-9]+", "_")
            .toLowerCase(), "_");
        return Character.isDigit(sanitized.charAt(0)) ? "_" + sanitized : sanitized;
    }

    public static Optional<String> optional(String value) {
        return StringUtils.isBlank(value)
            ? Optional.empty()
            : Optional.of(value);
    }

}
