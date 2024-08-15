package dev.mwhitney.listeners;

/**
 * An interface for the transfer of media.
 * 
 * @author mwhitney57
 */
public interface PiPMediaTransferListener extends PiPListener {
    /**
     * A method called when a data transfer fails.
     * The two accepted methods of transfer are:
     * <br>
     * - Drag and Drop
     * <br>
     * - Clipboard (Copy & Paste)
     * 
     * @param msg - a String with a message pertaining to the transfer failure.
     */
    public void transferFailed(String msg);
}
