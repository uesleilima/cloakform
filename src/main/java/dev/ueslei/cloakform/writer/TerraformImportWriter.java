package dev.ueslei.cloakform.writer;

import dev.ueslei.cloakform.model.TerraformImport;
import org.springframework.stereotype.Component;

@Component
public class TerraformImportWriter extends TerraformObjectWriter<TerraformImport> {

    private static final String TEMPLATE = "templates/import.tf.mustache";

    public TerraformImportWriter() {
        super(TEMPLATE);
    }

}
