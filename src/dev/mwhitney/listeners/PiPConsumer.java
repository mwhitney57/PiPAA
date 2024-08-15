package dev.mwhitney.listeners;

/**
 * Similar to {@link Consumer}, represents an operation that accepts a single
 * input argument but returns nothing. It is similar in function to
 * {@link Runnable}, but arguments can be provided.
 * 
 * @param <I> - the first generic type, typically the index.
 * @param <T> - the second generic type, typically the primary object.
 * 
 * @author mwhitney57
 */
@FunctionalInterface
public interface PiPConsumer<I, T> {
    /**
     * Performs the accept operation on the given argument.
     *
     * @param i - the first type argument, typically the index.
     * @param t - the second type argument, typically the primary object.
     */
    public void accept(I i, T t);
}
