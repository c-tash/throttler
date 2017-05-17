package me.kosolapov;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.Test;

/**
 * @author Kirill Kosolapov (k.kosolapov@samsung.com)
 */
public class ConcurrentThrottlerTest {

    private static final Runnable EMPTY_RUNNABLE = () -> {};
    private static final TimeCounter TEST_COUNTER = SimpleThrottler.TIME_COUNTER;

    @Test
    public void testRps() throws Exception {
        final int rps = 100000;
        final int clientRps = rps * 2;
        final ConcurrentThrottler throttler = new ConcurrentThrottler(rps);
        final int clientCount = 8;
        final List<ThrottlingResult> results = new ArrayList<>(clientRps*clientCount);
        final List<CompletableFuture<List<ThrottlingResult>>> clients = new ArrayList<>(clientCount);
        for (int i = 0; i < clientCount; i++) {
            clients.add(CompletableFuture.supplyAsync(() -> {
                final List<ThrottlingResult> throttlingResults = new ArrayList<>(clientRps);
                for (int j = 0; j < clientRps; j++) {
                    throttlingResults.add(throttler.tryThrottleWithResult(EMPTY_RUNNABLE));
                }
                return throttlingResults;
            }));
        }
        for (final CompletableFuture<List<ThrottlingResult>> client : clients) {
            results.addAll(client.get());
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