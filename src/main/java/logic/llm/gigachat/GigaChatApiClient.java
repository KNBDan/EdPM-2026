package logic.llm.gigachat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Minimal GigaChat API client (OAuth + chat completions).
 *
 * Endpoints are configurable because deployments can differ.
 */
public class GigaChatApiClient {

    public static final String DEFAULT_OAUTH_URL = "https://ngw.devices.sberbank.ru:9443/api/v2/oauth";
    public static final String DEFAULT_API_BASE_URL = "https://gigachat.devices.sberbank.ru/api/v1";
    public static final String DEFAULT_SCOPE = "GIGACHAT_API_PERS";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String oauthUrl;
    private final String apiBaseUrl;
    private final Duration timeout;

    public GigaChatApiClient() {
        this(createUnsafeHttpClient(), new ObjectMapper(), DEFAULT_OAUTH_URL, DEFAULT_API_BASE_URL, Duration.ofSeconds(60));
    }

    public GigaChatApiClient(HttpClient httpClient,
                             ObjectMapper objectMapper,
                             String oauthUrl,
                             String apiBaseUrl,
                             Duration timeout) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.oauthUrl = oauthUrl;
        this.apiBaseUrl = apiBaseUrl;
        this.timeout = timeout;
    }

    private static HttpClient createUnsafeHttpClient() {
        try {
            TrustManager[] trustAll = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[0];
                        }
                    }
            };
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAll, new java.security.SecureRandom());

            SSLParameters sslParameters = new SSLParameters();
            sslParameters.setEndpointIdentificationAlgorithm(null);

            return HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .sslParameters(sslParameters)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create unsafe HTTP client", e);
        }
    }

    /**
     * Exchanges API auth key for access token.
     *
     * @param authKey base64 API auth key for Authorization: Basic ...
     */
    public String fetchAccessToken(String authKey) throws IOException, InterruptedException {
        return fetchAccessToken(authKey, DEFAULT_SCOPE);
    }

    public String fetchAccessToken(String authKey, String scope) throws IOException, InterruptedException {
        String body = "scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(oauthUrl))
                .timeout(timeout)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .header("RqUID", UUID.randomUUID().toString())
                .header("Authorization", "Basic " + authKey)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        ensure2xx(response, "OAuth token request failed");

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode tokenNode = root.get("access_token");
        if (tokenNode == null || tokenNode.asText().isBlank()) {
            throw new IOException("OAuth response does not contain access_token");
        }
        return tokenNode.asText();
    }

    public ChatCompletionResult createChatCompletion(String accessToken,
                                                     String model,
                                                     String systemPrompt,
                                                     String userPrompt,
                                                     Double temperature,
                                                     Integer maxTokens) throws IOException, InterruptedException {
        List<Message> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(new Message("system", systemPrompt));
        }
        messages.add(new Message("user", userPrompt));

        ChatRequest payload = new ChatRequest(
                model == null || model.isBlank() ? "GigaChat" : model,
                messages,
                temperature,
                maxTokens
        );

        String body = objectMapper.writeValueAsString(payload);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiBaseUrl + "/chat/completions"))
                .timeout(timeout)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        ensure2xx(response, "Chat completion request failed");

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode choices = root.path("choices");

        String content = "";
        if (choices.isArray() && choices.size() > 0) {
            content = choices.get(0).path("message").path("content").asText("");
        }

        Usage usage = new Usage(
                root.path("usage").path("prompt_tokens").asInt(0),
                root.path("usage").path("completion_tokens").asInt(0),
                root.path("usage").path("total_tokens").asInt(0)
        );

        return new ChatCompletionResult(content, usage, response.body());
    }

    private static void ensure2xx(HttpResponse<String> response, String prefix) throws IOException {
        int code = response.statusCode();
        if (code < 200 || code >= 300) {
            throw new IOException(prefix + ": HTTP " + code + " | body=" + response.body());
        }
    }

    public static class ChatCompletionResult {
        private final String content;
        private final Usage usage;
        private final String rawJson;

        public ChatCompletionResult(String content, Usage usage, String rawJson) {
            this.content = content;
            this.usage = usage;
            this.rawJson = rawJson;
        }

        public String getContent() {
            return content;
        }

        public Usage getUsage() {
            return usage;
        }

        public String getRawJson() {
            return rawJson;
        }
    }

    public static class Usage {
        private final int promptTokens;
        private final int completionTokens;
        private final int totalTokens;

        public Usage(int promptTokens, int completionTokens, int totalTokens) {
            this.promptTokens = promptTokens;
            this.completionTokens = completionTokens;
            this.totalTokens = totalTokens;
        }

        public int getPromptTokens() {
            return promptTokens;
        }

        public int getCompletionTokens() {
            return completionTokens;
        }

        public int getTotalTokens() {
            return totalTokens;
        }
    }

    public static class Message {
        private final String role;
        private final String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }
    }

    public static class ChatRequest {
        private final String model;
        private final List<Message> messages;
        private final Double temperature;
        private final Integer max_tokens;

        public ChatRequest(String model, List<Message> messages, Double temperature, Integer max_tokens) {
            this.model = model;
            this.messages = messages;
            this.temperature = temperature;
            this.max_tokens = max_tokens;
        }

        public String getModel() {
            return model;
        }

        public List<Message> getMessages() {
            return messages;
        }

        public Double getTemperature() {
            return temperature;
        }

        public Integer getMax_tokens() {
            return max_tokens;
        }
    }
}
