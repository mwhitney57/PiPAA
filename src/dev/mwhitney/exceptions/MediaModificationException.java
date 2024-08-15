package dev.mwhitney.exceptions;

/**
 * An exception thrown when media modification fails.
 * 
 * @author mwhitney57
 */
public class MediaModificationException extends Exception {
    /**
     * The randomly-generated serial UID for MediaModificationExceptions.
     */
    private static final long serialVersionUID = 5349221806203159317L;
    
    public MediaModificationException(String errMsg) {
        super(errMsg);
    }
}
