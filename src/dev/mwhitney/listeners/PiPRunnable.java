package dev.mwhitney.listeners;

/**
 * An interface similarly structured to {@link Runnable} which simply contains a
 * run method. However, unlike {@link Runnable}, this is intended to be used in
 * the contexts which may throw exceptions.
 * 
 * @author mwhitney57
 */
@FunctionalInterface
public interface PiPRunnable {
    /**
     * A method to run code which may possibly throw an {@link Exception}.
     * 
     * @throws Exception if the code threw an exception.
     */
    public void run() throws Exception;
    /**
     * Checks if the passed {@link Exception} is covered by this runnable. Covered
     * exceptions are expected to be possibly thrown by the runnable's
     * {@link #run()} method.
     * 
     * @param e - the {@link Exception} to check for coverage.
     * @return <code>true</code> if the exception is covered; <code>false</code>
     *         otherwise.
     */
    public default boolean covers(final Exception e) {
        return (e != null);
    }
}
