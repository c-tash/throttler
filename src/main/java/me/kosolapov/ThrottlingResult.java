package me.kosolapov;

/**
 * Data class to provide result of throttling try.
 */
public class ThrottlingResult {
    private final boolean passed;
    private final long time;

    /**
     * Creates new instance of result of a throttling attempt.
     *
     * @param passed whether the action passed through the throttler.
     * @param time start time of attempt in form of a long value, calculated by {@link TimeCounter}
     */
    public ThrottlingResult(boolean passed, long time) {
        this.passed = passed;
        this.time = time;
    }

    public boolean isPassed() {
        return passed;
    }

    public long getTime() {
        return time;
    }
}
