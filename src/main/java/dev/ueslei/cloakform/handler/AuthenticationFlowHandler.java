package dev.ueslei.cloakform.handler;

import dev.ueslei.cloakform.model.TerraformImport;
import dev.ueslei.cloakform.model.TerraformResource;
import dev.ueslei.cloakform.processor.flow.AuthenticationFlowImportProcessor;
import dev.ueslei.cloakform.processor.flow.AuthenticationFlowResourceProcessor;
import dev.ueslei.cloakform.util.Helpers;
import dev.ueslei.cloakform.writer.TerraformImportWriter;
import dev.ueslei.cloakform.writer.TerraformResourceWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
@RequiredArgsConstructor
@ShellCommandGroup("Authentication Flow Generator")
public class AuthenticationFlowHandler {

    private final AuthenticationFlowImportProcessor importProcessor;
    private final TerraformImportWriter importWriter;

    private final AuthenticationFlowResourceProcessor resourceProcessor;
    private final TerraformResourceWriter resourceWriter;

    @ShellMethod(value = "Generates terraform file with Authentication Flows imports.", key = "flow imports")
    public void generateImports(
        @ShellOption(value = {"-r", "--realm"}) String realm,
        @ShellOption(value = {"-f", "--flow"}, defaultValue = ShellOption.NULL) String flowAlias,
        @ShellOption(value = {"-o", "--output"}, defaultValue = "flow_imports.tf") String output)
        throws IOException {

        List<TerraformImport> imports = importProcessor.generate(realm, Helpers.optional(flowAlias));
        if (imports.isEmpty()){
            System.out.println("No objects found");
            return;
        }
        var outFile = Path.of(output);
        importWriter.write(imports, outFile);
        System.out.println("File generated: " + outFile.toAbsolutePath());
    }

    @ShellMethod(value = "Generates terraform file with Authentication Flows resources.", key = "flow resources")
    public void generateResources(
        @ShellOption(value = {"-r", "--realm"}) String realm,
        @ShellOption(value = {"-f", "--flow"}, defaultValue = ShellOption.NULL) String flowAlias,
        @ShellOption(value = {"-o", "--output"}, defaultValue = "flow_resources.tf") String output)
        throws IOException {

        List<TerraformResource> resources = resourceProcessor.generate(realm, Helpers.optional(flowAlias));
        if (resources.isEmpty()){
            System.out.println("No objects found");
            return;
        }
        var outFile = Path.of(output);
        resourceWriter.write(resources, outFile);
        System.out.println("File generated: " + outFile.toAbsolutePath());
    }

}
