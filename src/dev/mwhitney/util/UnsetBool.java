package dev.mwhitney.util;

/**
 * A class which allows for "triple-option boolean" functionality. Typically,
 * booleans can either be <code>true</code> or <code>false</code>. If using a
 * {@link Boolean} object, one could technically achieve three options by
 * setting the object to <code>null</code>. However, not only is this
 * cumbersome, but it's prone to errors in multi-threaded environments, since
 * the object may not be final or effectively final. This class is similar to
 * {@link java.util.concurrent.atomic.AtomicBoolean}, but it is simpler and
 * allows for an "unset" value.
 * <p>
 * Creating an instance of this class automatically uses the value
 * {@link #UNSET}. This value can be set using {@link #set(boolean)}, but
 * <b>cannot be unset again</b>.
 * 
 * @author mwhitney57
 * @since 0.9.4
 */
public class UnsetBool {
    /** An internal int representing the <code>false</code> boolean value. */
    private static final int FALSE = -1;
    /** An internal int representing an unset or empty value. */
    private static final int UNSET =  0;
    /** An internal int representing the <code>true</code> boolean value. */
    private static final int TRUE  =  1;
    /** An int with the current value. */
    private volatile int value = UNSET;
    
    /**
     * Checks if the value is set to either <code>true</code> or <code>false</code>.
     * 
     * @return <code>true</code> if the value is set at all; <code>false</code>
     *         otherwise.
     * @see {@link #isUnset()} for checking the inverse of this method.
     */
    public boolean isSet() {
        return this.value != UNSET;
    }
    /**
     * Checks if the value is unset, equaling neither <code>true</code> nor
     * <code>false</code>.
     * 
     * @return <code>true</code> if the value is unset; <code>false</code>
     *         otherwise.
     * @see {@link #isSet()} for checking the inverse of this method.
     */
    public boolean isUnset() {
        return this.value == UNSET;
    }
    /**
     * Checks if the value is <code>true</code>.
     * 
     * @return <code>true</code> if the value is <code>true</code>;
     *         <code>false</code> otherwise.
     */
    public boolean isTrue() {
        return this.value == TRUE;
    }
    /**
     * Checks if the value is <code>false</code>.
     * 
     * @return <code>true</code> if the value is <code>false</code>;
     *         <code>false</code> otherwise.
     */
    public boolean isFalse() {
        return this.value == FALSE;
    }
    /**
     * Sets the value to be the passed boolean. The value cannot become unset again
     * after calling this method.
     * 
     * @param val - the boolean value to set.
     */
    public void set(boolean val) {
        this.value = (val ? TRUE : FALSE);
    }
}
