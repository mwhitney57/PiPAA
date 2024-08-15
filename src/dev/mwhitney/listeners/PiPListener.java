package dev.mwhitney.listeners;

/**
 * A listener for PiPAA classes for communication between objects.
 * Specific listeners and adapters implement this interface.
 * 
 * @author mwhitney57
 */
public interface PiPListener {
    /**
     * Gets the parent, controller, or reference object of the object holding this listener.
     * 
     * @return the parent or controller object.
     */
    public Object get();
}