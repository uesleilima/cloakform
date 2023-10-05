package dev.ueslei.cloakform.handler;

import dev.ueslei.cloakform.model.TerraformImport;
import dev.ueslei.cloakform.processor.ClientImportProcessor;
import dev.ueslei.cloakform.util.Helpers;
import dev.ueslei.cloakform.writer.TerraformImportWriter;
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
@ShellCommandGroup("Client Generator")
public class ClientHandler {

    private final ClientImportProcessor importProcessor;
    private final TerraformImportWriter importWriter;

    @ShellMethod(value = "Generates terraform file with Clients imports.", key = "client imports")
    public void generateImports(
        @ShellOption(value = {"-r", "--realm"}) String realm,
        @ShellOption(value = {"-c", "--client-id"}, defaultValue = ShellOption.NULL) String clientId,
        @ShellOption(value = {"-o", "--output"}, defaultValue = "client_imports.tf") String output)
        throws IOException {

        List<TerraformImport> imports = importProcessor.generate(realm, Helpers.optional(clientId));
        var outFile = Path.of(output);
        importWriter.write(imports, outFile);
        System.out.println("File generated: " + outFile.toAbsolutePath());
    }

}
