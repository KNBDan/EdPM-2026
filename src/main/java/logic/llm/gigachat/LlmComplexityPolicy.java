package logic.llm.gigachat;

/**
 * Strategy for converting measured LLM latency to V-block complexity coefficient O.
 */
public interface LlmComplexityPolicy {
    int mapLatencyToO(long latencyMs);
}

