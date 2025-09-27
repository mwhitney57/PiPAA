package dev.mwhitney.media.exceptions;

/**
 * An exception thrown when media is invalid.
 * 
 * @author mwhitney57
 */
public class InvalidMediaException extends Exception {
	/**
	 * The randomly-generated serial UID for InvalidMediaExceptions.
	 */
	private static final long serialVersionUID = 9139884369844580786L;
	
	public InvalidMediaException(String errMsg) {
		super(errMsg);
	}
}
