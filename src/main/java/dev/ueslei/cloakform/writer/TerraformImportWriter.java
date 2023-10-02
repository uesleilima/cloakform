package dev.ueslei.cloakform.writer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.ueslei.cloakform.model.TerraformImport;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.springframework.stereotype.Component;

@Component
public class TerraformImportWriter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String TEMPLATE = """
        import {
          id = "${id}"
          to = ${resource}.${name}
        }
        """;

    public void write(List<TerraformImport> importList, String outFile) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(outFile), StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING)) {
            importList.stream()
                .map(i -> objectMapper.convertValue(i, new TypeReference<Map<String, Object>>() {
                }))
                .map(map -> new StrSubstitutor(map).replace(TEMPLATE))
                .forEach(line -> {
                    try {
                        writer.write(line);
                        writer.newLine();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            writer.flush();
        }
    }

}
