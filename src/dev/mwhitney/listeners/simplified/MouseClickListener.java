package dev.mwhitney.listeners.simplified;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * A simplified, functional extension of the {@link MouseListener}, meant to
 * only define and use the {@link MouseListener#mouseClicked(MouseEvent)}
 * method.
 * 
 * @author mwhitney57
 * @since 0.9.4
 * @see {@link MouseAdapter} for using more than one, but not all, of the
 *      {@link MouseListener} interface methods.
 */
@FunctionalInterface
public interface MouseClickListener extends MouseListener {
    // Leave mouseClicked(MouseEvent) undefined for functional interface implementation.
    
    // Unutilized for this type of listener -- Make empty definition default.
    @Override
    public default void mousePressed(MouseEvent e) {}
    // Unutilized for this type of listener -- Make empty definition default.
    @Override
    public default void mouseReleased(MouseEvent e) {}
    // Unutilized for this type of listener -- Make empty definition default.
    @Override
    public default void mouseEntered(MouseEvent e) {}
    // Unutilized for this type of listener -- Make empty definition default.
    @Override
    public default void mouseExited(MouseEvent e) {}
}
