package dev.mwhitney.exceptions;

/**
 * An exception thrown when a binary attempts an action it does not support.
 * 
 * @author mwhitney57
 */
public class UnsupportedBinActionException extends Exception {
    /**
     * The randomly-generated serial UID for UnsupportedBinActionExceptions.
     */
    private static final long serialVersionUID = -4630943028028354838L;

    public UnsupportedBinActionException(String errMsg) {
        super(errMsg);
    }
}
