package dev.mwhitney.listeners;

/**
 * An extension of {@link PiPSupplier} designed to be interruptible and throw an
 * {@link InterruptedException}.
 * 
 * @param <T> - the type to retrieve from the supplier via {@link #get()}.
 * @author mwhitney57
 * @since 0.9.5
 */
@FunctionalInterface
public interface InterruptibleSupplier<T> extends PiPSupplier<T> {
    /**
     * A method to supply code which may possibly throw a
     * {@link InterruptedException}.
     * 
     * @throws InterruptedException if the code was interrupted before supplying.
     */
    @Override
    public T get() throws InterruptedException;
}
