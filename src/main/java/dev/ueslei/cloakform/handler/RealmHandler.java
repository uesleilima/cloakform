package dev.ueslei.cloakform.handler;

import dev.ueslei.cloakform.model.TerraformImport;
import dev.ueslei.cloakform.processor.realm.RealmImportApiProcessor;
import dev.ueslei.cloakform.processor.realm.RealmImportFileProcessor;
import dev.ueslei.cloakform.util.Helpers;
import dev.ueslei.cloakform.writer.TerraformImportWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jline.terminal.Terminal;
import org.springframework.core.io.FileSystemResource;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
@RequiredArgsConstructor
@ShellCommandGroup("CloakForm :: Realms")
public class RealmHandler {

    private final RealmImportApiProcessor importApiProcessor;
    private final RealmImportFileProcessor importFileProcessor;
    private final TerraformImportWriter importWriter;
    private final Terminal terminal;

    @ShellMethod(value = "Generates a Terraform configuration file with Realm imports.", key = "realm imports")
    public void generateImports(
        @ShellOption(value = {"-r", "--realm"}, defaultValue = ShellOption.NULL) String realm,
        @ShellOption(value = {"-o", "--output"}, defaultValue = "realm_imports.tf") String output,
        @ShellOption(value = {"-j", "--realm-json"}, defaultValue = ShellOption.NULL) String realmJson)
        throws IOException {

        List<TerraformImport> imports = Helpers.optional(realmJson).isPresent()
            ? importFileProcessor.generate(new FileSystemResource(realmJson))
            : importApiProcessor.generate(Helpers.optional(realm));
        if (imports.isEmpty()) {
            terminal.writer().println("No imports created");
        } else {
            imports.forEach(terminal.writer()::println);
            var outFile = Path.of(output);
            importWriter.write(imports, outFile);
            terminal.writer().println("File generated: " + outFile.toAbsolutePath());
        }
        terminal.writer().flush();
    }

}
