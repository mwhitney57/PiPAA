package dev.mwhitney.main;

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
     * @param s     - the String to compare.
     * @return <code>true</code> if the String matches; <code>false</code>
     *         otherwise.
     */
    public default boolean is(final String s) {
        return (s != null) && s.equalsIgnoreCase(this.toString());
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
