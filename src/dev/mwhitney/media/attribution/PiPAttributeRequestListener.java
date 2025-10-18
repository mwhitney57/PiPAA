package dev.mwhitney.media.attribution;

import dev.mwhitney.listeners.PiPListener;
import dev.mwhitney.media.PiPMedia;
import dev.mwhitney.media.PiPMediaAttributes;
import dev.mwhitney.media.attribution.AttributionFlag;

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
     * @param flags - any number of attribution {@link AttributionFlag} values which affect how
     *              the attribution process executes.
     * @return a PiPMediaAttributes object with the media's attributes.
     */
    public PiPMediaAttributes requestAttributes(PiPMedia media, AttributionFlag... flags);
}
