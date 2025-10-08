package dev.mwhitney.util.interfaces.tfunctions;

/**
 * A generic function that takes two arguments and may throw an
 * {@link Exception}.
 * 
 * @param <P>  - the first function parameter.
 * @param <P2> - the second function parameter.
 * @param <E>  - the {@link Exception} that the function may throw.
 * @author mwhitney57
 * @since 0.9.5
 */
@FunctionalInterface
public interface TFunctionII<P, P2, E extends Exception> {
    /**
     * A generic function that takes arguments, and may throw an {@link Exception}.
     * 
     * @param arg1 - the first argument.
     * @param arg2 - the second argument.
     * @throws E the {@link Exception} that may be thrown.
     */
    public void apply(P arg1, P2 arg2) throws E;
}
