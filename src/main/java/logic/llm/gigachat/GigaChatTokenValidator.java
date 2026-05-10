package logic.llm.gigachat;

import java.io.IOException;

/**
 * Validates token/auth key by performing a real lightweight test request.
 */
public class GigaChatTokenValidator {

    private final GigaChatApiClient apiClient;

    public GigaChatTokenValidator() {
        this(new GigaChatApiClient());
    }

    public GigaChatTokenValidator(GigaChatApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Tries token as Bearer first, then as auth key (Basic -> OAuth -> Bearer).
     * Returns true only if a real response is received.
     */
    public ValidationResult validate(String tokenOrAuthKey) throws IOException, InterruptedException {
        String prompt = "Привет! Ответь одним словом: ОК";

        try {
            GigaChatApiClient.ChatCompletionResult direct = apiClient.createChatCompletion(
                    tokenOrAuthKey, "GigaChat", null, prompt, 0.0, 16
            );
            return new ValidationResult(true, "Bearer token is valid.", direct.getContent(), "bearer");
        } catch (IOException bearerEx) {
            String accessToken = apiClient.fetchAccessToken(tokenOrAuthKey, GigaChatApiClient.DEFAULT_SCOPE);
            GigaChatApiClient.ChatCompletionResult viaOAuth = apiClient.createChatCompletion(
                    accessToken, "GigaChat", null, prompt, 0.0, 16
            );
            return new ValidationResult(true, "Auth key is valid (OAuth).", viaOAuth.getContent(), "auth_key");
        }
    }

    public record ValidationResult(boolean valid, String message, String responsePreview, String mode) {}
}
