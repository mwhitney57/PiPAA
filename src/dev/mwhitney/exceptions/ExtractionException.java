package dev.mwhitney.exceptions;

/**
 * An exception thrown when an extraction fails.
 * 
 * @author mwhitney57
 */
public class ExtractionException extends Exception {
    /**
     * The randomly-generated serial UID for InvalidMediaExceptions.
     */
    private static final long serialVersionUID = -4063566882336854658L;
    
    public ExtractionException(String errMsg) {
        super(errMsg);
    }
}
