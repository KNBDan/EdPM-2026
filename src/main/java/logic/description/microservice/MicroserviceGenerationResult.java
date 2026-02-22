package logic.description.microservice;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MicroserviceGenerationResult {
    private final Path outputDirectory;
    private final List<String> generatedFileNames;

    public MicroserviceGenerationResult(Path outputDirectory, List<String> generatedFileNames) {
        this.outputDirectory = outputDirectory;
        this.generatedFileNames = new ArrayList<>(generatedFileNames);
    }

    public Path getOutputDirectory() {
        return outputDirectory;
    }

    public List<String> getGeneratedFileNames() {
        return Collections.unmodifiableList(generatedFileNames);
    }
}
