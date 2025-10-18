package dev.mwhitney.media.attribution;

/**
 * A listener for notifying objects when media attributes are updated.
 * 
 * @author mwhitney57
 */
public interface AttributeUpdateListener {
    /**
     * Called when all, or at least multiple, attributes are updated.
     */
    public void allUpdated();
    /**
     * Called when the title attribute is updated.
     * 
     * @param title - a String with the updated title.
     */
    public void titleUpdated(String title);
}
