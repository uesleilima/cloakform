package dev.ueslei.cloakform.handler;

import dev.ueslei.cloakform.model.TerraformImport;
import dev.ueslei.cloakform.processor.client.ClientImportApiProcessor;
import dev.ueslei.cloakform.util.Helpers;
import dev.ueslei.cloakform.util.RealmNotFoundException;
import dev.ueslei.cloakform.writer.TerraformImportWriter;
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
@ShellCommandGroup("CloakForm :: Clients")
public class ClientHandler {

    private final ClientImportApiProcessor importProcessor;
    private final TerraformImportWriter importWriter;
    private final Terminal terminal;

    @ShellMethod(value = "Generates a Terraform configuration file with Clients imports.", key = "client imports")
    public void generateImports(
        @ShellOption(value = {"-r", "--realm"}) String realm,
        @ShellOption(value = {"-c", "--client-id"}, defaultValue = ShellOption.NULL) String clientId,
        @ShellOption(value = {"-o", "--output"}, defaultValue = "client_imports.tf") String output)
        throws IOException {

        try {
            List<TerraformImport> imports = importProcessor.generate(realm, Helpers.optional(clientId));
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

}
