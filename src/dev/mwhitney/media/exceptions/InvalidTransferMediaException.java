package dev.mwhitney.media.exceptions;

/**
 * An exception thrown when transfer media is invalid.
 * 
 * @author mwhitney57
 */
public class InvalidTransferMediaException extends InvalidMediaException {
    /**
     * The randomly-generated serial UID for InvalidTransferMediaExceptions.
     */
    private static final long serialVersionUID = -4428648355617682023L;

    public InvalidTransferMediaException(String errMsg) {
        super(errMsg);
    }
}
