package dev.mwhitney.util.interfaces.tfunctions;

/**
 * A generic function that takes four arguments and may throw an
 * {@link Exception}.
 * 
 * @param <P>  - the first function parameter.
 * @param <P2> - the second function parameter.
 * @param <P3> - the third function parameter.
 * @param <P4> - the fourth function parameter.
 * @param <E>  - the {@link Exception} that the function may throw.
 * @author mwhitney57
 * @since 0.9.5
 */
@FunctionalInterface
public interface TFunctionIV<P, P2, P3, P4, E extends Exception> {
    /**
     * A generic function that takes arguments, and may throw an {@link Exception}.
     * 
     * @param arg1 - the first argument.
     * @param arg2 - the second argument.
     * @param arg3 - the third argument.
     * @param arg4 - the fourth argument.
     * @throws E the {@link Exception} that may be thrown.
     */
    public void apply(P arg1, P2 arg2, P3 arg3, P4 arg4) throws E;
}
