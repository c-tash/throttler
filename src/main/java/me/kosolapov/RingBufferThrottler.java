package me.kosolapov;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLongArray;

/**
 * @author Kirill Kosolapov (k.kosolapov@samsung.com)
 */
public class RingBufferThrottler implements Throttler {

    private final int rps;
    private final TimeCounter timeCounter = new NanoCounter();

    private final AtomicLongArray ring;
    private final AtomicInteger cursor = new AtomicInteger(0);

    public RingBufferThrottler(int rps) {
        this.rps = rps;
        final long baseTime = System.nanoTime() - timeCounter.getSecondAsTime() - 1;
        ring = new AtomicLongArray(rps);
        for (int i = 0; i < rps; i++) {
            ring.set(i, baseTime);
        }
    }

    @Override
    public boolean tryThrottle(Runnable runnable) {
        return false;
    }

    @Override
    public ThrottlingResult tryThrottleWithResult(Runnable runnable) {
        return null;
    }

    @Override
    public ThrottlingResult tryThrottleWithResult() {
        while (true) {
            final long time = timeCounter.getTime();
            final int curCursor = cursor.get();
            final int ringCursor = curCursor % rps;
            final long oldestTime = ring.get(ringCursor);
            if (curCursor == cursor.get()) {
                if (timeCounter.insideSecond(oldestTime, time)) {
                    return new ThrottlingResult(false, time);
                } else {
                    final int newCursor = curCursor < Integer.MAX_VALUE ? curCursor+1 : (ringCursor + 1);
                    if (cursor.compareAndSet(curCursor, newCursor)) {
                        if (ring.compareAndSet(ringCursor, oldestTime, time)) {
                            return new ThrottlingResult(true, time);
                        }
                    }
                }
            }
        }
    }
}
