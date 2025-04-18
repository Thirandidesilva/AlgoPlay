package org.example.algoplay.utils;

/**
 * Utility class to track execution time of algorithms
 */
public class TimeTracker {
    private long startTime;
    private long endTime;

    /**
     * Start the timer
     */
    public void start() {
        startTime = System.currentTimeMillis();
    }

    /**
     * Stop the timer and return elapsed time in milliseconds
     * @return elapsed time in milliseconds
     */
    public long stop() {
        endTime = System.currentTimeMillis();
        return getElapsedTime();
    }

    /**
     * Get elapsed time in milliseconds
     * @return elapsed time
     */
    public long getElapsedTime() {
        return endTime - startTime;
    }

    /**
     * Reset the timer
     */
    public void reset() {
        startTime = 0;
        endTime = 0;
    }

    /**
     * Check if timer is running
     * @return true if timer is running
     */
    public boolean isRunning() {
        return startTime > 0 && endTime == 0;
    }

    /**
     * Format elapsed time as a string
     * @return formatted time string
     */
    public String getFormattedTime() {
        long elapsedTime = getElapsedTime();

        if (elapsedTime < 1000) {
            return elapsedTime + " ms";
        } else if (elapsedTime < 60000) {
            return String.format("%.2f sec", elapsedTime / 1000.0);
        } else {
            long minutes = elapsedTime / 60000;
            long seconds = (elapsedTime % 60000) / 1000;
            return String.format("%d min %d sec", minutes, seconds);
        }
    }

    /**
     * Utility method to time and return the execution duration of a runnable
     * @param runnable The code to time
     * @return Execution time in milliseconds
     */
    public static long time(Runnable runnable) {
        TimeTracker tracker = new TimeTracker();
        tracker.start();
        runnable.run();
        return tracker.stop();
    }
}