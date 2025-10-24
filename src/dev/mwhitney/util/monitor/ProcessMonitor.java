package dev.mwhitney.util.monitor;

/**
 * An interface to be implemented by classes which implement or handle a
 * {@link ThreadMonitor} and execute external processes via
 * {@link ProcessBuilder} and {@link Process}.
 * <p>
 * In order for an implementation of this interface to be useful and valid, it
 * must simply provide a {@link ThreadMonitor} via an override of
 * {@link #getMonitor()}. The implementing class need not have its own
 * {@link ThreadMonitor} member, but should at least provide one.
 * <p>
 * The monitors provide a critical function: the ability to interrupt specific
 * threads at any time. This prevents rogue processes from continuing their
 * execution, especially when they are no longer needed. For example, if a
 * window is performing a task, and that window closes, its task becomes
 * unnecessary. It should be interrupted to prevent unwanted background
 * execution and improve performance.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public interface ProcessMonitor {
    /**
     * Gets the monitor used by this class for monitoring threads which are
     * executing, or awaiting results from, a {@link Process}.
     * 
     * @return the {@link ThreadMonitor} instance.
     */
    public ThreadMonitor getMonitor();
    /**
     * Checks if the value provided by {@link #getMonitor()} is
     * non-<code>null</code>.
     * 
     * @return <code>true</code> if a valid monitor is retrievable;
     *         <code>false</code> otherwise.
     */
    default public boolean hasMonitor() {
        return getMonitor() != null;
    }
    /**
     * A simplified call to {@link ThreadMonitor#interruptAll()} that provides
     * <code>null</code> safety by checking {@link #hasMonitor()} first.
     */
    default public void interruptAll() {
        if (hasMonitor()) getMonitor().interruptAll();
    }
    /**
     * A simplified call to {@link ThreadMonitor#interruptLinked(Object)} that
     * provides <code>null</code> safety by checking {@link #hasMonitor()} first.
     * 
     * @param link - the object linked to the threads that should be interrupted.
     */
    default public void interruptLinked(Object link) {
        if (hasMonitor()) getMonitor().interruptLinked(link);
    }
}
