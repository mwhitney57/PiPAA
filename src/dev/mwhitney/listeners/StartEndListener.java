package dev.mwhitney.listeners;

/**
 * A listener for receiving updates when something has started and ended. For
 * clarity in specific cases, this listener can be extended with new names.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public interface StartEndListener {
    /**
     * Called when something has started.
     */
    public void started();
    /**
     * Called when something has ended.
     */
    public void ended();
}
