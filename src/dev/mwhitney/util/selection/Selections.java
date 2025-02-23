package dev.mwhitney.util.selection;

/**
 * An interface for selections used by a {@link Selector}. This interface can be
 * implemented as a functional interface to quickly and easily define a set of
 * selections.
 * 
 * @param <T> - the type of selections.
 * @author mwhitney57
 * @since 0.9.4
 */
@FunctionalInterface
public interface Selections<T> {
    /**
     * Gets all of the available selections.
     * 
     * @return an array with the selections.
     */
    public T[] getSelections();
}
