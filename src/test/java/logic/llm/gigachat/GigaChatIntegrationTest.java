package logic.llm.gigachat;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * Real integration tests for GigaChat API.
 *
 * How to run valid-token scenario:
 * 1) set env GIGACHAT_TEST_TOKEN to your token or auth key
 * 2) run mvn test -Dtest=GigaChatIntegrationTest
 */
public class GigaChatIntegrationTest {

    @Test
    void emptyTokenShouldFailValidation() {
        GigaChatTokenValidator validator = new GigaChatTokenValidator(new GigaChatApiClient());
        assertThrows(Exception.class, () -> validator.validate(""));
    }

    @Test
    void invalidTokenShouldFailValidation() {
        GigaChatTokenValidator validator = new GigaChatTokenValidator(new GigaChatApiClient());
        assertThrows(Exception.class, () -> validator.validate("invalid_token_value_123"));
    }

    @Test
    void validTokenShouldPassAndReturnAnswer() throws Exception {
        String token = System.getenv("GIGACHAT_TEST_TOKEN");
        Assumptions.assumeTrue(token != null && !token.isBlank(),
                "Set GIGACHAT_TEST_TOKEN to run this integration test");

        GigaChatTokenValidator validator = new GigaChatTokenValidator(new GigaChatApiClient());
        GigaChatTokenValidator.ValidationResult validation = validator.validate(token);

        assertTrue(validation.valid(), "Expected valid token/auth key");
        assertNotNull(validation.responsePreview(), "Response preview should not be null");
        assertFalse(validation.responsePreview().isBlank(), "Response preview should not be blank");
    }

    @Test
    void validTokenShouldHandleSimplePrompt() throws Exception {
        String token = System.getenv("GIGACHAT_TEST_TOKEN");
        Assumptions.assumeTrue(token != null && !token.isBlank(),
                "Set GIGACHAT_TEST_TOKEN to run this integration test");

        GigaChatComplexityService service = new GigaChatComplexityService(new GigaChatApiClient());
        GigaChatComplexityService.LlmComplexityMeasurement m;
        try {
            m = service.measureWithBearerToken(token, "GigaChat", null, "Ответь одним словом: ОК", 0.0, 16);
        } catch (Exception bearerFail) {
            m = service.measureWithAuthKey(token, GigaChatApiClient.DEFAULT_SCOPE,
                    "GigaChat", null, "Ответь одним словом: ОК", 0.0, 16);
        }

        assertNotNull(m);
        assertTrue(m.latencyMs() > 0, "Latency should be measured");
        assertTrue(m.complexityO() >= 1, "Complexity O should be >= 1");
        assertNotNull(m.responseText(), "Response text should not be null");
        assertFalse(m.responseText().isBlank(), "Response text should not be blank");
        assertNotEquals(0, m.totalTokens(), "Token usage should be non-zero for real answer");
    }
}
