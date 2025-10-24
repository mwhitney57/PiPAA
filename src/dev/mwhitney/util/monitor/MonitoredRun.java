package dev.mwhitney.util.monitor;

/**
 * A function to be monitored by {@link ThreadMonitor} which may throw an
 * {@link Exception} or a {@link InterruptedException}.
 * 
 * @param <E> - the function's thrown {@link Exception}.
 * @author mwhitney57
 * @since 0.9.5
 */
@FunctionalInterface
public interface MonitoredRun <E extends Exception> {
    /**
     * Executes the passed function. Expected to be called after setting up a
     * monitor on the calling thread.
     * 
     * @throws InterruptedException if the execution was interrupted, likely by the
     *                              monitor.
     * @throws E                    if the function itself threw an exception.
     */
    public void monitor() throws InterruptedException, E;
}
