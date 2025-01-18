package dev.mwhitney.listeners.simplified;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

/**
 * A simplified, functional extension of the {@link WindowFocusListener}, meant
 * to only define and use the
 * {@link WindowFocusListener#windowLostFocus(WindowEvent)} method.
 * 
 * @author mwhitney57
 * @since 0.9.4
 * @see {@link WindowFocusListener} for using both focus interface methods.
 */
@FunctionalInterface
public interface WindowFocusLostListener extends WindowFocusListener {
    // Unutilized for this type of listener -- Make empty definition default.
    @Override
    public default void windowGainedFocus(WindowEvent e) {}
    
    // Leave windowLostFocus(WindowEvent) undefined for functional interface implementation.
}
