package dev.mwhitney.listeners;

/**
 * A listener for the PiPAA tray item/icon.
 * 
 * @author mwhitney57
 */
public interface PiPTrayListener extends PiPListener {
    /**
     * Called when the tray receives input from the user to close the application.
     */
    public void applicationClosing();
}
