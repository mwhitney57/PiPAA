package dev.mwhitney.listeners;

/**
 * A listener that fires when the number of PiPWindows changes.
 * 
 * @author mwhitney57
 */
public interface PiPWindowCountListener {
    /**
     * Called when the window count has changed.
     */
    public void windowCountChanged();
}
