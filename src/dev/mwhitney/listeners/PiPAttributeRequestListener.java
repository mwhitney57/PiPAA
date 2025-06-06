package dev.mwhitney.listeners;

import dev.mwhitney.media.PiPMedia;
import dev.mwhitney.media.PiPMediaAttributes;
import dev.mwhitney.media.PiPMediaAttributor.Flag;

/**
 * An interface for requesting attributes for media.
 * 
 * @author mwhitney57
 */
public interface PiPAttributeRequestListener extends PiPListener {
    /**
     * Requests a new set of PiPMediaAttributes for the passed PiPMedia.
     * 
     * @param media - the PiPMedia to determine the attributes of.
     * @param flags - any number of attribution {@link Flag} values which affect how
     *              the attribution process executes.
     * @return a PiPMediaAttributes object with the media's attributes.
     */
    public PiPMediaAttributes requestAttributes(PiPMedia media, Flag... flags);
}
