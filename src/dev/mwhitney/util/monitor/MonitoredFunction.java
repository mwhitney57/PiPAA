package dev.mwhitney.util.monitor;

/**
 * A function to be monitored by {@link ThreadMonitor} which takes a single
 * parameter, returns a value, and may throw an {@link Exception} or a
 * {@link InterruptedException}.
 * 
 * @param <T> - the function's parameter type.
 * @param <R> - the function's return type.
 * @param <E> - the function's thrown {@link Exception}.
 * @author mwhitney57
 * @since 0.9.5
 */
@FunctionalInterface
public interface MonitoredFunction<T, R, E extends Exception> {
    /**
     * Executes the passed function. Expected to be called after setting up a
     * monitor on the calling thread.
     * 
     * @param input - the input parameter of the function.
     * @return the return value of the function.
     * @throws InterruptedException if the execution was interrupted, likely by the
     *                              monitor.
     * @throws E                    if the function itself threw an exception.
     */
    public R monitor(T input) throws InterruptedException, E;
}
