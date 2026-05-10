package logic.regression;

import EPM.mdi;
import domain.DiagramLoadResult;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;
import logic.description.DescriptionService;
import logic.description.microservice.MicroserviceGenerationResult;
import logic.description.microservice.MicroserviceRBundleGenerator;
import logic.serialization.DiagramSerializer;
import logic.serialization.model.ConvertedObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

public class EdpmXesRegressionTest {

    private static final String PROP_ENABLED = "edpm.regression.enabled";
    private static final String PROP_MODEL = "edpm.regression.model";
    private static final String PROP_EXPECTED_XES = "edpm.regression.expected.xes";
    private static final String PROP_XES_NAME = "edpm.regression.xes.name";
    private static final String PROP_RSCRIPT = "edpm.rscript";
    private static final String PROP_N = "edpm.regression.n";

    @Test
    void linearAndMicroserviceShouldMatchExpectedXes() throws Exception {
        boolean enabled = Boolean.parseBoolean(System.getProperty(PROP_ENABLED, "false"));
        Assumptions.assumeTrue(enabled, "Set -Dedpm.regression.enabled=true to run this test.");

        String modelPathRaw = System.getProperty(PROP_MODEL, "").trim();
        String expectedXesRaw = System.getProperty(PROP_EXPECTED_XES, "").trim();
        Assumptions.assumeTrue(!modelPathRaw.isEmpty(), "Set -Dedpm.regression.model=<path-to-model.json>.");
        Assumptions.assumeTrue(!expectedXesRaw.isEmpty(), "Set -Dedpm.regression.expected.xes=<path-to-expected.csv>.");

        Path modelPath = Paths.get(modelPathRaw).toAbsolutePath().normalize();
        Path expectedXes = Paths.get(expectedXesRaw).toAbsolutePath().normalize();
        Assumptions.assumeTrue(Files.exists(modelPath), "Model file not found: " + modelPath);
        Assumptions.assumeTrue(Files.exists(expectedXes), "Expected XES file not found: " + expectedXes);

        String rscript = resolveRscript();
        Assumptions.assumeTrue(rscript != null, "Rscript not found. Set -Dedpm.rscript=<full-path>.");

        String xesName = System.getProperty(PROP_XES_NAME, "xesik").trim();
        String nValue = System.getProperty(PROP_N, "30").trim();
        configurePrefs(xesName, nValue);

        Path runRoot = Paths.get("target", "regression-runs", sanitizeName(modelPath.getFileName().toString()));
        recreateDir(runRoot);

        DiagramSerializer serializer = new DiagramSerializer();
        DiagramLoadResult loaded = serializer.loadFromJson(modelPath.toString());
        ConvertedObject converted = serializer.createConvertedObject(
                loaded.getFigures(),
                loaded.getLines(),
                loaded.getZoom(),
                loaded.getIdS(),
                loaded.getIdNV(),
                loaded.getIdV(),
                loaded.getIdR(),
                loaded.getIdO(),
                loaded.getIdIF()
        );

        DescriptionService descriptionService = new DescriptionService();
        String pseudoCode = descriptionService.generateDescription(converted);
        String linearR = descriptionService.generateRCode(loaded.getFigures(), pseudoCode);

        Path linearScript = runRoot.resolve("linear_generated.R");
        Files.writeString(linearScript, linearR, StandardCharsets.UTF_8);
        runRscript(rscript, runRoot, "linear_generated.R");

        Path linearXes = runRoot.resolve(xesName + ".csv");
        Assertions.assertTrue(Files.exists(linearXes), "Linear XES not generated: " + linearXes);

        MicroserviceRBundleGenerator bundleGenerator = new MicroserviceRBundleGenerator();
        MicroserviceGenerationResult bundle = bundleGenerator.generateBundle(linearR, runRoot.resolve("GeneratedMicroservices"));
        Path bundleDir = bundle.getOutputDirectory();
        runRscript(rscript, bundleDir, "run_local.R", "1", "12345", "1");

        Path microXes = bundleDir.resolve(xesName + ".csv");
        Assertions.assertTrue(Files.exists(microXes), "Microservice XES not generated: " + microXes);

        String expectedCanonical = canonicalCsv(expectedXes);
        String linearCanonical = canonicalCsv(linearXes);
        String microCanonical = canonicalCsv(microXes);

        Assertions.assertEquals(expectedCanonical, linearCanonical, "Linear XES differs from expected.");
        Assertions.assertEquals(linearCanonical, microCanonical, "Microservice XES differs from linear XES.");
    }

    private static void configurePrefs(String xesName, String nValue) {
        Preferences prefs = mdi.prefsMdi;
        prefs.put("NValue", nValue);
        prefs.putBoolean("graphState", true);
        prefs.putBoolean("xesState", true);
        prefs.put("xesName", xesName);
        prefs.put("startId", "66");
        prefs.put("stepId", "66");
        prefs.putBoolean("oActiveState", true);
    }

    private static String resolveRscript() {
        String configured = System.getProperty(PROP_RSCRIPT, "").trim();
        if (!configured.isEmpty() && canRun(configured)) {
            return configured;
        }
        String[] candidates = isWindows()
                ? new String[]{"Rscript.exe", "Rscript"}
                : new String[]{"Rscript"};
        for (String candidate : candidates) {
            if (canRun(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean canRun(String command) {
        try {
            Process p = new ProcessBuilder(command, "--version")
                    .redirectErrorStream(true)
                    .start();
            int code = p.waitFor();
            return code == 0;
        } catch (Exception ex) {
            return false;
        }
    }

    private static void runRscript(String rscript, Path workDir, String... args) throws Exception {
        List<String> cmd = new ArrayList<>();
        cmd.add(rscript);
        for (String arg : args) {
            cmd.add(arg);
        }
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(workDir.toFile());
        pb.redirectErrorStream(true);
        Path logFile = workDir.resolve("regression_exec.log");
        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile.toFile()));
        Process p = pb.start();
        int code = p.waitFor();
        if (code != 0) {
            String log = Files.exists(logFile) ? Files.readString(logFile, StandardCharsets.UTF_8) : "";
            throw new IllegalStateException("Rscript failed with exit code " + code + "\n" + log);
        }
    }

    private static String canonicalCsv(Path csvPath) throws IOException {
        List<String> lines = Files.readAllLines(csvPath, StandardCharsets.UTF_8);
        if (lines.isEmpty()) {
            return "";
        }
        List<String> normalized = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String ln = lines.get(i).trim();
            if (ln.isEmpty()) {
                continue;
            }
            normalized.add(dropRowIndexIfPresent(ln, i == 0));
        }
        if (normalized.isEmpty()) {
            return "";
        }
        String header = normalized.get(0);
        List<String> body = new ArrayList<>(normalized.subList(1, normalized.size()));
        body.sort(Comparator.naturalOrder());
        StringBuilder sb = new StringBuilder();
        sb.append(header).append("\n");
        for (String row : body) {
            sb.append(row).append("\n");
        }
        return sb.toString();
    }

    private static String dropRowIndexIfPresent(String line, boolean isHeader) {
        String[] parts = line.split(",", -1);
        if (parts.length <= 1) {
            return line;
        }
        String first = parts[0].replace("\"", "").trim();
        boolean indexColumn = isHeader ? first.isEmpty() : first.matches("\\d+");
        if (!indexColumn) {
            return line;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < parts.length; i++) {
            if (i > 1) {
                sb.append(",");
            }
            sb.append(parts[i]);
        }
        return sb.toString();
    }

    private static void recreateDir(Path dir) throws IOException {
        if (Files.exists(dir)) {
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
        }
        Files.createDirectories(dir);
    }

    private static String sanitizeName(String fileName) {
        String base = fileName.replaceAll("[^A-Za-z0-9._-]", "_");
        int dot = base.lastIndexOf('.');
        if (dot > 0) {
            base = base.substring(0, dot);
        }
        return base.toLowerCase(Locale.ROOT);
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }
}
