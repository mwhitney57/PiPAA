package dev.mwhitney.listeners.simplified;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * A simplified, functional extension of the {@link KeyListener}, meant to only
 * define and use the {@link KeyListener#keyPressed(KeyEvent)} method.
 * 
 * @author mwhitney57
 * @since 0.9.4
 * @see {@link KeyAdapter} for using more than one, but not all, of the
 *      {@link KeyListener} interface methods.
 */
@FunctionalInterface
public interface KeyPressListener extends KeyListener {
    // Unutilized for this type of listener -- Make empty definition default.
    @Override
    public default void keyTyped(KeyEvent e) {}
    
    // Leave keyPressed(KeyEvent) undefined for functional interface implementation.
    
    // Unutilized for this type of listener -- Make empty definition default.
    @Override
    public default void keyReleased(KeyEvent e) {}
}
