package dev.mwhitney.listeners;

import dev.mwhitney.gui.PiPWindow;
import dev.mwhitney.media.PiPMedia;

/**
 * An interface for handing off media to other windows or objects.
 * 
 * @author mwhitney57
 */
public interface PiPHandoffListener extends PiPListener {
    /**
     * Performs a handoff of PiPMedia.
     * 
     * @param media - the PiPMedia to handoff.
     */
    public PiPWindow handoff(PiPMedia media);
}
