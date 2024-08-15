package dev.mwhitney.listeners;

/**
 * A simple, functional interface for requesting repaints on a Swing component.
 * 
 * @author mwhitney57
 */
@FunctionalInterface
public interface PaintRequester {
    /**
     * Requests a repaint of the object implementing this interface, which is either
     * a Swing component or an object within one.
     */
    public void requestPaint();
}
