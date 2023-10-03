package dev.ueslei.cloakform.writer;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import dev.ueslei.cloakform.model.TerraformImport;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TerraformImportWriter {

    private static final String TEMPLATE = "templates/import.tf.mustache";

    private final MustacheFactory mustacheFactory = new DefaultMustacheFactory();

    public void write(List<TerraformImport> importList, String outFile) throws IOException {
        Mustache mustache = mustacheFactory.compile(TEMPLATE);
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(outFile), StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING)) {
            importList.forEach(obj -> {
                try {
                    mustache.execute(writer, obj.getAttributes());
                    writer.newLine();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
            writer.flush();
        }
    }

}
