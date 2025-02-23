package dev.mwhitney.listeners;

import dev.mwhitney.media.PiPMediaCMD;

/**
 * An interface for sending media commands to windows.
 * 
 * @author mwhitney57
 */
public interface PiPCommandListener extends PiPListener {
    /**
     * A method called to send a media command to the window. It is up to the window
     * as to whether or not it wants to handle the command.
     * 
     * @param <C>  - the type of the argument(s).
     * @param cmd  - the PiPMediaCMD to send.
     * @param args - any arguments to send alongside the command.
     */
    public <C> void sendMediaCMD(PiPMediaCMD cmd, @SuppressWarnings("unchecked") C... args);
}
