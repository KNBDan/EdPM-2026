package logic.description.microservice;

public final class MicroserviceGenerationOptions {
    private final LlmSimulationMode llmSimulationMode;
    private final String llmToken;

    private MicroserviceGenerationOptions(LlmSimulationMode llmSimulationMode, String llmToken) {
        this.llmSimulationMode = llmSimulationMode == null ? LlmSimulationMode.DISABLED : llmSimulationMode;
        this.llmToken = llmToken == null ? "" : llmToken.trim();
    }

    public static MicroserviceGenerationOptions defaults() {
        return new MicroserviceGenerationOptions(LlmSimulationMode.DISABLED, "");
    }

    public static MicroserviceGenerationOptions withLlmSimulationMode(LlmSimulationMode mode) {
        return new MicroserviceGenerationOptions(mode, "");
    }

    public static MicroserviceGenerationOptions withLlmSettings(LlmSimulationMode mode, String llmToken) {
        return new MicroserviceGenerationOptions(mode, llmToken);
    }

    public LlmSimulationMode getLlmSimulationMode() {
        return llmSimulationMode;
    }

    public String getLlmToken() {
        return llmToken;
    }
}
