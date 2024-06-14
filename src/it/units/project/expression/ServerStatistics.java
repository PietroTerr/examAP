package it.units.project.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerStatistics {
    private final List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());

    public void recordResponse(long responseTime) {
        responseTimes.add(responseTime);
    }

    public int getTotalResponses() {
        return responseTimes.size();
    }

    public double getAverageResponseTime() {
        synchronized (responseTimes) {
            if (responseTimes.isEmpty()) return 0.0;
            long totalResponseTime = 0;
            for (long time : responseTimes) {
                totalResponseTime += time;
            }
            return (double) totalResponseTime / responseTimes.size();
        }
    }

    public long getMaxResponseTime() {
        synchronized (responseTimes) {
            if (responseTimes.isEmpty()) return 0;
            long maxResponseTime = Long.MIN_VALUE;
            for (long time : responseTimes) {
                if (time > maxResponseTime) {
                    maxResponseTime = time;
                }
            }
            return maxResponseTime;
        }
    }
}
