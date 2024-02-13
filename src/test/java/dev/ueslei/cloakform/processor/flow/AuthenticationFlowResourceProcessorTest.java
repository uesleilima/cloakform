package dev.ueslei.cloakform.processor.flow;

import dev.ueslei.cloakform.config.ConverterConfiguration;
import dev.ueslei.cloakform.config.KeycloakConfiguration;
import dev.ueslei.cloakform.mapper.AuthenticationExecutionMapper;
import dev.ueslei.cloakform.test.AbstractKeycloakContainerBaseTest;
import dev.ueslei.cloakform.util.RealmNotFoundException;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.extensions.spring.test.ConverterScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ConverterScan(basePackageClasses = AuthenticationExecutionMapper.class)
@ContextConfiguration(classes = {KeycloakConfiguration.class,
    ConverterConfiguration.class,
    AuthenticationFlowResourceFileProcessor.class,
    AuthenticationFlowResourceApiProcessor.class})
public class AuthenticationFlowResourceProcessorTest extends AbstractKeycloakContainerBaseTest {

    @Autowired
    AuthenticationFlowResourceFileProcessor fileProcessor;

    @Autowired
    AuthenticationFlowResourceApiProcessor apiProcessor;

    @Test
    void generateTest() throws IOException, RealmNotFoundException {
        var realmFile = new ClassPathResource("cloakform-realm.json");
        var fileResources = fileProcessor.generate(realmFile, Optional.empty());
        Assertions.assertFalse(fileResources.isEmpty());

        var apiResources = apiProcessor.generate("cloakform", Optional.empty());
        Assertions.assertFalse(apiResources.isEmpty());

        Assertions.assertEquals(fileResources.size(), apiResources.size());
    }

}
