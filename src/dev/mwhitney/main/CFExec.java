package dev.mwhitney.main;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import dev.mwhitney.listeners.PiPConsumer;
import dev.mwhitney.listeners.PiPRunnable;
import dev.mwhitney.listeners.PiPSupplier;
import dev.mwhitney.util.annotations.NeedsTesting;

/**
 * A helper class for executing {@link CompletableFuture} code in special, convenient ways.
 * 
 * @author mwhitney57
 */
public class CFExec {
    /**
     * The combined results from a single {@link CFExec} run call.
     * 
     * @param <T> - the data type of each {@link CFExecResult}'s raw result.
     * @author mwhitney57
     */
    public static class CFExecResults<T> {
        /** The internal array with all of the results from a single {@link CFExec} run call. */
        private CFExecResult<T>[] results;
        
        /**
         * Creates a new CFExecResults instance with the passed {@link CFExecResult} array.
         * 
         * @param res - a {@link CFExecResult} array with raw result-type <code>T</code>.
         */
        private CFExecResults(final CFExecResult<T>[] res) {
            this.results = Objects.requireNonNullElse(res, emptyArray());
        }
        
        /**
         * Creates and returns an empty {@link CFExecResult} array.
         * 
         * @param <T> - the data type of each {@link CFExecResult}'s raw result. The
         *            type is irrelevant here, other than to satisfy return requirements
         *            of {@link CFExec} methods.
         * @return the empty {@link CFExecResult} array.
         * @since 0.9.5
         */
        @SuppressWarnings("unchecked")
        private static <T> CFExecResult<T>[] emptyArray() {
            return (CFExecResult<T>[]) Array.newInstance(CFExecResult.class, 0);
        }
        
        /**
         * Creates a {@link CFExecResults} instance with no results via
         * {@link #emptyArray()}.
         * <p>
         * This method is useful for providing
         * 
         * @param <T> - the data type of each {@link CFExecResult}'s raw result. The
         *            type is irrelevant here, other than to satisfy return requirements
         *            of {@link CFExec} methods.
         * @return the {@link CFExecResults} instance with no results.
         * @since 0.9.5
         */
        private static <T> CFExecResults<T> empty() {
            return new CFExecResults<>(emptyArray());
        }
        
        /**
         * Checks if the passed exception class is present anywhere in the causal chain
         * of the passed exception. In other words, if the exception, or any of its
         * causes, is an instance of the passed class, this method will return
         * <code>true</code>.
         * <p>
         * If either of the passed parameters is <code>null</code>, this method returns
         * <code>false</code>.
         * 
         * @param <E>     - the type of exception being compared against the passed
         *                exception class.
         * @param exClass - the exception {@link Class} to check for.
         * @param ex      - the exception to check.
         * @return <code>true</code> if the passed exception, or any of its causes,
         *         matches the passed exception class; <code>false</code> otherwise.
         * @since 0.9.5
         */
        private <E extends Exception> boolean isExceptionInChain(Class<? extends Exception> exClass, E ex) {
            // Return null if parameters are invalid.
            if (ex == null || exClass == null) return false;

            // Check if passed exception is an instance of the class.
            if (exClass.isInstance(ex)) return true;

            // Check if any cause in the chain matches the class.
            Throwable cause = ex.getCause();
            while (cause != null) {
                if (exClass.isInstance(cause)) return true;
                cause = cause.getCause();
            }
            return false;
        }
        
        /**
         * Throws the passed Exception if any {@link CFExecResult} within the
         * {@link CFExecResults} threw an exception of the exact same class (or a
         * subclass). Otherwise, this method does nothing.
         * <p>
         * Unlike {@link #throwIfFrom(Exception)}, this method checks the passed
         * exception’s class (including subclasses) without examining the causal chain.
         * It only considers the surface-level exception stored in each result. It does
         * not check if the passed exception caused another exception to be thrown
         * through {@link Exception#getCause()}.
         * 
         * @param <E> the type of Exception to check for and potentially throw.
         * @param ex  - the Exception instance to throw if an exception with a matching
         *            class is found within the results.
         * @return this CFExecResults instance.
         * @throws E if an exception of the same class as the parameter was found within
         *           the results.
         */
        public <E extends Exception> CFExecResults<T> throwIfIs(final E ex) throws E {
            Objects.requireNonNull(ex, "CFExec: Cannot potentially throw passed Exception \"null\" from CFExecResults.");
            
            for (int i = 0; i < results.length; i++) {
                if (results[i].except() == null) continue;
                if (ex.getClass().isInstance(results[i].except())) throw ex;
            }
            return this;
        }

        /**
         * Throws the passed Exception if any {@link CFExecResult} within the
         * {@link CFExecResults} threw an exception from the same class. Otherwise, this
         * method does nothing.
         * <p>
         * The causal chain of each result's exception is considered. If the specified
         * exception is the cause of another exception in the chain, it is a match, and
         * the specified exception is thrown.
         * 
         * @param <E> the type of Exception to check for and potentially throw.
         * @param ex  - the Exception instance to throw if an exception with a matching
         *            class is found within the results.
         * @return this CFExecResults instance.
         * @throws E if an exception of the same class as the parameter was found within
         *           the results.
         * @since 0.9.5
         */
        public <E extends Exception> CFExecResults<T> throwIfFrom(final E ex) throws E {
            Objects.requireNonNull(ex, "CFExec: Cannot potentially throw passed Exception \"null\" from CFExecResults.");
            
            for (int i = 0; i < results.length; i++) {
                if (results[i].except() == null) continue;
                if (isExceptionInChain(ex.getClass(), results[i].except())) throw ex;
            }
            return this;
        }
        
        /**
         * Throws the passed Exception if <b>any</b> {@link CFExecResult} within the
         * {@link CFExecResults} caught <b>any</b> type of exception. Otherwise, this
         * method does nothing.
         * 
         * @param <E> the type of Exception to potentially throw.
         * @param ex  - the Exception instance to throw if an exception was found within
         *            the results.
         * @return this CFExecResults instance.
         * @throws E if an exception was found within the results.
         */
        public <E extends Exception> CFExecResults<T> throwIfAny(final E ex) throws E {
            Objects.requireNonNull(ex, "CFExec: Cannot potentially throw passed Exception \"null\" from CFExecResults.");
            
            for (int i = 0; i < results.length; i++) {
                if (results[i].except() != null) throw ex;
            }
            return this;
        }
        
        /**
         * Throws the first of any Exception within the results which is an instance of,
         * or extends, the passed class(es). If there are no matches, this method will
         * effectively do nothing and simply return this CFExecResults instance. Unlike
         * {@link #throwIfFrom(Exception)} or {@link #throwIfAny(Exception)}, this method
         * will throw the specific matching exception present in the results, not one
         * that was custom-provided when called.
         * <p>
         * <b>Note:</b> Passing more than one argument to this method will result in the
         * method <code>throws</code> being the {@link Exception} superclass instead of
         * a specific extension subclass. If you wish to only have your code throw
         * specific exceptions, you can stack {@link #throwAny(Class...)} calls, one
         * after the other, to achieve this goal.
         * 
         * @param <E>        any extension upon Exception to check for and potentially
         *                   throw.
         * @param exceptions - one or more Class objects of type <code>E</code> or an
         *                   extension of <code>E</code>.
         * @return this CFExecResults instance.
         * @throws E if a matching exception was found within the results to what was
         *           passed.
         */
        @SafeVarargs
        @NeedsTesting(info = "Perform more tests with passing multiple exception classes.")
        public final <E extends Exception> CFExecResults<T> throwAny(final Class<? extends E>... exceptions) throws E {
            Objects.requireNonNull(exceptions, "CFExec: Cannot potentially throw Exception type(s) \"null\" from CFExecResults.");
            
            for (final Class<? extends E> ex : exceptions) {
                if (ex == null) continue;
                for (int i = 0; i < results.length; i++) {
                    if (results[i].except() == null) continue;
                    if (ex.isAssignableFrom(results[i].except().getClass()))
                        throw ex.cast(results[i].except());
                }
            }
            return this;
        }
        
        /**
         * Runs the exception check on each {@link CFExecResult}, running the passed
         * {@link PiPConsumer} code if an exception was thrown during execution.
         * <p>
         * The method requires the two consumer arguments: Integer and Exception.
         * The Integer argument is the index of the previously-passed execution which is being referenced.
         * The Exception argument contains the specific exception that was thrown during execution.
         * <p>
         * <b>None of the {@link PiPConsumer}'s code executes if there was no exception thrown.</b>
         * 
         * @param consumer - the {@link PiPConsumer} which provides an Integer index for which execution threw the Exception provided in the second argument.
         * @return this CFExecResults instance.
         */
        public CFExecResults<T> excepts(final PiPConsumer<Integer, Exception> consumer) {
            for (int i = 0; i < results.length; i++) {
                if (results[i].except() != null) consumer.accept(i, results[i].except());
            }
            return this;
        }
        
        /**
         * Returns an {@link ArrayList} of type <code>T</code> with the {@link CFExecResult} from each execution.
         * This method is shorthand for {@link #results(boolean)} and passing <code>false</code>.
         * 
         * @return an {@link ArrayList} of type <code>T</code> with the results.
         * @see {@link #results(boolean)} to specify whether or not results should be trimmed.
         */
        public ArrayList<T> results() {
            return results(false);
        }

        /**
         * Returns an {@link ArrayList} of type <code>T</code> with the
         * {@link CFExecResult} from each execution. The {@link CFExecResult} list can
         * be trimmed of any <code>null</code> result values by passing
         * <code>true</code>.
         * <p>
         * Keep in mind that, by trimming the results, the indices may no longer line up
         * with those of the executions. If the indices must be preserved, it could be
         * better to call {@link #results()} and perform <code>null</code> checks
         * afterwards. If the order is all that matters, then this method is safe, since
         * it preserves the order.
         * 
         * @param trim - a boolean for whether or not to trim the results.
         * @return an {@link ArrayList} of type <code>T</code> with the possibly-trimmed
         *         results.
         * @see {@link #results()} to use the default trim value of <code>false</code>.
         */
        public ArrayList<T> results(boolean trim) {
            final ArrayList<T> array = new ArrayList<>(results.length);
            for (int i = 0; i < results.length; i++) {
                if (!trim || results[i].result() != null)
                    array.add(results[i].result());
            }
            return array;
        }
    }
    /**
     * A result from a single execution within a {@link CFExec} run call.
     * 
     * @param <T> - the raw result's data type.
     * @author mwhitney57
     */
    public static class CFExecResult<T> {
        /** The {@link Exception} thrown, if any, during execution. Can be <code>null</code>. */
        private Exception exception;
        /** The raw result from the execution. Can be <code>null</code> if there is no returned result. */
        private T result;
        
        /**
         * Creates a new CFExecResult instance with any thrown {@link Exception} as well
         * as the raw execution result.
         * 
         * @param ex  - the {@link Exception} thrown during execution, if any.
         * @param res - the raw result, of type <code>T</code>.
         */
        private CFExecResult(Exception ex, T res) {
            this.exception = ex;
            this.result = res;
        }
        
        /**
         * Creates a new CFExecResult instance with any thrown {@link Exception} and a
         * default <code>null</code> execution result.
         * 
         * @param ex - the {@link Exception} thrown during execution, if any.
         */
        private CFExecResult(Exception ex) {
            this(ex, null);
        }
        
        /**
         * Gets the {@link Exception} thrown during execution, if there was any at all.
         * Since an exception may not have been thrown, this method can return
         * <code>null</code>.
         * 
         * @return the {@link Exception} thrown during execution, or <code>null</code>
         *         if none was thrown.
         */
        public Exception except() {
            return this.exception;
        }
        
        /**
         * Gets the result of the execution. If the execution returned no result, this
         * method can return <code>null</code>.
         * 
         * @return the result, of type <code>T</code>, or <code>null</code> if there was
         *         no returned result.
         */
        public T result() {
            return this.result;
        }
    }

    /**
     * The executor for <b>Virtual Threads</b>.
     * <p>
     * Virtual Threads were finalized and shipped with Java 21 and provide a
     * <b>major</b> benefit over traditional platform threads. They are a
     * lightweight addition on top of platform threads, with the ability to back off
     * of the thread into memory while waiting for blocking tasks to complete.
     * <p>
     * If a virtual thread waits, such as with a web request or I/O operation, the
     * thread is "parked." This pulls it off of the platform thread that it is
     * running on, allowing other tasks to execute on the platform thread during the
     * waiting period.
     * <p>
     * When the virtual thread resumes, it is added back into the queue of tasks on
     * the platform thread it was originally on, or is stolen and placed on another
     * platform thread that is open (not blocked) to speed things up and get it
     * executing sooner.
     * <p>
     * Therefore, if an asynchronous task requires any such I/O, sleeping, or
     * waiting periods, it is advised to use a virtual thread. Avoid in other
     * scenarios that provide no benefit, such as with native code that pins the
     * virtual thread to its carrier thread, preventing it from yielding.
     * 
     * @since 0.9.5
     */
    public static final ExecutorService VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();
    
    /**
     * Runs every passed {@link PiPRunnable} synchronously and sequentially. The
     * method performs run calls on each Runnable, one by one. Additionally, this
     * method will catch and store any exception thrown by each runnable. Each
     * exception can be handled individually through the returned
     * {@link CFExecResults}.
     * <p>
     * <b>Note:</b> This method blocks until each and every one has executed or
     * thrown an exception.
     * <p>
     * If any passed {@link PiPRunnable} objects are <code>null</code>, they will
     * simply be ignored and the result pertaining to that runnable will be have
     * <code>null</code> values for both the result and any caught exception.
     * 
     * @param runs - one or more {@link PiPRunnable} objects to execute one after
     *             the other.
     * @return a {@link CFExecResults} instance with result type <code>Void</code>,
     *         since no result is provided within.
     * @see {@link #run(PiPRunnable...)} to run multiple batches of code
     *      nearly-simultaneously and asynchronously.
     * @see {@link #runAndGet(PiPSupplier...)} to get and store an object from the
     *      passed suppliers in the returned results.
     */
    @SuppressWarnings("unchecked")
    @NeedsTesting
    public static CFExecResults<Void> runSync(PiPRunnable... runs) {
        // Do nothing if array is null or no elements.
        if (runs == null || runs.length < 1) return CFExecResults.empty();
        
        // Initialize array, nest runs and store it in array.
        final Runnable[] nestedRuns = new Runnable[runs.length];
        for (int i = 0; i < runs.length; i++) {
            // Ignore any null objects.
            if (runs[i] == null) continue;
            
            // Setup Nested Runnable with CompletionException Error Throw
            final int r = i;
            nestedRuns[i] = () -> {
                try {
                    runs[r].run();
                } catch (Exception e) { throw new CompletionException(e); }
            };
        }
        
        // For each, run back to back before finishing function.
        final CFExecResult<Void>[] results = new CFExecResult[runs.length];
        for (int i = 0; i < nestedRuns.length; i++) {
            Exception exc = null;
            try {
                if (nestedRuns[i] != null) nestedRuns[i].run();
            } catch (CompletionException ce) {
                exc = (Exception) ce.getCause();
            }
            results[i] = new CFExecResult<Void>(exc);
        }
        
        // Return execution results.
        return new CFExecResults<Void>(results);
    }
    
    /**
     * Runs every passed {@link PiPRunnable} asynchronously, and nearly
     * simultaneously. The method performs async run calls on each Runnable. Then,
     * it waits for all of them to finish executing. Additionally, this method will
     * catch and store any exception thrown by each runnable. Each exception can be
     * handled individually through the returned {@link CFExecResults}.
     * <p>
     * <b>Note:</b> This method blocks until each and every one has executed or
     * thrown an exception.
     * <p>
     * If any passed {@link PiPRunnable} objects are <code>null</code>, they will
     * simply be ignored and the result pertaining to that runnable will be have
     * <code>null</code> values for both the result and any caught exception.
     * 
     * @param virtual - a boolean for whether or not the code should execute in
     *                virtual threads.
     * @param runs    - one or more {@link PiPRunnable} objects to execute
     *                concurrently alongside one another.
     * @return a {@link CFExecResults} instance with result type <code>Void</code>,
     *         since no result is provided within.
     * @see {@link #runAndGet(PiPSupplier...)} to get and store an object from the
     *      passed suppliers in the returned results.
     */
    @SuppressWarnings("unchecked")
    public static CFExecResults<Void> run(final boolean virtual, PiPRunnable... runs) {
        // Do nothing if array is null or no elements.
        if (runs == null || runs.length < 1) return CFExecResults.empty();
        
        // Initialize array, run each async then store it in array.
        final CompletableFuture<Void>[] cfs = new CompletableFuture[runs.length];
        for (int i = 0; i < runs.length; i++) {
            // Ignore any null objects.
            if (runs[i] == null) continue;
            
            // Setup Nested Runnable with CompletionException Error Throw
            final int r = i;
            final Runnable run = () -> {
                try {
                    runs[r].run();
                } catch (Exception e) { throw new CompletionException(e); }
            };
            
            // Run the Runnable Asynchronously and Store it in the Array.
            cfs[i] = (virtual ? CompletableFuture.runAsync(run, VIRTUAL_EXECUTOR) : CompletableFuture.runAsync(run));
        }
        
        // For each, join back to the main thread before finishing function.
        final CFExecResult<Void>[] gets = new CFExecResult[runs.length];
        for (int i = 0; i < cfs.length; i++) {
            Exception exc = null;
            try {
                if (cfs[i] != null) cfs[i].join();
            } catch (CompletionException e) {
                exc = (Exception) e.getCause();
            }
            gets[i] = new CFExecResult<Void>(exc);
        }
        
        // Return execution results.
        return new CFExecResults<Void>(gets);
    }
    
    /**
     * Runs every passed {@link PiPRunnable} asynchronously, and nearly simultaneously. The
     * method performs async run calls on each Runnable. Then, it waits for all of
     * them to finish executing. Additionally, this method will catch and store any
     * exception thrown by each runnable. Each exception can be handled individually
     * through the returned {@link CFExecResults}.
     * <p>
     * <b>Note:</b> This method blocks until each and every one has executed or
     * thrown an exception.
     * <p>
     * If any passed {@link PiPRunnable} objects are <code>null</code>, they will simply be ignored
     * and the result pertaining to that runnable will be have <code>null</code>
     * values for both the result and any caught exception.
     * 
     * @param runs - one or more {@link PiPRunnable} objects to execute concurrently alongside one
     *             another.
     * @return a {@link CFExecResults} instance with result type <code>Void</code>,
     *         since no result is provided within.
     * @see {@link #runAndGet(PiPSupplier...)} to get and store an object from the
     *      passed suppliers in the returned results.
     */
    public static CFExecResults<Void> run(PiPRunnable... runs) {
        return run(false, runs);
    }

    /**
     * Runs every passed {@link PiPRunnable} asynchronously, and nearly
     * simultaneously. The method performs async run calls on each Runnable. Then,
     * it waits for all of them to finish executing. Additionally, this method will
     * catch and store any exception thrown by each runnable. Each exception can be
     * handled individually through the returned {@link CFExecResults}.
     * <p>
     * All asynchronous code execution will happen on <b>virtual threads</b>. See
     * the documentation for the {@link #VIRTUAL_EXECUTOR} for more information.
     * <p>
     * <b>Note:</b> This method blocks until each and every one has executed or
     * thrown an exception.
     * <p>
     * If any passed {@link PiPRunnable} objects are <code>null</code>, they will
     * simply be ignored and the result pertaining to that runnable will be have
     * <code>null</code> values for both the result and any caught exception.
     * 
     * @param runs - one or more {@link PiPRunnable} objects to execute concurrently
     *             alongside one another.
     * @return a {@link CFExecResults} instance with result type <code>Void</code>,
     *         since no result is provided within.
     * @see {@link #runAndGet(PiPSupplier...)} to get and store an object from the
     *      passed suppliers in the returned results.
     * @since 0.9.5
     */
    public static CFExecResults<Void> runVirtual(PiPRunnable... runs) {
        return run(true, runs);
    }

    /**
     * Runs every passed {@link PiPRunnable} asynchronously and sequentially. The
     * method performs the sequential run calls asynchronously on another thread.
     * Therefore, this method will return almost immediately.
     * <p>
     * <b>Note:</b> This method suppresses any and every {@link Exception} thrown
     * during execution of a {@link PiPRunnable}.
     * <p>
     * If any passed {@link PiPRunnable} objects are <code>null</code>, they will
     * simply be ignored.
     * 
     * @param virtual - a boolean for whether or not the code should execute in
     *                virtual threads.
     * @param runs    - one or more PiPRunnables to execute sequentially.
     * @see {@link #run(PiPRunnable...)} to get simultaneous and synchronous
     *      execution, while also receiving a returned result.
     */
    @NeedsTesting
    public static void runSequential(final boolean virtual, PiPRunnable... runs) {
        // Do nothing if array is null or no elements.
        if (runs == null || runs.length < 1) return;
        
        // Setup Runnable
        final Runnable run = () -> {
            // For each, run back to back before finishing function.
            for (int i = 0; i < runs.length; i++) {
                // Ignore any null objects.
                if (runs[i] == null) continue;
                
                try {
                    runs[i].run();
                } catch (Exception e) { /* Suppress exception. */ }
            }
        };
        
        // Perform runs sequentially and asynchronously.
        if (virtual) CompletableFuture.runAsync(run, VIRTUAL_EXECUTOR);
        else         CompletableFuture.runAsync(run);
    }

    /**
     * Runs every passed {@link PiPRunnable} asynchronously and sequentially. The
     * method performs the sequential run calls asynchronously on another thread.
     * Therefore, this method will return almost immediately.
     * <p>
     * <b>Note:</b> This method suppresses any and every {@link Exception} thrown
     * during execution of a {@link PiPRunnable}.
     * <p>
     * If any passed {@link PiPRunnable} objects are <code>null</code>, they will
     * simply be ignored.
     * 
     * @param runs - one or more PiPRunnables to execute sequentially.
     * @see {@link #run(PiPRunnable...)} to get simultaneous and synchronous
     *      execution, while also receiving a returned result.
     */
    @NeedsTesting
    public static void runSequential(PiPRunnable... runs) {
        runSequential(false, runs);
    }

    /**
     * Runs every passed {@link PiPRunnable} asynchronously and sequentially. The
     * method performs the sequential run calls asynchronously on another thread.
     * Therefore, this method will return almost immediately.
     * <p>
     * All asynchronous code execution will happen on <b>virtual threads</b>. See
     * the documentation for the {@link #VIRTUAL_EXECUTOR} for more information.
     * <p>
     * <b>Note:</b> This method suppresses any and every {@link Exception} thrown
     * during execution of a {@link PiPRunnable}.
     * <p>
     * If any passed {@link PiPRunnable} objects are <code>null</code>, they will
     * simply be ignored.
     * 
     * @param runs - one or more PiPRunnables to execute sequentially.
     * @see {@link #run(PiPRunnable...)} to get simultaneous and synchronous
     *      execution, while also receiving a returned result.
     * @since 0.9.5
     */
    @NeedsTesting
    public static void runSequentialVirtual(PiPRunnable... runs) {
        runSequential(true, runs);
    }

    /**
     * Runs every passed {@link PiPSupplier} asynchronously, and nearly
     * simultaneously. The method performs async run calls on each Supplier. Then,
     * it waits for all of them to finish executing. Additionally, this method will
     * track any thrown exceptions within the passed supplier's and be accessible
     * via the {@link CFExecResults#excepts(PiPConsumer)} or
     * {@link CFExecResult#except()} methods.
     * <p>
     * <b>Note:</b> This method blocks until each and every one of the passed,
     * non-<code>null</code> suppliers has executed or thrown an exception.
     * Suppliers that throw exceptions may return no result. Furthermore, it is
     * possible for both {@link CFExecResult#result()} and
     * {@link CFExecResult#except()} to return <code>null</code>, as a supplier may
     * choose to return <code>null</code>.
     * <p>
     * If any passed {@link PiPSupplier} objects are <code>null</code>, they will
     * simply be ignored and their result will be <code>null</code> in the results.
     * 
     * @param <T>     - the raw data type of the return from each supplier.
     * @param virtual - a boolean for whether or not the code should execute in
     *                virtual threads.
     * @param sups    - one or more {@link PiPSupplier} objects to execute
     *                concurrently alongside one another.
     * @return a {@link CFExecResults} instance with result type <code>T</code>.
     * @see {@link #run(PiPRunnable...)} to run multiple batches of code
     *      nearly-simultaneously, similar to this method, but without a returned
     *      result for each.
     */
    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T> CFExecResults<T> runAndGet(final boolean virtual, final PiPSupplier<T>... sups) {
        // Do nothing if array is null or no elements.
        if (sups == null || sups.length < 1) return CFExecResults.empty();
        
        // Initialize array, run each async then store it in array.
        final CompletableFuture<T>[] cfs = new CompletableFuture[sups.length];
        for (int i = 0; i < sups.length; i++) {
            // Ignore any null objects.
            if (sups[i] == null) continue;
            
            // Setup Nested Supplier with CompletionException Error Throw
            final int r = i;
            final Supplier<T> sup = () -> {
                try {
                    return sups[r].get();
                } catch (Exception e) { throw new CompletionException(e); }
            };
            
            // Run the Supplier Asynchronously and Store it in the Array.
            cfs[i] = (virtual ? CompletableFuture.supplyAsync(sup, VIRTUAL_EXECUTOR) : CompletableFuture.supplyAsync(sup));
        }
        
        // For each, get result while joining back to the main thread before finishing function.
        final CFExecResult<T>[] gets = new CFExecResult[sups.length];
        for (int i = 0; i < cfs.length; i++) {
            T res = null;
            Exception exc = null;
            try {
                res = (cfs[i] != null ? cfs[i].get() : null);
            } catch (CompletionException | InterruptedException | ExecutionException e) {
                exc = (e instanceof CompletionException ? (Exception) e.getCause() : e);
            }
            gets[i] = new CFExecResult<T>(exc, res);
        }
        
        // Return execution results.
        return new CFExecResults<T>(gets);
    }

    /**
     * Runs every passed {@link PiPSupplier} asynchronously, and nearly
     * simultaneously. The method performs async run calls on each Supplier. Then,
     * it waits for all of them to finish executing. Additionally, this method will
     * track any thrown exceptions within the passed supplier's and be accessible
     * via the {@link CFExecResults#excepts(PiPConsumer)} or
     * {@link CFExecResult#except()} methods.
     * <p>
     * <b>Note:</b> This method blocks until each and every one of the passed,
     * non-<code>null</code> suppliers has executed or thrown an exception.
     * Suppliers that throw exceptions may return no result. Furthermore, it is
     * possible for both {@link CFExecResult#result()} and
     * {@link CFExecResult#except()} to return <code>null</code>, as a supplier may
     * choose to return <code>null</code>.
     * <p>
     * If any passed {@link PiPSupplier} objects are <code>null</code>, they will simply be ignored
     * and their result will be <code>null</code> in the results.
     * 
     * @param <T>  - the raw data type of the return from each supplier.
     * @param sups - one or more {@link PiPSupplier} objects to execute concurrently alongside one
     *             another.
     * @return a {@link CFExecResults} instance with result type <code>T</code>.
     * @see {@link #run(PiPRunnable...)} to run multiple batches of code
     *      nearly-simultaneously, similar to this method, but without a returned
     *      result for each.
     */
    @SafeVarargs
    public static <T> CFExecResults<T> runAndGet(final PiPSupplier<T>... sups) {
        return runAndGet(false, sups);
    }

    /**
     * Runs every passed {@link PiPSupplier} asynchronously, and nearly
     * simultaneously. The method performs async run calls on each Supplier. Then,
     * it waits for all of them to finish executing. Additionally, this method will
     * track any thrown exceptions within the passed supplier's and be accessible
     * via the {@link CFExecResults#excepts(PiPConsumer)} or
     * {@link CFExecResult#except()} methods.
     * <p>
     * All asynchronous code execution will happen on <b>virtual threads</b>. See
     * the documentation for the {@link #VIRTUAL_EXECUTOR} for more information.
     * <p>
     * <b>Note:</b> This method blocks until each and every one of the passed,
     * non-<code>null</code> suppliers has executed or thrown an exception.
     * Suppliers that throw exceptions may return no result. Furthermore, it is
     * possible for both {@link CFExecResult#result()} and
     * {@link CFExecResult#except()} to return <code>null</code>, as a supplier may
     * choose to return <code>null</code>.
     * <p>
     * If any passed {@link PiPSupplier} objects are <code>null</code>, they will
     * simply be ignored and their result will be <code>null</code> in the results.
     * 
     * @param <T>  - the raw data type of the return from each supplier.
     * @param sups - one or more {@link PiPSupplier} objects to execute concurrently
     *             alongside one another.
     * @return a {@link CFExecResults} instance with result type <code>T</code>.
     * @see {@link #run(PiPRunnable...)} to run multiple batches of code
     *      nearly-simultaneously, similar to this method, but without a returned
     *      result for each.
     * @since 0.9.5
     */
    @SafeVarargs
    public static <T> CFExecResults<T> runAndGetVirtual(final PiPSupplier<T>... sups) {
        return runAndGet(true, sups);
    }
}
