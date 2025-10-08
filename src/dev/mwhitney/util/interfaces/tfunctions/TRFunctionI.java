package dev.mwhitney.util.interfaces.tfunctions;

/**
 * A generic function that takes an argument, returns a value, and may throw an
 * {@link Exception}.
 * 
 * @param <P>  - the first function parameter.
 * @param <R>  - the return value.
 * @param <E>  - the {@link Exception} that the function may throw.
 * @author mwhitney57
 * @since 0.9.5
 */
@FunctionalInterface
public interface TRFunctionI<P, R, E extends Exception> {
    /**
     * A generic function that takes an argument, returns a value, and may throw an
     * {@link Exception}.
     * 
     * @param arg - the argument.
     * @return the function return value.
     * @throws E the {@link Exception} that may be thrown.
     */
    public R apply(P arg) throws E;
}
