package dev.ueslei.cloakform.handler;

import dev.ueslei.cloakform.model.TerraformImport;
import dev.ueslei.cloakform.model.TerraformResource;
import dev.ueslei.cloakform.processor.flow.AuthenticationFlowImportApiProcessor;
import dev.ueslei.cloakform.processor.flow.AuthenticationFlowResourceApiProcessor;
import dev.ueslei.cloakform.util.Helpers;
import dev.ueslei.cloakform.util.RealmNotFoundException;
import dev.ueslei.cloakform.writer.TerraformImportWriter;
import dev.ueslei.cloakform.writer.TerraformResourceWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jline.terminal.Terminal;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
@RequiredArgsConstructor
@ShellCommandGroup("CloakForm :: Authentication Flows")
public class AuthenticationFlowHandler {

    private final AuthenticationFlowImportApiProcessor importProcessor;
    private final TerraformImportWriter importWriter;

    private final AuthenticationFlowResourceApiProcessor resourceProcessor;
    private final TerraformResourceWriter resourceWriter;

    private final Terminal terminal;

    @ShellMethod(value = "Generates a Terraform configuration file with Authentication Flows imports.", key = "flow imports")
    public void generateImports(
        @ShellOption(value = {"-r", "--realm"}) String realm,
        @ShellOption(value = {"-f", "--flow"}, defaultValue = ShellOption.NULL) String flowAlias,
        @ShellOption(value = {"-o", "--output"}, defaultValue = "flow_imports.tf") String output)
        throws IOException {

        try {
            List<TerraformImport> imports = importProcessor.generate(realm, Helpers.optional(flowAlias));
            if (imports.isEmpty()) {
                terminal.writer().println("No imports created");
            } else {
                imports.forEach(terminal.writer()::println);
                var outFile = Path.of(output);
                importWriter.write(imports, outFile);
                terminal.writer().println("File generated: " + outFile.toAbsolutePath());
            }
        } catch (RealmNotFoundException e) {
            terminal.writer().printf("Realm %s not found%n", realm);
        }
        terminal.writer().flush();
    }

    @ShellMethod(value = "Generates a Terraform configuration file with Authentication Flows resources.", key = "flow resources")
    public void generateResources(
        @ShellOption(value = {"-r", "--realm"}) String realm,
        @ShellOption(value = {"-f", "--flow"}, defaultValue = ShellOption.NULL) String flowAlias,
        @ShellOption(value = {"-o", "--output"}, defaultValue = "flow_resources.tf") String output)
        throws IOException {

        try {
            List<TerraformResource> resources = resourceProcessor.generate(realm, Helpers.optional(flowAlias));
            if (resources.isEmpty()) {
                terminal.writer().println("No resources created");
            } else {
                resources.forEach(terminal.writer()::println);
                var outFile = Path.of(output);
                resourceWriter.write(resources, outFile);
                terminal.writer().println("File generated: " + outFile.toAbsolutePath());
            }
        } catch (RealmNotFoundException e) {
            terminal.writer().printf("Realm %s not found%n", realm);
        }
        terminal.writer().flush();
    }

}
