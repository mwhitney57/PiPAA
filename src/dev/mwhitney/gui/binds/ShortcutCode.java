package dev.mwhitney.gui.binds;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * Custom keyboard and mouse codes that can be used with {@link KeyInput} or
 * {@link MouseInput} instances. The standard {@link KeyEvent},
 * {@link MouseEvent}, and {@link MouseWheelEvent} classes, while thorough when
 * viewed in parts, do not provide quite enough unique codes for viewing an
 * event as one. For example, scrolling events do not provide a proper,
 * directional code, which presents a problem when we differentiate binds by
 * their unique codes and modifiers.
 * <p>
 * This class fixes that problem by defining custom codes which can be matched
 * against events and then compared to the current bind configuration.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public abstract class ShortcutCode {
    // Static Custom Codes - Begin at int value 1,000,000 to easily avoid conflicting with any KeyEvent/MouseEvent codes via getKeyCode() or getButton().
    /**
     * A custom mouse button code for scrolling events. Typically not used, as
     * scrolling events will be discernible between {@link #CODE_SCROLL_UP} and
     * {@link #CODE_SCROLL_DOWN}.
     */
    public static final int CODE_SCROLL          = 1000000;
    /**
     *  A custom mouse button code for scrolling up.
     *  Rather than referring to the direction, "up" refers to the resulting {@link MouseWheelEvent#getWheelRotation()} int being positive.
     */
    public static final int CODE_SCROLL_UP       = 1000001;
    /**
     *  A custom mouse button code for scrolling events.
     *  Rather than referring to the direction, "up" refers to the resulting {@link MouseWheelEvent#getWheelRotation()} int being negative.
     */
    public static final int CODE_SCROLL_DOWN     = 1000002;
    
    /**
     * Checks if the passed int matches one of the custom codes defined within.
     * Some {@link Shortcut} bindings may use these custom codes.
     * 
     * @param code - an int that may or may not match a custom code.
     * @return <code>true</code> if the passed int matches a custom code; <code>false</code> otherwise.
     */
    public static boolean isCode(int code) {
        return switch(code) {
        case
        CODE_SCROLL,
        CODE_SCROLL_UP,
        CODE_SCROLL_DOWN -> true;
        default -> false;
        };
    }
    
    /**
     * Gets a String representation of the passed custom code.
     * This method will still succeed if the passed int does not match a custom code,
     * but will show <code>undefined</code>.
     * 
     * @param code - an int with the custom code to get a String representation of.
     * @return a String representation of the passed custom code.
     */
    public static String toString(int code) {
        return ("ShortcutCode( " + switch(code) {
        case CODE_SCROLL            -> "CODE_SCROLL";
        case CODE_SCROLL_UP         -> "CODE_SCROLL_UP";
        case CODE_SCROLL_DOWN       -> "CODE_SCROLL_DOWN";
        default -> "<undefined>";
        } + " )");
    }
}
