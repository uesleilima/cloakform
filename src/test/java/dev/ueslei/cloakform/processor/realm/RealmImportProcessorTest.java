package dev.ueslei.cloakform.processor.realm;

import dev.ueslei.cloakform.config.KeycloakConfiguration;
import dev.ueslei.cloakform.test.AbstractKeycloakContainerBaseTest;
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
    RealmImportFileProcessor.class,
    RealmImportApiProcessor.class})
public class RealmImportProcessorTest extends AbstractKeycloakContainerBaseTest {

    @Autowired
    RealmImportFileProcessor fileProcessor;

    @Autowired
    RealmImportApiProcessor apiProcessor;

    @Test
    void generateTest() throws IOException {
        var realmFile = new ClassPathResource("cloakform-realm.json");
        var fileImports = fileProcessor.generate(realmFile);
        Assertions.assertFalse(fileImports.isEmpty());

        var apiImports = apiProcessor.generate(Optional.of("cloakform"));
        Assertions.assertFalse(apiImports.isEmpty());

        Assertions.assertEquals(fileImports.size(), apiImports.size());
    }

}
