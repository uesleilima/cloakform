package dev.ueslei.cloakform.writer;

import dev.ueslei.cloakform.model.TerraformResource;
import org.springframework.stereotype.Component;

@Component
public class TerraformResourceWriter extends TerraformObjectWriter<TerraformResource> {

    private static final String TEMPLATE = "templates/resource.tf.mustache";

    public TerraformResourceWriter() {
        super(TEMPLATE);
    }
}
