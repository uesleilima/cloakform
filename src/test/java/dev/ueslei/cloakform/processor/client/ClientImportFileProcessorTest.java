package dev.ueslei.cloakform.processor.client;

import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

public class ClientImportFileProcessorTest {

    private final ClientImportFileProcessor processor = new ClientImportFileProcessor();

    @Test
    void generateTest() throws IOException {
        var realmFile = new ClassPathResource("cloakform-realm.json");
        var imports = processor.generate(realmFile, Optional.empty());
        Assertions.assertFalse(imports.isEmpty());
    }

}
