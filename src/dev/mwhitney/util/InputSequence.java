package dev.mwhitney.util;

import java.util.Objects;

/**
 * A sequence of inputs, capped at a fixed length. If a new input is received,
 * the current sequence is shifted based on the {@link Order}, and the oldest
 * input is discarded to make room.
 * <p>
 * The <b>length</b> is determined at construction, based on how many elements
 * are present in the passed array. The simplest way to begin using an input
 * sequence is by calling {@link #InputSequence(Object[])} and passing an empty
 * array of the desired sequence length. For example, passing
 * <code>new String[5]</code> into the constructor would create a String
 * sequence with a fixed length of 5.
 * <p>
 * The sequence values will be <code>null</code> by default after construction,
 * but can be immediately filled with another value using {@link #fill(Object)}.
 * <p>
 * Common Examples:
 * <pre>
 * // Has null defaults – No fill call ↓
 * new InputSequence(new Object[5]);
 * 
 * // Filled with custom default values.
 * new InputSequence(new String[5]).fill("");
 * new InputSequence(new Integer[20]).fill(0);
 * new InputSequence(new MyClass[1]).fill(new MyClass());
 * </pre>
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public class InputSequence<T> {
    /**
     * The order which inputs should be placed in the sequence.
     * <p>
     * Though replaceable by a boolean while there are only two values, an enum is
     * more readable and allows for additional, perhaps more complex, orders to be
     * added in the future.
     */
    public static enum Order {
        /**
         * The normal order. Shifts ← right to left, replacing the right end.
         */
        NORMAL,
        /**
         * The reverse order. Shifts → left to right, replacing the left end.
         */
        REVERSE;
    }
    /** The current {@link Order} in which to place inputs into the sequence. */
    private final Order order;
    /** The array of type {@link T} with the sequence data. */
    private final T[] array;

    /**
     * Creates a new InputSequence using the passed array. The passed array data
     * must be non-<code>null</code> and have a length of at least one, but does not
     * need to contain any starting inputs.
     * <p>
     * Inputs can be added to the sequence using {@link #in(Object)} or
     * {@link #inWithOrder(Object, Order)}. The {@link Order} determines where
     * inputs are shifted and placed.
     * <p>
     * A non-<code>null</code> default value can be filled for all elements in the
     * sequence using {@link #fill(Object)}.
     * 
     * @param order - the {@link Order} in which to input values into the sequence.
     * @param array - the array, of type {@link T}, with the desired length. May be
     *              filled or empty.
     * @throws NullPointerException     if the passed array is <code>null</code>.
     * @throws InvalidArgumentException if the passed array's length is below one.
     */
    public InputSequence(Order order, T[] arr) {
        this.array = Objects.requireNonNull(arr);
        this.order = order;
        
        // The internal array should never have a length <= 0.
        if (this.array.length < 1) throw new IllegalArgumentException("Cannot create InputSequence with a length < 1.");
    }
    
    /**
     * Creates a new InputSequence using the passed array. The passed array data
     * must be non-<code>null</code> and have a length of at least one, but does not
     * need to contain any starting inputs. <b>This constructor uses the default
     * order of {@link Order#NORMAL}.</b>
     * <p>
     * Inputs can be added to the sequence using {@link #in(Object)} or
     * {@link #inWithOrder(Object, Order)}. The {@link Order} determines where
     * inputs are shifted and placed.
     * <p>
     * A non-<code>null</code> default value can be filled for all elements in the
     * sequence using {@link #fill(Object)}.
     * 
     * @param array - the array, of type {@link T}, with the desired length. May be
     *              filled or empty.
     * @throws NullPointerException     if the passed array is <code>null</code>.
     * @throws InvalidArgumentException if the passed array's length is below one.
     */
    public InputSequence(T[] arr) {
        this(Order.NORMAL, arr);
    }
    
    /**
     * Fills every element of the sequence with the passed value. This method is
     * especially useful right after construction when a non-<code>null</code>
     * default value is desired.
     * 
     * @param value - the value to fill throughout the sequence.
     * @return this InputSequence instance.
     */
    public InputSequence<T> fill(T value) {
        for (int i = 0; i < this.array.length; i++) {
            this.array[i] = value;
        }
        return this;
    }
    
    /**
     * Shifts the elements in the sequence based on the passed {@link Order}. The
     * unshifted element in the sequence keeps its previous value, but is intended
     * to be <b>replaced</b> elsewhere in this class' logic. This method is
     * typically called by {@link #inWithOrder(Object, Order)} for that reason.
     * <p>
     * For example, a {@link Order#NORMAL} shift would look something like this,
     * with the last element awaiting replacement afterwards:
     * 
     * <pre>
     * shift(Order.NORMAL) → { 0, 1, 2, 3, 4 } → { 1, 2, 3, 4, 4 }
     * </pre>
     * 
     * @param order - the {@link Order} determining the nature of the shift.
     */
    private void shift(Order order) {
        // Shift logic changes depending on order.
        // Determine start and end indices.
        final int startIndex = switch (order) {
        case NORMAL  -> 0;
        case REVERSE -> this.array.length - 1;
        };
        final int endIndex   = replaceableIndexByOrder(order);
        
        // Perform shift.
        switch (order) {
        case NORMAL -> {
            // Example: { 0, 1, 2 } → { 1, 2, 2 }
            for (int i = startIndex; i < endIndex; i++)
                this.array[i] = this.array[i + 1];
        }
        case REVERSE -> {
            // Example: { 0, 1, 2 } → { 0, 0, 1 }
            for (int i = startIndex; i > endIndex; i--)
                this.array[i] = this.array[i - 1];
        }
        }
    }
    
    /**
     * Returns the index within the sequence that would be replaced after a shift,
     * based on the passed {@link Order}.
     * 
     * @param order - the {@link Order} determining which index would be replaced.
     * @return an int with the index.
     */
    private int replaceableIndexByOrder(Order order) {
        return switch (order) {
        case NORMAL  -> this.array.length - 1;
        case REVERSE -> 0;
        };
    }
    
    /**
     * Checks if the sequence "has" the passed index, ensuring it is in bounds. So
     * long as the array itself has a length, and the passed index would point to an
     * element within the sequence, this method returns <code>true</code>.
     * 
     * @param index - an int with the index to check.
     * @return <code>true</code> if the sequence has the index; <code>false</code>
     *         otherwise.
     */
    public boolean hasIndex(int index) {
        return (this.array.length > 0 && index >= 0 && index < this.array.length);
    }
    
    /**
     * Gets the raw sequence data array.
     * 
     * @return an array containing all elements of the sequence in their current
     *         positions.
     */
    public T[] getSequence() {
        return this.array;
    }
    
    /**
     * Gets the element of the sequence at the passed index, which can have a value
     * of <code>null</code>. This method can also return <code>null</code> if the
     * passed index is out of bounds and does not point to an element in the
     * sequence.
     * 
     * @param index - an int with the index to get.
     * @return the element in the sequence at the specified index, or
     *         <code>null</code>.
     */
    public T get(int index) {
        if (!hasIndex(index)) return null;
        return this.array[index];
    }
    
    /**
     * Sets the value of the element at the specified index within the sequence.
     * This method is generally not needed but can be useful if overwriting specific
     * values is necessary. Otherwise, consider using {@link #in(Object)} or
     * {@link #inWithOrder(Object, Order)}.
     * 
     * @param value - the value to set.
     * @param index - an int with the index where the value should be set.
     */
    public void set(T value, int index) {
        if (!hasIndex(index)) return;
        
        this.array[index] = value;
    }
    
    /**
     * Input a value into the sequence with the sequence's current {@link Order}.
     * 
     * @param input - the value to input into the sequence.
     * @return this InputSequence instance.
     * @see {@link #inWithOrder(Object, Order)} to input a value with a different
     *      {@link Order}.
     */
    public InputSequence<T> in(T input) {
        return inWithOrder(input, this.order);
    }
    
    /**
     * Input a value into the sequence with the passed {@link Order}.
     * 
     * @param input - the value to input into the sequence.
     * @param order - the {@link Order} to use when shifting the current elements
     *              and placing the new input.
     * @return this InputSequence instance.
     * @see {@link #inWithOrder(Object, Order)} to input a value with a different
     *      {@link Order}.
     */
    public InputSequence<T> inWithOrder(T input, Order order) {
        shift(order);
        this.array[replaceableIndexByOrder(order)] = input;
        return this;
    }
    
    /**
     * Gets the length of the sequence.
     * 
     * @return an int with the sequence length.
     */
    public int length() {
        return this.array.length;
    }
    
    @Override
    public String toString() {
        // Optimized toString which prints in format: "{ x, x, x, x }"
        final StringBuilder builder = new StringBuilder((this.array.length*3) + 2);
        builder.append("{ ");
        for (int i = 0; i < this.array.length; i++) {
            if (i != 0) builder.append(", ");
            builder.append(this.array[i]);
        }
        builder.append(" }");
        return builder.toString();
    }
}
