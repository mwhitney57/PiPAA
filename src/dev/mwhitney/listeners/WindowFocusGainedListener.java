package dev.mwhitney.listeners;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

/**
 * A simple extension upon the {@link WindowFocusListener} interface. Turns
 * {@link WindowFocusListener#windowGainedFocus(WindowEvent)} into a functional
 * interface method, allowing for lambda expression usage.
 * {@link WindowFocusListener#windowLostFocus(WindowEvent)} does nothing by
 * default.
 * 
 * @author mwhitney57
 * @since 0.9.4
 */
@FunctionalInterface
public interface WindowFocusGainedListener extends WindowFocusListener {
    // The functional interface method becomes WindowFocusListener#windowGainedFocus(WindowEvent).
    // Override the lost focus method and make it do nothing by default.
    @Override
    public default void windowLostFocus(WindowEvent e) {}
}
