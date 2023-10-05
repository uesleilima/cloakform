package dev.ueslei.cloakform.handler;

import dev.ueslei.cloakform.model.TerraformImport;
import dev.ueslei.cloakform.processor.RealmImportProcessor;
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
@ShellCommandGroup("Realm Generator")
public class RealmHandler {

    private final RealmImportProcessor importProcessor;
    private final TerraformImportWriter importWriter;

    @ShellMethod(value = "Generates terraform file with Realm imports.", key = "realm imports")
    public void generateImports(
        @ShellOption(value = {"-r", "--realm"}, defaultValue = ShellOption.NULL) String realm,
        @ShellOption(value = {"-o", "--output"}, defaultValue = "realm_imports.tf") String output)
        throws IOException {

        List<TerraformImport> imports = importProcessor.generate(Helpers.optional(realm));
        if (imports.isEmpty()) {
            System.out.println("No objects found");
            return;
        }
        var outFile = Path.of(output);
        importWriter.write(imports, outFile);
        System.out.println("File generated: " + outFile.toAbsolutePath());
    }

}
