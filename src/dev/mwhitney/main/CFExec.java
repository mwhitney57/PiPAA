package dev.mwhitney.main;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import dev.mwhitney.listeners.PiPConsumer;
import dev.mwhitney.listeners.PiPRunnable;
import dev.mwhitney.listeners.PiPSupplier;

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
            this.results = res;
        }
        
        /**
         * Throws the passed Exception if any {@link CFExecResult} within the
         * {@link CFExecResults} threw an exception of the same class.
         * Otherwise, this method does nothing.
         * 
         * @param <E> the type of Exception to check for and potentially throw.
         * @param ex  - the Exception instance to throw if an exception with a matching
         *            class is found within the results.
         * @return this CFExecResults instance.
         * @throws E if an exception of the same class as the parameter was found within
         *           the results.
         */
        @NeedsTesting(info = "Should be working, but pay attention when using for first time.")
        public <E extends Exception> CFExecResults<T> throwIfOf(final E ex) throws E {
            Objects.requireNonNull(ex, "CFExec: Cannot potentially throw passed Exception \"null\" from CFExecResults.");
            
            for (int i = 0; i < results.length; i++) {
                if (results[i].except() == null) continue;
                if (ex.getClass().isAssignableFrom(results[i].except().getClass())) throw ex;
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
         * {@link #throwIfOf(Exception)} or {@link #throwIfAny(Exception)}, this method
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
         * Returns an {@link ArrayList} of type <code>T</code> with the {@link CFExecResult} from each execution.
         * The {@link CFExecResult} list can be trimmed of any <code>null</code> result values by passing <code>true</code>.
         * 
         * @param trim - a boolean for whether or not to trim the results.
         * @return an {@link ArrayList} of type <code>T</code> with the possibly-trimmed results.
         * @see {@link #results()} to use the default trim value of <code>false</code>.
         */
        public ArrayList<T> results(boolean trim) {
            final ArrayList<T> array = new ArrayList<>(results.length);
            for (int i = 0; i < results.length; i++) {
                if (!trim || results[i].result() != null)
                    array.add(i, results[i].result());
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
        if (runs == null || runs.length < 1) return null;
        
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
    @SuppressWarnings("unchecked")
    public static CFExecResults<Void> run(PiPRunnable... runs) {
        // Do nothing if array is null or no elements.
        if (runs == null || runs.length < 1) return null;
        
        // Initialize array, run each async then store it in array.
        final CompletableFuture<?>[] cfs = new CompletableFuture<?>[runs.length];
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
            cfs[i] = CompletableFuture.runAsync(run);
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
     * Runs every passed {@link PiPRunnable} asynchronously and sequentially. The
     * method performs run calls asynchronously on another thread. Therefore, this
     * method will return almost immediately.
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
        // Do nothing if array is null or no elements.
        if (runs == null || runs.length < 1) return;
        
        // Perform runs sequentially and asynchronously.
        CompletableFuture.runAsync(() -> {
            // For each, run back to back before finishing function.
            for (int i = 0; i < runs.length; i++) {
                // Ignore any null objects.
                if (runs[i] == null) continue;
                
                try {
                    runs[i].run();
                } catch (Exception e) { /* Suppress exception. */ }
            }
        });
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
    @SuppressWarnings("unchecked")
    public static <T extends Object> CFExecResults<T> runAndGet(final PiPSupplier<T>... sups) {
        // Do nothing if array is null or no elements.
        if (sups == null || sups.length < 1) return null;
        
        // Initialize array, run each async then store it in array.
        final CompletableFuture<?>[] cfs = new CompletableFuture<?>[sups.length];
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
            cfs[i] = CompletableFuture.supplyAsync(sup);
        }
        
        // For each, get result while joining back to the main thread before finishing function.
        final CFExecResult<T>[] gets = new CFExecResult[sups.length];
        for (int i = 0; i < cfs.length; i++) {
            T res = null;
            Exception exc = null;
            try {
                res = (cfs[i] != null ? ((T) cfs[i].get()) : null);
            } catch (CompletionException | InterruptedException | ExecutionException e) {
                exc = (e instanceof CompletionException ? (Exception) e.getCause() : e);
            }
            gets[i] = new CFExecResult<T>(exc, res);
        }
        
        // Return execution results.
        return new CFExecResults<T>(gets);
    }
}
