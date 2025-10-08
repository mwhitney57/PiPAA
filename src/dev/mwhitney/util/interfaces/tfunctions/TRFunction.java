package dev.mwhitney.util.interfaces.tfunctions;

/**
 * A generic function that returns a value and may throw an {@link Exception}.
 * 
 * @param <R> - the return value.
 * @param <E> - the {@link Exception} that the function may throw.
 * @author mwhitney57
 * @since 0.9.5
 */
@FunctionalInterface
public interface TRFunction<R, E extends Exception> {
    /**
     * A generic function that returns a value and may throw an {@link Exception}.
     * 
     * @return the function return value.
     * @throws E the {@link Exception} that may be thrown.
     */
    public R apply() throws E;
}
