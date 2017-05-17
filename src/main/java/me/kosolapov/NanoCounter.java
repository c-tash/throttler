package me.kosolapov;

/**
 * Implementation of {@link TimeCounter}
 * based on {@link System#nanoTime()}.
 */
public class NanoCounter implements TimeCounter {

    /**
     * Amount of nanoseconds in a second.
     */
    public static final long NANO_IN_SECOND = 1000000000L;

    /**
     * Error to be used in comparisons.
     */
    private static final long EPSILON_ERROR = 0L;

    @Override
    public long getTime() {
        return System.nanoTime();
    }

    @Override
    public boolean insideSecond(long first, long second) {
        return first < second ? (second - first) < NANO_IN_SECOND - EPSILON_ERROR
                : (first - second) < NANO_IN_SECOND - EPSILON_ERROR;
    }

    @Override
    public long getSecondAsTime() {
        return NANO_IN_SECOND;
    }
}