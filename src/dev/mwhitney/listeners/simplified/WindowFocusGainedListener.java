package dev.mwhitney.listeners.simplified;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

/**
 * A simplified, functional extension of the {@link WindowFocusListener}, meant
 * for only defining and using the
 * {@link WindowFocusListener#windowGainedFocus(WindowEvent)} method.
 * 
 * @author mwhitney57
 * @since 0.9.4
 * @see {@link WindowFocusListener} for using both focus interface methods.
 */
@FunctionalInterface
public interface WindowFocusGainedListener extends WindowFocusListener {
    // Leave windowGainedFocus(WindowEvent) undefined for functional interface implementation.
    
    // Unutilized for this type of listener -- Make empty definition default.
    @Override
    public default void windowLostFocus(WindowEvent e) {}
}
