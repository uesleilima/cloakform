package dev.ueslei.cloakform.handler;

import dev.ueslei.cloakform.model.TerraformImport;
import dev.ueslei.cloakform.model.TerraformResource;
import dev.ueslei.cloakform.processor.ImportAuthenticationFlowProcessor;
import dev.ueslei.cloakform.processor.ResourceAuthenticationFlowProcessor;
import dev.ueslei.cloakform.writer.TerraformImportWriter;
import dev.ueslei.cloakform.writer.TerraformResourceWriter;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
@RequiredArgsConstructor
public class AuthenticationFlowHandler {

    private final ImportAuthenticationFlowProcessor importProcessor;
    private final TerraformImportWriter importWriter;

    private final ResourceAuthenticationFlowProcessor resourceProcessor;
    private final TerraformResourceWriter resourceWriter;

    @ShellMethod("Generates terraform file with Authentication Flows imports.")
    public void imports(
        @ShellOption(value = {"-r", "--realm"}) String realm,
        @ShellOption(value = {"-f", "--flow"}) String flowAlias,
        @ShellOption(value = {"-o", "--output"}, defaultValue = "/tmp/flows_imports.tf") String output)
        throws IOException {

        List<TerraformImport> imports = importProcessor.generate(realm, flowAlias);
        importWriter.write(imports, output);
    }

    @ShellMethod("Generates terraform file with Authentication Flows resources.")
    public void resources(
        @ShellOption(value = {"-r", "--realm"}) String realm,
        @ShellOption(value = {"-f", "--flow"}) String flowAlias,
        @ShellOption(value = {"-o", "--output"}, defaultValue = "/tmp/flows_resources.tf") String output)
        throws IOException {

        List<TerraformResource> resources = resourceProcessor.generate(realm, flowAlias);
        resourceWriter.write(resources, output);
    }

}
