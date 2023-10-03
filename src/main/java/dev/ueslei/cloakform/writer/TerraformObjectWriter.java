package dev.ueslei.cloakform.writer;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import dev.ueslei.cloakform.model.TerraformObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class TerraformObjectWriter<T extends TerraformObject> {

    private final Mustache mustache;

    public TerraformObjectWriter(String template) {
        this.mustache = createMustacheFactory().compile(template);
    }

    protected MustacheFactory createMustacheFactory() {
        return new DefaultMustacheFactory();
    }

    public void write(List<T> resources, String outFile) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(outFile),
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            resources.forEach(obj -> {
                try {
                    mustache.execute(writer, obj);
                    writer.newLine();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            writer.flush();
        }
    }

}
