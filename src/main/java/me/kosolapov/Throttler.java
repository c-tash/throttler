package me.kosolapov;

public interface Throttler {

    /**
     * Try throttle the action, if rps limit is exceeded false is returned, else action is run and true is returned.
     * Differs from {@link #tryThrottleWithResult(Runnable)} in that it does not return {@link ThrottlingResult},
     * but only success/failure flag.
     *
     * @param runnable action to run.
     * @return true if action successfully goes through the rps limit, false if limit is exceeded.
     */
    boolean tryThrottle(Runnable runnable);

    /**
     * Try throttle the action, if rps limit is exceeded false {@link ThrottlingResult} is returned,
     * else action is run and true {@link ThrottlingResult} is returned. For every result a start time in a form of long
     * is also provided.
     *
     * @param runnable action to run.
     * @return {@link ThrottlingResult} with {@code time} filled with start time and {@code passed} as true
     * if action successfully goes through the rps limit, false if limit is exceeded.
     */
    ThrottlingResult tryThrottleWithResult(Runnable runnable);

    ThrottlingResult tryThrottleWithResult();
}
