package dev.mwhitney.util.interfaces.tfunctions;

/**
 * A generic function that takes two arguments, returns a value, and may throw an
 * {@link Exception}.
 * 
 * @param <P>  - the first function parameter.
 * @param <P2> - the second function parameter.
 * @param <R>  - the return value.
 * @param <E>  - the {@link Exception} that the function may throw.
 * @author mwhitney57
 * @since 0.9.5
 */
@FunctionalInterface
public interface TRFunctionII<P, P2, R, E extends Exception> {
    /**
     * A generic function that takes arguments, returns a value, and may throw an
     * {@link Exception}.
     * 
     * @param arg1 - the first argument.
     * @param arg2 - the second argument.
     * @return the function return value.
     * @throws E the {@link Exception} that may be thrown.
     */
    public R apply(P arg1, P2 arg2) throws E;
}
