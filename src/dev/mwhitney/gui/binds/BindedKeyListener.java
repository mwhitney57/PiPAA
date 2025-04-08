package dev.mwhitney.gui.binds;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * A simplified {@link KeyListener} for the shortcut and bind system that only
 * requires implementation of {@link #keyPressed(KeyEvent)} and
 * {@link #keyReleased(KeyEvent)} methods.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public interface BindedKeyListener extends KeyListener {
    // Unutilized for this type of listener -- Make empty definition default.
    @Override
    public default void keyTyped(KeyEvent e) {}
    
    // Leave keyPressed(KeyEvent) undefined for bind implementation.
    // Leave keyReleased(KeyEvent) undefined for bind implementation.
}
