package dev.mwhitney.util.interfaces;

/**
 * A simple, functional interface for providing a user-friendly error message.
 * Commonly implemented by {@link Exception} classes. Useful for offering a less
 * technical error message that can be presented to the user without sacrificing
 * the standard one that's typically intended for the eyes of developers.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
@FunctionalInterface
public interface UserFriendlyErrorMessage {
    /**
     * Gets a user-friendly message that represents the exception or class when an
     * error occurs.
     * 
     * @return a String with the user-friendly error message.
     */
    public String getUserFriendlyErrorMessage();
}
