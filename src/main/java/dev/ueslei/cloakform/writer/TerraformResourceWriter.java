package dev.ueslei.cloakform.writer;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.reflect.ReflectionObjectHandler;
import dev.ueslei.cloakform.model.TerraformResource;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class TerraformResourceWriter extends TerraformObjectWriter<TerraformResource> {

    private static final String TEMPLATE = "templates/resource.tf.mustache";

    public TerraformResourceWriter() {
        super(TEMPLATE);
    }

    @Override
    protected MustacheFactory createMustacheFactory() {
        DefaultMustacheFactory factory = new DefaultMustacheFactory();
        factory.setObjectHandler(new ReflectionObjectHandler() {
            @Override
            protected boolean areMethodsAccessible(Map<?, ?> map) {
                return true;
            }
        });
        return factory;
    }
}
