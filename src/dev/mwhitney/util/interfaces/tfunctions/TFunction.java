package dev.mwhitney.util.interfaces.tfunctions;

/**
 * A generic function that may throw an {@link Exception}.
 * <p>
 * This is the base interface, with other kinds of implementations that return a
 * value, or take numerous generic parameters.
 * 
 * @param <E> - the {@link Exception} that the function may throw.
 * @author mwhitney57
 * @since 0.9.5
 */
@FunctionalInterface
public interface TFunction<E extends Exception> {
    /**
     * A generic function that may throw an {@link Exception}.
     * 
     * @throws E the {@link Exception} that may be thrown.
     */
    public void apply() throws E;
}
