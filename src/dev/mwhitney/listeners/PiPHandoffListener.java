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
     * Performs a handoff of {@link PiPMedia} to a new {@link PiPWindow}, returning
     * the window after it is created.
     * 
     * @param media - the PiPMedia to handoff.
     * @return the new {@link PiPWindow} that was created as a result of this
     *         handoff.
     */
    public PiPWindow handoff(PiPMedia media);
}
