package dev.mwhitney.listeners;

import dev.mwhitney.gui.PiPWindow;
import dev.mwhitney.media.PiPMedia;

/**
 * An interface for communicating with PiPWindows.
 * 
 * @author mwhitney57
 */
public interface PiPWindowListener extends PiPListener {
    // Override to Force Correct Object Return Type
    @Override
    public PiPWindow get();
    /**
     * A method called to set the window's location on screen. Intended to
     * automatically handle executing the call on the EDT, if not called from the
     * EDT. <b>In that case, this method call will not block until completion.</b>
     * 
     * @param x - an int with the x-coordinate on screen.
     * @param y - an int with the y-coordinate on screen.
     */
//    @Deprecated(since = "beta20", forRemoval = true)
//    public void setWindowLocation(int x, int y);
    /**
     * A method called to set the window media. Intended to automatically handle
     * executing the call off of the EDT, if called from the EDT. <b>In that case,
     * this method call will not block until completion of setting the media.</b>
     * 
     * @param media - the new window media, or <code>null</code> to close the
     *              current media.
     */
    public void setWindowMedia(PiPMedia media);
}
