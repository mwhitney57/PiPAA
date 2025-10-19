package dev.mwhitney.util.interfaces;

/**
 * An interface for PiPAA enums.
 * <p>
 * <b>Do not implement this interface in standard classes</b>, as it is only
 * intended to be used with enums. Certain methods, such as
 * {@link #matchAny(String)} will fail if the implementing class is not an enum.
 * 
 * @param <E> the enum that is implementing this interface.
 * @author mwhitney57
 */
public interface PiPEnum<E extends Enum<E>> {
    /**
     * Attempts to match the passed String with a value in the enum under the
     * provided class. If a matching value is found, it is immediately returned. If
     * not, then <code>null</code> will be returned. This matching process is
     * case-insensitive.
     * <p>
     * Methods like {@link #match(String)} or {@link #matchAny(String)} can be
     * useful, but they require an instance/value to be called on. This static
     * method provides a clear way to check for any match without requiring an
     * instance to reference.
     * 
     * @param <P> - the {@link PiPEnum} to check for matches with.
     * @param c   - the Class of the {@link PiPEnum} to match with.
     * @param s   - the String to match.
     * @return the matching value within the passed enum class; <code>null</code>
     *         otherwise if no match exists.
     * @since 0.9.5
     */
    public static <P extends PiPEnum<?>> P match(Class<P> c, String s) {
        // Finds the first match and returns the value. Returns null if no match exists.
        for (final P value : c.getEnumConstants()) {
            if (value.is(s)) return value;
        }
        return null;
    }
    /**
     * Attempts to match the passed String with this enum value. If it is a match, this
     * enum value is returned. If it is not a match, then <code>null</code> is
     * returned. This matching process is case-insensitive.
     * 
     * @param s - the String to match.
     * @return this enum value if it's a match; <code>null</code> otherwise.
     * @see {@link #matchAny(String)} for matching with any value in an enum while
     *      having a fallback in the case of no matches.
     */
    public default PiPEnum<?> match(final String s) {
        if (is(s)) return this;
        else return null;
    }
    /**
     * Attempts to safely match the passed String with any value in the enum. If no
     * match is found, this enum value is used as a fallback. This matching process
     * is case-insensitive. To match with a specific enum value, without the safety
     * net, consider using {@link #match(String)}.
     * 
     * @param s - the String to match.
     * @return the matching enum value, or this enum value if no match is found.
     * @see {@link #match(String)} for an unsafe, 1:1 comparison which returns
     *      <code>null</code> when there's no match.
     */
    @SuppressWarnings("unchecked")
    public default E matchAny(final String s) {
        for (final PiPEnum<?> value : this.getClass().getEnumConstants()) {
            if (value.is(s)) return (E) value;
        }
        return (E) this;
    }
    /**
     * Checks if the passed enum is equivalent to this enum.
     * 
     * @param <E> e - the enum to compare.
     * @return <code>true</code> if the enum matches; <code>false</code> otherwise.
     */
    public default boolean is(final E e) {
        return (this == e);
    }
    /**
     * Checks if the passed String is equivalent to this enum.
     * This method is <b>not</b> case sensitive.
     * 
     * @param s - the String to compare.
     * @return <code>true</code> if the String matches; <code>false</code>
     *         otherwise.
     */
    public default boolean is(final String s) {
        return (s != null) && s.equalsIgnoreCase(this.toString());
    }
    /**
     * Checks if the passed enum is <b>not</b> equivalent to this enum.
     * 
     * @param <E> e - the enum to compare.
     * @return <code>true</code> if the enum <b>does not</b> match;
     *         <code>false</code> otherwise.
     */
    public default boolean not(final E e) {
        return !this.is(e);
    }
    /**
     * Checks if the passed String is <b>not</b> equivalent to this enum. This
     * method is <b>not</b> case sensitive.
     * 
     * @param s - the String to compare.
     * @return <code>true</code> if the String <b>does not</b> match;
     *         <code>false</code> otherwise.
     */
    public default boolean not(final String s) {
        return !this.is(s);
    }
    /**
     * Checks if this enum exists within the passed array.
     * Internally uses this enum's {@link #is(Enum)} method.
     * Overriding and changing that method may affect this method's behavior.
     * 
     * @param array - {@link E} array to check against.
     * @return <code>true</code> if this enum exists in the passed array; <code>false</code> otherwise.
     * @see {@link #in(Enum[])} to check if it does not exist instead.
     */
    public default boolean in(E[] array) {
        if (array == null) return false;
        
        for (final E e : array) {
            if (is(e)) return true;
        }
        return false;
    }
    /**
     * Checks if this enum <b>does not</b> exist within the passed array.
     * Internally uses this enum's {@link #in(Enum[])} method.
     * Overriding and changing that method may affect this method's behavior.
     * 
     * @param array - {@link E} array to check against.
     * @return <code>true</code> if this enum does not exist in the passed array; <code>false</code> otherwise.
     * @see {@link #in(Enum[])} to check if it exists instead.
     */
    public default boolean notIn(E[] array) {
        return !in(array);
    }
    /**
     * Gets the index of this enum within its internal <code>values()</code> array.
     * This method will return <code>-1</code> if no match is found, but <b>this
     * should never happen</b>. This enum has to be non-<code>null</code> to execute
     * this method, and the values returned should correspond to its own enum class.
     * 
     * @return the int index of this enum in its internal array.
     */
    public default int index() {
        final PiPEnum<?>[] values = this.getClass().getEnumConstants();
        for (int i = 0; i < values.length; i++) {
            if (this.is(values[i].toString())) return i;
        }
        return -1;
    }
    /**
     * Gets an uppercase {@link #toString()} version of this enum.
     * 
     * @return a String with the enum in uppercase.
     */
    public default String upper() {
        return this.toString().toUpperCase();
    }
    /**
     * Gets a lowercase {@link #toString()} version of this enum.
     * 
     * @return a String with the enum in lowercase.
     */
    public default String lower() {
        return this.toString().toLowerCase();
    }
}
