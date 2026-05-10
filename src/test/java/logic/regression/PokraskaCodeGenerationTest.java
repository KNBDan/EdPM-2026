package logic.regression;

import EPM.mdi;
import domain.DiagramLoadResult;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.prefs.Preferences;
import logic.description.DescriptionService;
import logic.description.microservice.MicroserviceGenerationResult;
import logic.description.microservice.MicroserviceRBundleGenerator;
import logic.serialization.DiagramSerializer;
import logic.serialization.model.ConvertedObject;
import logic.serialization.model.GenerationSettings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Regression-style unit tests for model->code generation using real fixtures.
 * These tests intentionally do strict content comparison to expose real drifts.
 */
public class PokraskaCodeGenerationTest {

    private static final String FIXTURE_MODEL = "fixtures/pokraska/model.json";
    private static final String FIXTURE_EXPECTED_MANUAL = "fixtures/pokraska/expected_manual.R";
    private static final String FIXTURE_EXPECTED_MICRO = "fixtures/pokraska/expected_microservice.R";

    @Test
    void linearRGenerationShouldMatchPokraskaFixture() throws Exception {
        configurePrefsForPokraska();

        DiagramLoadResult loaded = loadFixtureModel();
        ConvertedObject converted = toConverted(loaded);

        DescriptionService descriptionService = new DescriptionService();
        String pseudoCode = descriptionService.generateDescription(converted);
        String actualLinearR = descriptionService.generateRCode(loaded.getFigures(), pseudoCode);
        String expectedLinearR = readFixtureText(FIXTURE_EXPECTED_MANUAL);

        Path outDir = Path.of("target", "test-artifacts", "pokraska");
        Files.createDirectories(outDir);
        Files.writeString(outDir.resolve("actual_linear_generated.R"), actualLinearR, StandardCharsets.UTF_8);

        assertRCodeEquals("linear R generation", expectedLinearR, actualLinearR);
    }

    @Test
    void microserviceRGenerationShouldMatchPokraskaFixture() throws Exception {
        configurePrefsForPokraska();

        DiagramLoadResult loaded = loadFixtureModel();
        ConvertedObject converted = toConverted(loaded);

        DescriptionService descriptionService = new DescriptionService();
        String pseudoCode = descriptionService.generateDescription(converted);
        String linearR = descriptionService.generateRCode(loaded.getFigures(), pseudoCode);

        MicroserviceRBundleGenerator generator = new MicroserviceRBundleGenerator();
        Path outDir = Path.of("target", "test-artifacts", "pokraska-micro");
        Files.createDirectories(outDir);
        MicroserviceGenerationResult result = generator.generateBundle(linearR, outDir);
        String actualMicroR = Files.readString(result.getOutputDirectory().resolve("run_microservice.R"), StandardCharsets.UTF_8);
        String expectedMicroR = readFixtureText(FIXTURE_EXPECTED_MICRO);

        Files.writeString(outDir.resolve("actual_micro_generated.R"), actualMicroR, StandardCharsets.UTF_8);

        assertRCodeEquals("microservice R generation", expectedMicroR, actualMicroR);
    }

    @Test
    void oldModelWithoutGenerationSettingsShouldLoadWithDefaults() throws Exception {
        DiagramLoadResult loaded = loadFixtureModel();
        GenerationSettings s = loaded.getGenerationSettings();
        Assertions.assertNotNull(s, "Generation settings must always be available.");
        Assertions.assertEquals(1000, s.getNValue());
        Assertions.assertEquals(1, s.getIValue());
        Assertions.assertEquals(1, s.getFpValue());
        Assertions.assertEquals(1, s.getStartId());
        Assertions.assertEquals(1, s.getStepId());
        Assertions.assertEquals("result", s.getXesName());
    }

    @Test
    void saveModelShouldPersistGenerationSettingsToJson() throws Exception {
        configurePrefsForPokraska();
        // Use non-default values to make persistence obvious.
        Preferences prefs = mdi.prefsMdi;
        prefs.put("NValue", "777");
        prefs.put("IValue", "9");
        prefs.put("FPValue", "13");
        prefs.put("startId", "42");
        prefs.put("stepId", "7");
        prefs.putBoolean("graphState", false);
        prefs.putBoolean("xesState", true);
        prefs.putBoolean("oActiveState", false);
        prefs.put("xesName", "my_xes_name");

        DiagramLoadResult loaded = loadFixtureModel();
        ConvertedObject converted = toConverted(loaded);
        DiagramSerializer serializer = new DiagramSerializer();
        Path out = Path.of("target", "test-artifacts", "pokraska", "saved_with_generation_settings.json");
        Files.createDirectories(out.getParent());
        serializer.saveToJson(out.toString(), converted);

        String json = Files.readString(out, StandardCharsets.UTF_8);
        Assertions.assertTrue(json.contains("\"generationSettings\""));
        Assertions.assertTrue(json.contains("\"nValue\" : 777"));
        Assertions.assertTrue(json.contains("\"iValue\" : 9"));
        Assertions.assertTrue(json.contains("\"fpValue\" : 13"));
        Assertions.assertTrue(json.contains("\"startId\" : 42"));
        Assertions.assertTrue(json.contains("\"stepId\" : 7"));
        Assertions.assertTrue(json.contains("\"xesName\" : \"my_xes_name\""));
    }

    private static void configurePrefsForPokraska() {
        Preferences prefs = mdi.prefsMdi;
        prefs.put("NValue", "1000");
        prefs.put("IValue", "1");
        prefs.put("FPValue", "1");
        prefs.putBoolean("graphState", true);
        prefs.putBoolean("xesState", true);
        prefs.put("xesName", "xesik");
        prefs.put("startId", "2");
        prefs.put("stepId", "66");
        prefs.putBoolean("oActiveState", true);
    }

    private static DiagramLoadResult loadFixtureModel() throws IOException {
        Path tempModel = materializeFixture(FIXTURE_MODEL, ".json");
        DiagramSerializer serializer = new DiagramSerializer();
        return serializer.loadFromJson(tempModel.toString());
    }

    private static ConvertedObject toConverted(DiagramLoadResult loaded) {
        DiagramSerializer serializer = new DiagramSerializer();
        return serializer.createConvertedObject(
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
    }

    private static Path materializeFixture(String resourcePath, String suffix) throws IOException {
        try (InputStream in = PokraskaCodeGenerationTest.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("Fixture not found in classpath: " + resourcePath);
            }
            Path tmp = Files.createTempFile("edpm-fixture-", suffix);
            Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
            tmp.toFile().deleteOnExit();
            return tmp;
        }
    }

    private static String readFixtureText(String resourcePath) throws IOException {
        try (InputStream in = PokraskaCodeGenerationTest.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("Fixture not found in classpath: " + resourcePath);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void assertRCodeEquals(String label, String expected, String actual) {
        String normalizedExpected = normalizeRCode(expected);
        String normalizedActual = normalizeRCode(actual);
        if (!normalizedExpected.equals(normalizedActual)) {
            String diff = firstDiff(normalizedExpected, normalizedActual);
            Assertions.fail(label + " mismatch.\n" + diff);
        }
    }

    private static String normalizeRCode(String code) {
        String normalized = code.replace("\r\n", "\n").replace("\r", "\n");
        normalized = normalized.replaceAll("(?m)^# Generated:.*$", "# Generated: <normalized>");
        normalized = normalized.replaceAll("(?m)[ \\t]+$", "");
        normalized = normalized.replaceAll("(?m)^\\s+$", "");
        return normalized.trim() + "\n";
    }

    private static String firstDiff(String expected, String actual) {
        List<String> exp = expected.lines().toList();
        List<String> act = actual.lines().toList();
        int max = Math.max(exp.size(), act.size());
        for (int i = 0; i < max; i++) {
            String e = i < exp.size() ? exp.get(i) : "<no line>";
            String a = i < act.size() ? act.get(i) : "<no line>";
            if (!e.equals(a)) {
                return "First diff at line " + (i + 1) + "\nEXPECTED: " + e + "\nACTUAL  : " + a;
            }
        }
        return "Content differs, but line-level first mismatch not found.";
    }
}
