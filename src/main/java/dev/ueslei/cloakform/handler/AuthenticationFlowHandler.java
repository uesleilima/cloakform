package dev.ueslei.cloakform.handler;

import dev.ueslei.cloakform.model.TerraformImport;
import dev.ueslei.cloakform.processor.AuthenticationFlowProcessor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
@RequiredArgsConstructor
public class AuthenticationFlowHandler {

    private final AuthenticationFlowProcessor processor;

    @ShellMethod("Generates terraform file with Authentication Flows imports.")
    public void generate(
        @ShellOption(value = {"-r", "--realm"}) String realm,
        @ShellOption(value = {"-f", "--flow"}) String flowAlias,
        @ShellOption(value = {"-o", "--output"}, defaultValue = "/tmp/flows_imports.tf") String output) {

        List<TerraformImport> imports = processor.generateImports(realm, flowAlias);
    }

}
