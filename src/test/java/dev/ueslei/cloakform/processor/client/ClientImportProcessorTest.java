package dev.ueslei.cloakform.processor.client;

import dev.ueslei.cloakform.config.KeycloakConfiguration;
import dev.ueslei.cloakform.test.AbstractKeycloakContainerBaseTest;
import dev.ueslei.cloakform.util.RealmNotFoundException;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {KeycloakConfiguration.class,
    ClientImportFileProcessor.class,
    ClientImportApiProcessor.class})
public class ClientImportProcessorTest extends AbstractKeycloakContainerBaseTest {

    @Autowired
    ClientImportFileProcessor fileProcessor;

    @Autowired
    ClientImportApiProcessor apiProcessor;

    @Test
    void generateTest() throws IOException, RealmNotFoundException {
        var realmFile = new ClassPathResource("cloakform-realm.json");
        var fileImports = fileProcessor.generate(realmFile, Optional.empty());
        Assertions.assertFalse(fileImports.isEmpty());

        var apiImports = apiProcessor.generate("cloakform", Optional.empty());
        Assertions.assertFalse(apiImports.isEmpty());

        Assertions.assertEquals(fileImports.size(), apiImports.size());
    }

}
