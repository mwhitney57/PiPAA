package dev.mwhitney.listeners.simplified;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * A simplified, functional extension of the {@link MouseListener}, meant to
 * only define and use the {@link MouseListener#mouseReleased(MouseEvent)}
 * method.
 * 
 * @author mwhitney57
 * @since 0.9.4
 * @see {@link MouseAdapter} for using more than one, but not all, of the
 *      {@link MouseListener} interface methods.
 */
@FunctionalInterface
public interface MouseReleaseListener extends MouseListener {
    // Unutilized for this type of listener -- Make empty definition default.
    @Override
    public default void mouseClicked(MouseEvent e) {}
    // Unutilized for this type of listener -- Make empty definition default.
    @Override
    public default void mousePressed(MouseEvent e) {}
    
    // Leave mouseReleased(MouseEvent) undefined for functional interface implementation.
    
    // Unutilized for this type of listener -- Make empty definition default.
    @Override
    public default void mouseEntered(MouseEvent e) {}
    // Unutilized for this type of listener -- Make empty definition default.
    @Override
    public default void mouseExited(MouseEvent e) {}
}
