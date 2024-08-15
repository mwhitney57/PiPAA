package dev.mwhitney.exceptions;

/**
 * An exception thrown when a media's extension is invalid.
 * 
 * @author mwhitney57
 */
public class InvalidMediaExtensionException extends InvalidMediaException {
    /**
	 * The randomly-generated serial UID for InvalidMediaExceptions.
	 */
    private static final long serialVersionUID = 7321279990303545976L;
	
	public InvalidMediaExtensionException(String errMsg) {
		super(errMsg);
	}
}
