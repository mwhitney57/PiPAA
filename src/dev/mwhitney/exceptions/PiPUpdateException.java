package dev.mwhitney.exceptions;

/**
 * An exception thrown when an update process unexpectedly fails.
 * 
 * @author mwhitney57
 */
public class PiPUpdateException extends Exception {
    /**
     * The randomly-generated serial UID for MediaModificationExceptions.
     */
    private static final long serialVersionUID = -4388741295638854571L;
    
    /** The underlying exception, if one exists. */
    private Exception exception;
    
    /**
     * Creates a new PiPUpdateException with no underlying exception. Shorthand for
     * passing a <code>null</code> second argument to
     * {@link #PiPUpdateException(String, Exception)}.
     * 
     * @param errMsg - the exception's error message.
     */
    public PiPUpdateException(String errMsg) {
        this(errMsg, null);
    }
    
    /**
     * Creates a new PiPUpdateException.
     * 
     * @param errMsg - the exception's error message.
     * @param e      - the underlying exception, if one exists.
     */
    public PiPUpdateException(String errMsg, Exception e) {
        super(errMsg);
        this.exception = e;
    }
    
    /**
     * Gets the total message, which consists of this PiPUpdateException's message
     * and any underlying {@link Exception}'s message.
     * 
     * @return a String with the total message.
     */
    public String getTotalMessage() {
        return (this.getMessage() + (hasUnderlying() ? " (" + this.exception.getMessage() + ")" : ""));
    }
    
    /**
     * Returns the underlying {@link Exception} for this PiPUpdateException
     * instance, if one exists.
     * 
     * @return the underlying {@link Exception}.
     */
    public Exception underlying() {
        return this.exception;
    }
    
    /**
     * Checks if this PiPUpdateException has an underlying {@link Exception}.
     * 
     * @return <code>true</code> if there is an underlying exception;
     *         <code>false</code> otherwise.
     */
    public boolean hasUnderlying() {
        return (this.exception != null);
    }
}
