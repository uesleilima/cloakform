package dev.ueslei.cloakform.processor.flow;

import dev.ueslei.cloakform.config.ConverterConfiguration;
import dev.ueslei.cloakform.mapper.AuthenticationExecutionMapper;
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
@ContextConfiguration(classes = {ConverterConfiguration.class, AuthenticationFlowResourceFileProcessor.class})
public class AuthenticationFlowResourceFileProcessorTest {

    @Autowired
    AuthenticationFlowResourceFileProcessor processor;

    @Test
    void generateTest() throws IOException, RealmNotFoundException {
        var realmFile = new ClassPathResource("cloakform-realm.json");
        var resources = processor.generate(realmFile, Optional.empty());
        Assertions.assertFalse(resources.isEmpty());
    }

}
