package dev.mwhitney.util;

import java.util.Objects;

/**
 * A loop of objects, which is an array that can be iterated over, restarting at
 * the beginning when the end is reached.
 * 
 * @param <T> the type of object stored in the loop array.
 * @author mwhitney57
 */
public class Loop<T> {
    /** The array of type {@link T} iterated over within the loop. */
    private T[] array;
    /** The <code>int</code> index or position within the loop. */
    private int index;
    
    /**
     * Creates a new Loop with the passed array data, starting at the passed index.
     * The passed array data must be non-<code>null</code>.
     * <p>
     * <b>Important:</b> The passed <code>index</code> will be reduced or increased
     * to not exceed the bounds of the passed array when necessary. Calling the
     * constructor with the following example values...
     * 
     * <pre>
     *   array = { 'A', 'B', 'C' };
     *   index = 4;
     * </pre>
     * 
     * ...would result in an {@link #index()} of <code>2</code>.
     * 
     * @param array - the array data of type {@link T}.
     * @param index - the <code>int</code> index or position within the array to
     *              start at.
     */
    public Loop(T[] array, int index) {
        this.array = Objects.requireNonNull(array);
        this.index = Math.max(0, Math.min(index, array.length - 1));
    }
    
    /**
     * Creates a new Loop with the passed array data, starting at the default index
     * of <code>0</code>. The passed array data must be non-<code>null</code>.
     * 
     * @param array - the array data of type {@link T}.
     */
    public Loop(T[] array) {
        this(array, 0);
    }
    
    /**
     * Returns the current index or position.
     * 
     * @return an <code>int</code> with the current index.
     */
    public int index() {
        return this.index;
    }
    
    /**
     * Returns the element at the current index or position in the loop.
     * 
     * @return an Object of type {@link T} at the current index.
     */
    public T current() {
        if (this.array.length == 0) return null;
        else return this.array[this.index];
    }
    
    /**
     * Advances the loop index and returns the element at the new position in the loop.
     * 
     * @return an Object of type {@link T} at the next index.
     */
    public T next() {
        if (index + 1 >= array.length) index = 0;
        else index += 1;
        
        return current();
    }
    
    /**
     * Retreats the loop index and returns the element at the new position in the loop.
     * 
     * @return an Object of type {@link T} at the next index.
     */
    public T previous() {
        if (index - 1 < 0) index = array.length - 1;
        else index -= 1;
        
        return current();
    }
}
