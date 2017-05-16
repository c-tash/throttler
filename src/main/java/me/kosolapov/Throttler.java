package me.kosolapov;

import java.util.Deque;
import java.util.LinkedList;

/**
 * Throttler class allows to reduce rps to desired values.
 */
public class Throttler {

    /**
     * TimeCounter interface to provide time in form of long values
     * and compare values.
     */
    interface TimeCounter {

        /**
         * Gets long value of current timestamp.
         *
         * @return long value of current timestamp
         */
        long getTime();

        /**
         * Determines whether the two timestamps provided lie inside one second period.
         *
         * @param first first timestamp
         * @param second second timestamp
         * @return true if timestamps lie inside one second period, else false
         */
        boolean insideSecond(long first, long second);
    }

    /**
     * Implementation of {@link TimeCounter}
     * based on {@link System#nanoTime()}.
     */
    public static class NanoCounter implements TimeCounter {

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
    }

    /**
     * Data class to provide result of throttling try.
     */
    public static class ThrottlingResult {
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

    /**
     * Default time counter.
     */
    public static final TimeCounter TIME_COUNTER = new NanoCounter();

    private final int rps;

    private final TimeCounter timeCounter;

    private final Deque<Long> timesForRequests = new LinkedList<>();

    /**
     * Creates a new {@code Throttler} with specified rps (Request per second) limit.
     *
     * @param rps int value of desired rps limit. Should be between 1 and 100000.
     * @throws IllegalArgumentException if {@code rps} value specified is less than 1 or more than 100000.
     */
    public Throttler(int rps) {
        if (rps < 1 || rps > 100000) {
            throw new IllegalArgumentException("RPS should be between 1 and 100000.");
        }
        this.rps = rps;
        timeCounter = TIME_COUNTER;
    }

    /**
     * Try throttle the action, if rps limit is exceeded false is returned, else action is run and true is returned.
     * Differs from {@link #tryThrottleWithResult(Runnable)} in that it does not return {@link ThrottlingResult},
     * but only success/failure flag.
     *
     * @param runnable action to run.
     * @return true if action successfully goes through the rps limit, false if limit is exceeded.
     */
    public boolean tryThrottle(Runnable runnable) {
        final ThrottlingResult throttlingResult = tryThrottleWithResult(runnable);
        return throttlingResult.passed;
    }

    /**
     * Try throttle the action, if rps limit is exceeded false {@link ThrottlingResult} is returned,
     * else action is run and true {@link ThrottlingResult} is returned. For every result a start time in a form of long
     * is also provided.
     *
     * @param runnable action to run.
     * @return {@link ThrottlingResult} with {@code time} filled with start time and {@code passed} as true
     * if action successfully goes through the rps limit, false if limit is exceeded.
     */
    public ThrottlingResult tryThrottleWithResult(Runnable runnable) {
        final long time = timeCounter.getTime();
        if (timesForRequests.size() < rps) {
            timesForRequests.addLast(time);
        } else {
            if (timeCounter.insideSecond(timesForRequests.getFirst(), time)) {
                return new ThrottlingResult(false, time);
            } else {
                timesForRequests.removeFirst();
                timesForRequests.addLast(time);
            }
        }
        runnable.run();
        return new ThrottlingResult(true, time);
    }

}
