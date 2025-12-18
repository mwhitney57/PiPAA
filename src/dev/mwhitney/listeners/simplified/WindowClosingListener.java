package dev.mwhitney.listeners.simplified;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * A functional interface and extension upon {@link WindowListener} which
 * provides default, empty implementations for all methods other than
 * {@link #windowClosing(WindowEvent)}. This interface can be used when only the
 * {@link #windowClosing(WindowEvent)} method is needed.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
@FunctionalInterface
public interface WindowClosingListener extends WindowListener {
    @Override
    public default void windowOpened(WindowEvent e) {}
    // Not defining windowClosing method to make functional interface.
    @Override
    public default void windowClosed(WindowEvent e) {}
    @Override
    public default void windowIconified(WindowEvent e) {}
    @Override
    public default void windowDeiconified(WindowEvent e) {}
    @Override
    public default void windowActivated(WindowEvent e) {}
    @Override
    public default void windowDeactivated(WindowEvent e) {}
}
