package me.kosolapov;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

public class SimpleThrottlerTest {

    private static final Runnable EMPTY_RUNNABLE = () -> {};
    private static final TimeCounter TEST_COUNTER = SimpleThrottler.TIME_COUNTER;

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionOnZeroRps() throws Exception {
        new SimpleThrottler(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionOnNegativeRps() throws Exception {
        new SimpleThrottler(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionOnMoreThan100000Rps() throws Exception {
        new SimpleThrottler(100001);
    }

    @Test
    public void shouldRpsOneButNotTwo() throws Exception {
        final int rps = 1;
        final int clientRps = rps * 10;
        final int requestCount = clientRps * 4;
        shouldRpsNButNotNPlusOne(rps, clientRps, requestCount);
    }

    @Test
    public void shouldRpsOneButNotTwoOneRequest() throws Exception {
        final int rps = 1;
        shouldRpsNButNotNPlusOne(rps, rps, rps);
    }

    @Test
    public void shouldRpsTenButNotEleven() throws Exception {
        final int rps = 10;
        final int clientRps = rps * 10;
        final int requestCount = clientRps * 4;
        shouldRpsNButNotNPlusOne(rps, clientRps, requestCount);
    }

    @Test
    public void shouldRps100ButNot101() throws Exception {
        final int rps = 100;
        final int clientRps = rps * 10;
        final int requestCount = clientRps * 4;
        shouldRpsNButNotNPlusOne(rps, clientRps, requestCount);
    }

    @Test
    public void shouldRps1000ButNot1001() throws Exception {
        final int rps = 1000;
        final int clientRps = rps * 10;
        final int requestCount = clientRps * 4;
        shouldRpsNButNotNPlusOne(rps, clientRps, requestCount);
    }


    @Test
    public void shouldRps1000ButNot1001Simple() throws Exception {
        final int rps = 1000;
        final SimpleThrottler throttler = new SimpleThrottler(rps);
        for (int i = 0; i < 1000; i++) {
            assertTrue(throttler.tryThrottle(EMPTY_RUNNABLE));
        }
        assertFalse(throttler.tryThrottle(EMPTY_RUNNABLE));
    }

    @Test
    public void testRps1000OnBorder() throws Exception {
        final int rps = 1000;
        final SimpleThrottler throttler = new SimpleThrottler(rps);
        final List<ThrottlingResult> results = new ArrayList<>(rps*2);
        final ThrottlingResult result = throttler.tryThrottleWithResult(EMPTY_RUNNABLE);
        results.add(result);
        busyWait(900*1000*1000);
        for (int i = 0; i < 999; i++) {
            results.add(throttler.tryThrottleWithResult(EMPTY_RUNNABLE));
        }
        busyWait(101*1000*1000);
        for (int i = 0; i < 1000; i++) {
            results.add(throttler.tryThrottleWithResult(EMPTY_RUNNABLE));
        }
        checkRps(results, rps);
    }

    private void busyWait(long waitTime) {
        final long start = System.nanoTime();
        long end;
        do {
            end = System.nanoTime();
        } while(end - start < waitTime);
    }


    @Test
    public void shouldRps10000ButNot10001() throws Exception {
        final int rps = 10000;
        final int clientRps = rps * 10;
        final int requestCount = clientRps * 4;
        shouldRpsNButNotNPlusOne(rps, clientRps, requestCount);
    }

    @Test
    public void shouldRps100000ButNot100001() throws Exception {
        final int rps = 100000;
        final int clientRps = rps * 10;
        final int requestCount = clientRps * 4;
        shouldRpsNButNotNPlusOne(rps, clientRps, requestCount);
    }

    @Test
    public void shouldRps100000ButNot100001WithX13ClientRps() throws Exception {
        final int rps = 100000;
        final int clientRps = rps * 13;
        final int requestCount = clientRps * 4;
        shouldRpsNButNotNPlusOne(rps, clientRps, requestCount);
    }

    private void shouldRpsNButNotNPlusOne(int rps, int clientRps, int requestCount) {
        final SimpleThrottler throttler = new SimpleThrottler(rps);
        final List<ThrottlingResult> results = new ArrayList<>(requestCount);
        final long waitNano = TEST_COUNTER.getSecondAsTime() / clientRps;
        for (int i = 0; i < requestCount; i++) {
            final ThrottlingResult throttlingResult = throttler.tryThrottleWithResult(EMPTY_RUNNABLE);
            results.add(throttlingResult);
            final long start = throttlingResult.getTime();
            long end;
            do {
                end = System.nanoTime();
            } while (end - start < waitNano);
        }
        checkRps(results, rps);
    }

    private void checkRps(List<ThrottlingResult> results, long rps) {
        final int requestCount = results.size();
        int leftCursor = 0;
        int rightCursor = 1;
        int actualRps = (results.get(0).isPassed() ? 1 : 0);
        while (leftCursor < requestCount && rightCursor < requestCount) {
            while (rightCursor < requestCount
                    && TEST_COUNTER.insideSecond(results.get(leftCursor).getTime(),
                                                 results.get(rightCursor).getTime())) {
                actualRps += (results.get(rightCursor).isPassed() ? 1 : 0);
                assertTrue(actualRps <= rps);
                rightCursor++;
            }

            if (rightCursor == requestCount) {
                break;
            }

            while (leftCursor < requestCount
                    && !TEST_COUNTER.insideSecond(results.get(leftCursor).getTime(),
                                                  results.get(rightCursor).getTime())) {
                actualRps -= (results.get(leftCursor).isPassed() ? 1 : 0);
                leftCursor++;
            }
        }
    }

}