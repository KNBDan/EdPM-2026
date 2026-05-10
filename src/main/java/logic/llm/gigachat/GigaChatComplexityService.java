package logic.llm.gigachat;

import java.io.IOException;

public class GigaChatComplexityService {

    private final GigaChatApiClient client;
    private final LlmComplexityPolicy complexityPolicy;

    public GigaChatComplexityService(GigaChatApiClient client) {
        this(client, new LinearSecondsComplexityPolicy());
    }

    public GigaChatComplexityService(GigaChatApiClient client, LlmComplexityPolicy complexityPolicy) {
        this.client = client;
        this.complexityPolicy = complexityPolicy;
    }

    public GigaChatComplexityService() {
        this(new GigaChatApiClient(), new LinearSecondsComplexityPolicy());
    }

    /**
     * Run prompt via already issued bearer token and measure request latency.
     */
    public LlmComplexityMeasurement measureWithBearerToken(String bearerToken,
                                                           String model,
                                                           String systemPrompt,
                                                           String userPrompt,
                                                           Double temperature,
                                                           Integer maxTokens) throws IOException, InterruptedException {
        long startNs = System.nanoTime();
        GigaChatApiClient.ChatCompletionResult result = client.createChatCompletion(
                bearerToken,
                model,
                systemPrompt,
                userPrompt,
                temperature,
                maxTokens
        );
        long endNs = System.nanoTime();

        long latencyMs = Math.max(1L, (endNs - startNs) / 1_000_000L);
        int complexityO = complexityPolicy.mapLatencyToO(latencyMs);

        return new LlmComplexityMeasurement(
                complexityO,
                latencyMs,
                result.getContent(),
                result.getUsage().getPromptTokens(),
                result.getUsage().getCompletionTokens(),
                result.getUsage().getTotalTokens(),
                result.getRawJson()
        );
    }

    /**
     * Run prompt using OAuth auth-key flow and measure latency.
     */
    public LlmComplexityMeasurement measureWithAuthKey(String authKey,
                                                       String scope,
                                                       String model,
                                                       String systemPrompt,
                                                       String userPrompt,
                                                       Double temperature,
                                                       Integer maxTokens) throws IOException, InterruptedException {
        String accessToken = client.fetchAccessToken(authKey, scope);
        return measureWithBearerToken(accessToken, model, systemPrompt, userPrompt, temperature, maxTokens);
    }

    public record LlmComplexityMeasurement(int complexityO,
                                           long latencyMs,
                                           String responseText,
                                           int promptTokens,
                                           int completionTokens,
                                           int totalTokens,
                                           String rawJson) {}
}
