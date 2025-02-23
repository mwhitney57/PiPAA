package dev.mwhitney.util.selection;

import org.apache.commons.lang3.ArrayUtils;

/**
 * A basic selector class which simplifies the process of selecting from a list of options.
 * <p>
 * Any set of objects can be used as selections. To best utilize enums, check {@link Selections} and consider extending it.
 * Then, you can use the {@link #from(Selections)} method to create a new selector instance.
 * 
 * @author mwhitney57
 * @param <T> - the type of every selection available to this selector.
 * @since 0.9.4
 */
public class Selector <T> {
    /** The internal array of selections. */
    private final T[] selections;
    /** The index corresponding to the current selection within the {@link #selections}. */
    private int selectionIndex = -1;
    
    /**
     * Creates a Selector and selects the first passed element by default. The
     * entire purpose of this method is defeated by <b>only</b> passing
     * <code>null</code> objects. However, you can have <code>null</code> values
     * within the list of selections.
     * 
     * @param selections - the selections which this selector can pick from.
     */
    @SafeVarargs
    public Selector(T... selections) {
        this.selections = selections;
        if (hasSelections()) this.selectionIndex = 0;
    }
    
    /**
     * Creates and returns a new Selector with the passed {@link Selections}.
     * 
     * @param <S>        - the type of the {@link Selections}.
     * @param selections - the {@link Selections} object.
     * @return the new Selector instance.
     */
    public static <S extends Selections<S>> Selector<S> from(S selections) {
        return new Selector<S>(selections.getSelections()).select(selections);
    }
    
    /**
     * Checks if the passed int index is in the bounds of the internal selections
     * array. This method also requires that the selector {@link #hasSelections()},
     * and will return <code>false</code> if it does not have any.
     * 
     * @param index - the int index to check the bounds for.
     * @return <code>true</code> if the index is in bounds; <code>false</code>
     *         otherwise.
     */
    private boolean indexInBounds(int index) {
        return (hasSelections() && index >= 0 && index < this.selections.length);
    }
    
    /**
     * Selects the passed element, so long as it matches an available selection.
     * <p>
     * The selection will not change if the passed element does not match a
     * selection.
     * 
     * @param selection - the available element to select.
     * @return this Selector instance.
     */
    public Selector<T> select(T selection) {
        final int index = ArrayUtils.indexOf(this.selections, selection);
        if (index != ArrayUtils.INDEX_NOT_FOUND)
            this.selectionIndex = index;
        return this;
    }

    /**
     * Selects the element at the passed index, so long as the index is in bounds.
     * Calling {@link #select(Object)} will typically be preferred unless you know
     * the selections bounds.
     * <p>
     * The selection will not change if the index is out of bounds.
     * 
     * @param index - an int corresponding to a selection option.
     * @return this Selector instance.
     */
    public Selector<T> select(int index) {
        if (indexInBounds(index)) this.selectionIndex = index;
        return this;
    }
    
    /**
     * Selects and returns the passed element, so long as it matches an available
     * selection.
     * <p>
     * The selection will not change if the passed element does not match a
     * selection.
     * 
     * @param selection - the available element to select.
     * @return the new selection.
     */
    public T selectAndGet(T selection) {
        return select(selection).selection();
    }
    
    /**
     * Selects and returns the element at the passed index, so long as the index is
     * in bounds. Calling {@link #selectAndGet(Object)} will typically be preferred
     * unless you know the selections bounds.
     * <p>
     * The selection will not change if the index is out of bounds.
     * 
     * @param index - an int corresponding to a selection option.
     * @return the new selection.
     */
    public T selectAndGet(int index) {
        return select(index).selection();
    }
    
    /**
     * Checks if the current selection matches the passed element.
     * 
     * @param selection - the available element to check.
     * @return <code>true</code> if the passed element matches the current
     *         selection; <code>false</code> otherwise.
     */
    public boolean selected(T selection) {
        if (hasSelections()) {
            final int index = ArrayUtils.indexOf(this.selections, selection);
            return (index != ArrayUtils.INDEX_NOT_FOUND && index == this.selectionIndex);
        }
        return false;
    }

    /**
     * Checks if the current selection is at the passed int index. The passed index
     * must match the index of the current selection within the selector for this
     * method to return <code>true</code>.
     * 
     * @param index - an int corresponding to a selection option.
     * @return <code>true</code> if the passed index matches the internal selection
     *         index; <code>false</code> otherwise.
     */
    public boolean selected(int index) {
        return (indexInBounds(index) ? this.selectionIndex == index : false);
    }

    /**
     * Checks if this selector has the passed value available within its selections.
     * The passed value can be <code>null</code>, in which case <code>true</code>
     * could be returned if any of this selector's selections are <code>null</code>.
     * 
     * @param selection - the selection to check for.
     * @return <code>true</code> if the passed value exists within the selections;
     *         <code>false</code> otherwise.
     */
    public boolean hasSelection(T selection) {
        return ArrayUtils.contains(this.selections, selection);
    }
    
    /**
     * Gets the current selection, so long as one exists. Selections can be
     * <code>null</code>. This method will also return <code>null</code> if there
     * are no selections within the selector.
     * 
     * @return the current selection, which may be <code>null</code>, or
     *         <code>null</code> if there are no selections.
     */
    public T selection() {
        if (hasSelections()) return this.selections[selectionIndex];
        else return null;
    }

    /**
     * Checks if there are any available selections. Therefore, if the internal
     * selections array is <code>null</code>, or if there is not even one selection
     * available, this method will return <code>false</code>.
     * 
     * @return <code>true</code> if there is at least one selection available;
     *         <code>false</code> otherwise.
     */
    public boolean hasSelections() {
        return (this.selections != null && this.selections.length > 0);
    }
    
    /**
     * Gets all available selections an an array.
     * 
     * @return every available selection in an array.
     */
    public T[] selections() {
        return this.selections;
    }
}
