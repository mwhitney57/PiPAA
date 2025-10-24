package dev.mwhitney.util.monitor;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import dev.mwhitney.main.CFExec;

/**
 * Acts as a centralized monitor for one or more {@link Thread} objects.
 * <p>
 * Executing multiple asynchronous operations via {@link CompletableFuture} can
 * be helpful, but if there are blocking operations that need to be
 * interruptible, things may get tedious. This class provides an easy way for
 * code to add threads later down the line, and interrupt all of its saved
 * threads at once.
 * <p>
 * Furthermore, this class allows thread <b>linking</b>. A "link object" can be
 * provided when adding a thread. This can be useful later if only those linked
 * threads must be interrupted. The link object can be anything, so it is often
 * wise to simply pass a {@code new Object()}. However, it may be better to pass
 * other instances instead, depending on implementation.
 * <p>
 * There are convenience methods for easily executing code that should be
 * monitored and interruptible, such as
 * {@link #supply(MonitoredFunction, Object)} or
 * {@link #supplyAsyncLinked(Object, MonitoredFunction, Object)}.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public class ThreadMonitor {
    /** A map of threads to their linked objects. If no object was linked, the default {@link #noLinkObj} is used as the value. */
    private final ConcurrentHashMap<Thread, Object> threads = new ConcurrentHashMap<>();
    /** A plain object that is used for monitored threads with no link. */
    private final Object noLinkObj = new Object();
    
    /**
     * Adds the passed {@link Thread} to the monitor.
     * <p>
     * Since the monitor is largely used for interrupting threads en-masse, it is
     * advised that there is some catch for the thrown {@link InterruptedException}
     * caused by {@link #interruptAll()}.
     * 
     * @param t - the thread to add.
     * @return this {@link ThreadMonitor} instance.
     */
    public ThreadMonitor add(Thread t) {
        addLinked(t, null);
        return this;
    }

    /**
     * Adds the passed {@link Thread} to the monitor and links it to the passed
     * object. Passing a <code>null</code> link object is equivalent to calling
     * {@link #add(Thread)}.
     * <p>
     * A linked object solely serves to associate a monitored thread with that
     * object. Specific threads can be interrupted that are associated with a
     * particular object instead of interrupting all of them.
     * <p>
     * Since the monitor is largely used for interrupting threads en-masse, it is
     * advised that there is some catch for the thrown {@link InterruptedException}
     * caused by {@link #interruptAll()}.
     * 
     * @param t    - the thread to add.
     * @param link - the object to link to the added thread.
     * @return this {@link ThreadMonitor} instance.
     */
    public ThreadMonitor addLinked(Thread t, Object link) {
        if (t != null) threads.put(t, Objects.requireNonNullElse(link, this.noLinkObj));
        return this;
    }
    
    /**
     * Purges all <code>null</code>, dead, or interrupted threads from the monitor,
     * freeing them for garbage collection.
     * <p>
     * This method does not perform any interruptions.
     * 
     * @see {@link #isRemovable(Thread)} to verify conditions that deem a thread
     *      removable.
     */
    public void purge() {
        // Only purge threads that are deemed "removable."
        threads.keySet().removeIf(this::isRemovable);
    }

    /**
     * Unlike {@link #purge()}, this method forcefully clears all threads, making
     * them no longer monitored, regardless of their state.
     * <p>
     * This method does not perform any interruptions.
     */
    public void clear() {
        threads.clear();
    }

    /**
     * Checks if the passed thread is considered valid and alive. The thread must be
     * non-<code>null</code>, alive, and not interrupted.
     * 
     * @param t - the {@link Thread} to check.
     * @return <code>true</code> if the thread is alive and not interrupted;
     *         <code>false</code> otherwise.
     */
    private boolean isAlive(Thread t) {
        return (t != null && t.isAlive() && !t.isInterrupted());
    }
    
    /**
     * Checks if the passed thread is considered removable from the monitor. The
     * thread must be <code>null</code>, not alive, or interrupted to qualify.
     * 
     * @param t - the {@link Thread} to check.
     * @return <code>true</code> if the thread is not alive or interrupted;
     *         <code>false</code> otherwise.
     */
    private boolean isRemovable(Thread t) {
        return !isAlive(t);
    }
    
    /**
     * Interrupts all threads linked to the passed object. Any interrupted threads
     * are automatically removed during this process and will no longer be
     * monitored.
     * <p>
     * If a value of <code>null</code> if passed, then all alive threads will be
     * interrupted.
     * <p>
     * If {@link Thread#interrupt()} fails for some reason, due to a thrown
     * {@link SecurityException} for example, the thread will remain monitored.
     * 
     * @param link - the object linked to the threads to interrupt, or
     *             <code>null</code> to interrupt all threads.
     */
    private void interrupt(Object link) {
        threads.entrySet().removeIf(entry -> {
            final Thread thread = entry.getKey();
            // Okay to interrupt if no link is specified, or if a matching link is.
            final boolean linkCheck = link == null || (link != null && entry.getValue() == link);
            try {
                // Only interrupt alive threads. If link is specified, it must also match.
                if (linkCheck && isAlive(thread)) {
                    thread.interrupt();
                    return true;
                }
            } catch (SecurityException ignore) {}   // Current thread did not have permission to interrupt the thread.
            
            // While only alive threads were interrupted earlier, if the thread is removable, it should be removed regardless.
            return isRemovable(thread);
        });
    }
    
    /**
     * Interrupts all threads added to this monitor. Any interrupted threads are
     * automatically removed during this process and will no longer be monitored.
     * <p>
     * If {@link Thread#interrupt()} fails for some reason, due to a thrown
     * {@link SecurityException} for example, the thread will remain monitored.
     */
    public void interruptAll() {
        interrupt(null);
    }
    
    /**
     * Interrupts all threads added to this monitor that are linked to the passed
     * object. Any interrupted threads are automatically removed during this process
     * and will no longer be monitored.
     * <p>
     * If {@link Thread#interrupt()} fails for some reason, due to a thrown
     * {@link SecurityException} for example, the thread will remain monitored.
     * 
     * @param link - the object linked to the threads that should be interrupted.
     */
    public void interruptLinked(Object link) {
        interrupt(link);
    }
    
    /**
     * Throws an {@link InterruptedException} if the interrupted flag on the current
     * thread is raised. This can be checked by calling the instance method
     * {@link Thread#isInterrupted()}.
     * <p>
     * This method is useful for acting on interruptions that have yet to be
     * handled, since interrupting a thread does not guarantee that an exception
     * will be thrown. If no part of the code checks the flag and throws an
     * exception, it could go unnoticed. That's where this method comes in.
     * 
     * @throws InterruptedException if the current thread is interrupted.
     */
    private void throwIfInterrupted() throws InterruptedException {
        // Any thrown InterruptedExceptions propagate up. If the interrupted flag is set, a new exception must be thrown.
        if (Thread.currentThread().isInterrupted()) throw new InterruptedException("Monitored thread was interrupted.");
    }
    
    /**
     * Executes the passed function, first adding the current {@link Thread} to the
     * monitor.
     * <p>
     * The thread is added with a link to the passed object, if not
     * <code>null</code>. Any threads linked to that object can be interrupted via
     * {@link #interruptLinked(Object)}, as opposed to interrupting all monitored
     * threads with {@link #interruptAll()}.
     * <p>
     * If interrupted, this method will throw an {@link InterruptedException}. Other
     * {@link Exception} types may be thrown if done in the passed function.
     * <p>
     * This method simplifies the process of calling a method in a monitored
     * context.
     * 
     * @param <E> - the additional {@link Exception} type(s) thrown by the passed
     *            function.
     * @param run - a parameterless function, that returns no result, to execute
     *            while being monitored for interruptions.
     * @throws InterruptedException if the current thread was interrupted while
     *                              executing the passed function.
     * @throws E                    if the passed function throws a
     *                              {@link Exception}.
     */
    public <E extends Exception> void run(MonitoredRun<E> run) throws InterruptedException, E {
        runLinked(null, run);
    }
    
    /**
     * Executes the passed function, first adding the current {@link Thread} to the
     * monitor.
     * <p>
     * The thread is added with a link to the passed object, if not
     * <code>null</code>. Any threads linked to that object can be interrupted via
     * {@link #interruptLinked(Object)}, as opposed to interrupting all monitored
     * threads with {@link #interruptAll()}.
     * <p>
     * If interrupted, this method will throw an {@link InterruptedException}. Other
     * {@link Exception} types may be thrown if done in the passed function.
     * <p>
     * This method simplifies the process of calling a method in a monitored
     * context.
     * 
     * @param <E>  - the additional {@link Exception} type(s) thrown by the passed
     *             function.
     * @param link - the object to link to the monitored thread.
     * @param run  - a parameterless function, that returns no result, to execute
     *             while being monitored for interruptions.
     * @throws InterruptedException if the current thread was interrupted while
     *                              executing the passed function.
     * @throws E                    if the passed function throws a
     *                              {@link Exception}.
     */
    public <E extends Exception> void runLinked(Object link, MonitoredRun<E> run) throws InterruptedException, E {
        // Add this thread to the monitor, linked to the passed object.
        addLinked(Thread.currentThread(), link);
        
        // Run the monitored runnable, then ensure InterruptedException is thrown if it was interrupted.
        run.monitor();
        throwIfInterrupted();
    }
    
    /**
     * Executes the passed function with the passed input, first adding the current
     * {@link Thread} to the monitor.
     * <p>
     * If interrupted, this method will throw an {@link InterruptedException}. Other
     * {@link Exception} types may be thrown if done in the passed function.
     * <p>
     * This method simplifies the process of calling a method in a monitored
     * context.
     * 
     * @param <T>      - the input data type.
     * @param <R>      - the return data type.
     * @param <E>      - the additional {@link Exception} type(s) thrown by the
     *                 passed function.
     * @param function - a function execute while being monitored for interruptions.
     * @param input    - the input data for the function.
     * @return the data that would typically be returned by the passed function.
     * @throws InterruptedException if the current thread was interrupted while
     *                              executing the passed function.
     * @throws E                    if the passed function throws a
     *                              {@link Exception}.
     */
    public <T, R, E extends Exception> R supply(MonitoredFunction<T, R, E> function, T input) throws InterruptedException, E {
        return supplyLinked(null, function, input);
    }
    
    /**
     * Executes the passed function with the passed input, first adding the current
     * {@link Thread} to the monitor.
     * <p>
     * The thread is added with a link to the passed object, if not
     * <code>null</code>. Any threads linked to that object can be interrupted via
     * {@link #interruptLinked(Object)}, as opposed to interrupting all monitored
     * threads with {@link #interruptAll()}.
     * <p>
     * If interrupted, this method will throw an {@link InterruptedException}. Other
     * {@link Exception} types may be thrown if done in the passed function.
     * <p>
     * This method simplifies the process of calling a method in a monitored
     * context.
     * 
     * @param <T>      - the input data type.
     * @param <R>      - the return data type.
     * @param <E>      - the additional {@link Exception} type(s) thrown by the
     *                 passed function.
     * @param link     - the object to link to the monitored thread.
     * @param function - a function execute while being monitored for interruptions.
     * @param input    - the input data for the function.
     * @return the data that would typically be returned by the passed function.
     * @throws InterruptedException if the current thread was interrupted while
     *                              executing the passed function.
     * @throws E                    if the passed function throws a
     *                              {@link Exception}.
     */
    public <T, R, E extends Exception> R supplyLinked(Object link, MonitoredFunction<T, R, E> function, T input) throws InterruptedException, E {
        // Add this thread to the monitor, linked to the passed object.
        addLinked(Thread.currentThread(), link);
        
        // Supply a monitored function, then ensure InterruptedException is thrown if it was interrupted.
        final R r = function.monitor(input);
        throwIfInterrupted();
        return r;
    }
    
    /**
     * Executes the passed function with the passed input asynchronously on a
     * different {@link Thread}. That asynchronous thread is monitored instead of
     * the calling thread. However, this thread still blocks until a result is
     * received or an exception is thrown.
     * <p>
     * If interrupted, this method will throw an {@link InterruptedException}. Other
     * {@link Exception} types may be thrown if done in the passed function.
     * <p>
     * This method simplifies the process of calling a method in a monitored
     * context.
     * 
     * @param <T>      - the input data type.
     * @param <R>      - the return data type.
     * @param <E>      - the additional {@link Exception} type(s) thrown by the
     *                 passed function.
     * @param function - a function execute while being monitored for interruptions.
     * @param input    - the input data for the function.
     * @return the data that would typically be returned by the passed function.
     * @throws InterruptedException if the current thread was interrupted while
     *                              executing the passed function.
     * @throws E                    if the passed function throws a
     *                              {@link Exception}.
     */
    public <T, R, E extends Exception> R supplyAsync(MonitoredFunction<T, R, E> function, T input) throws InterruptedException, E {
        return supplyAsyncLinked(null, function, input);
    }

    /**
     * Executes the passed function with the passed input asynchronously on a
     * different {@link Thread}. That asynchronous thread is monitored instead of
     * the calling thread. However, this thread still blocks until a result is
     * received or an exception is thrown.
     * <p>
     * The thread is added with a link to the passed object, if not
     * <code>null</code>. Any threads linked to that object can be interrupted via
     * {@link #interruptLinked(Object)}, as opposed to interrupting all monitored
     * threads with {@link #interruptAll()}.
     * <p>
     * If interrupted, this method will throw an {@link InterruptedException}. Other
     * {@link Exception} types may be thrown if done in the passed function.
     * <p>
     * This method simplifies the process of calling a method in a monitored
     * context.
     * 
     * @param <T>      - the input data type.
     * @param <R>      - the return data type.
     * @param <E>      - the additional {@link Exception} type(s) thrown by the
     *                 passed function.
     * @param link     - the object to link to the monitored thread.
     * @param function - a function execute while being monitored for interruptions.
     * @param input    - the input data for the function.
     * @return the data that would typically be returned by the passed function.
     * @throws InterruptedException if the current thread was interrupted while
     *                              executing the passed function.
     * @throws E                    if the passed function throws a
     *                              {@link Exception}.
     */
    public <T, R, E extends Exception> R supplyAsyncLinked(Object link, MonitoredFunction<T, R, E> function, T input) throws InterruptedException, E {
        // Supply a monitored function, linked to the passed object. Throw an InterruptedException if one was thrown during function execution.
        final ArrayList<R> res = CFExec.runAndGetVirtual(() -> supplyLinked(link, function, input))
                .throwIfFrom(new InterruptedException("Monitored, asynchronous supplier execution was interrupted."))
                .results();
        
        if (res.isEmpty()) return null;
        else return res.getFirst();
    }
}
