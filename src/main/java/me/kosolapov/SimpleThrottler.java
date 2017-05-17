package me.kosolapov;

import java.util.Deque;
import java.util.LinkedList;

/**
 * SimpleThrottler class allows to reduce rps to desired values.
 */
public class SimpleThrottler implements Throttler {

    /**
     * Default time counter.
     */
    public static final TimeCounter TIME_COUNTER = new NanoCounter();

    private final int rps;

    private final TimeCounter timeCounter;

    private final Deque<Long> timesForRequests = new LinkedList<>();

    /**
     * Creates a new {@code SimpleThrottler} with specified rps (Request per second) limit.
     *
     * @param rps int value of desired rps limit. Should be between 1 and 100000.
     * @throws IllegalArgumentException if {@code rps} value specified is less than 1 or more than 100000.
     */
    public SimpleThrottler(int rps) {
        if (rps < 1 || rps > 100000) {
            throw new IllegalArgumentException("RPS should be between 1 and 100000.");
        }
        this.rps = rps;
        timeCounter = TIME_COUNTER;
    }

    @Override
    public boolean tryThrottle(Runnable runnable) {
        final ThrottlingResult throttlingResult = tryThrottleWithResult(runnable);
        return throttlingResult.isPassed();
    }

    @Override
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
