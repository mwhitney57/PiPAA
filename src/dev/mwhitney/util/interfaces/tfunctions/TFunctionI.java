package dev.mwhitney.util.interfaces.tfunctions;

/**
 * A generic function that takes an argument and may throw an {@link Exception}.
 * 
 * @param <P> - the first function parameter.
 * @param <E> - the {@link Exception} that the function may throw.
 * @author mwhitney57
 * @since 0.9.5
 */
@FunctionalInterface
public interface TFunctionI<P, E extends Exception> {
    /**
     * A generic function that takes an argument, and may throw an
     * {@link Exception}.
     * 
     * @param arg - the argument.
     * @throws E the {@link Exception} that may be thrown.
     */
    public void apply(P arg) throws E;
}
