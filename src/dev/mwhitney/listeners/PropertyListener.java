package dev.mwhitney.listeners;

import dev.mwhitney.main.PiPProperty;

/**
 * A basic listener for application property changes.
 * 
 * @author mwhitney57
 */
public interface PropertyListener {
    /**
     * Called when an application property has changed.
     * 
     * @param prop  - the PiPProperty that has changed.
     * @param value - the new property value.
     */
    public void propertyChanged(PiPProperty prop, String value);
    /**
     * Called to retrieve the current state of the passed application property.
     * 
     * @param prop - the PiPProperty to get the state of.
     * @return a String with the property's current value.
     */
    public <T> T propertyState(PiPProperty prop, Class<T> rtnType);
}
