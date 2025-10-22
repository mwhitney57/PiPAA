package dev.mwhitney.listeners;

/**
 * An interface similarly structured to {@link Supplier} which simply contains a
 * get method. However, unlike {@link Supplier}, this is intended to be used in
 * the contexts which may throw exceptions.
 * 
 * @param <T> - the type to retrieve from the supplier via {@link #get()}.
 * @author mwhitney57
 */
@FunctionalInterface
public interface PiPSupplier<T> {
    /**
     * A method to supply code which may possibly throw an {@link Exception}.
     * 
     * @throws Exception if the code threw an exception.
     */
    public T get() throws Exception;
}
