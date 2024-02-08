package dev.ueslei.cloakform.processor.realm;

import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

public class RealmImportFileProcessorTest {

    private final RealmImportFileProcessor processor = new RealmImportFileProcessor();

    @Test
    void generateTest() throws IOException {
        var realmFile = new ClassPathResource("cloakform-realm.json");
        var imports = processor.generate(realmFile);
        Assertions.assertFalse(imports.isEmpty());
    }

}
