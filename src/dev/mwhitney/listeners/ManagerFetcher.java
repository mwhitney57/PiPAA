package dev.mwhitney.listeners;

/**
 * Used to fetch the object instance managing the implementing class. Most
 * notably used to fetch the {@link PiPWindowManager} from within a
 * {@link PiPWindow}. This interface allows the manager type to be defined via
 * Java Generics in case other classes have a similar management structure and
 * would benefit from using this interface.
 * 
 * @param <T> - the manager class type.
 * @author mwhitney57
 * @since 0.9.5
 */
@FunctionalInterface
public interface ManagerFetcher<T> {
    /**
     * Gets (fetches) the manager of this class.
     * 
     * @return the manager of this class.
     */
    public T getManager();

    /**
     * Checks if this class has a non-<code>null</code> manager.
     * 
     * @return <code>true</code> if this class has a valid manager;
     *         <code>false</code> otherwise.
     */
    public default boolean hasManager() {
        return (getManager() != null);
    }
}
