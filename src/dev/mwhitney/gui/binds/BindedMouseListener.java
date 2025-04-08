package dev.mwhitney.gui.binds;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * A simplified {@link MouseListener} for the shortcut and bind system that only
 * requires implementation of {@link #mousePressed(MouseEvent)} and
 * {@link #mouseReleased(MouseEvent)} methods.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public interface BindedMouseListener extends MouseListener {
    // Unutilized for this type of listener -- Make empty definition default.
    @Override
    public default void mouseClicked(MouseEvent e) {}
    
    // Leave mousePressed(MouseEvent) undefined for bind implementation.
    // Leave mouseReleased(MouseEvent) undefined for bind implementation.
    
    // Unutilized for this type of listener -- Make empty definition default.
    @Override
    public default void mouseEntered(MouseEvent e) {}
    // Unutilized for this type of listener -- Make empty definition default.
    @Override
    public default void mouseExited(MouseEvent e) {}
}
