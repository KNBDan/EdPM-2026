package logic.llm.gigachat;

/**
 * Default policy: O = ceil(latencyMs / 1000), minimum 1.
 */
public class LinearSecondsComplexityPolicy implements LlmComplexityPolicy {
    @Override
    public int mapLatencyToO(long latencyMs) {
        return Math.max(1, (int) Math.ceil(latencyMs / 1000.0));
    }
}

