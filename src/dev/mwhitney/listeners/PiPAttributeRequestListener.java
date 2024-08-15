package dev.mwhitney.listeners;

import dev.mwhitney.media.PiPMedia;
import dev.mwhitney.media.PiPMediaAttributes;

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
     * @return a PiPMediaAttributes object with the media's attributes.
     */
    public default PiPMediaAttributes requestAttributes(PiPMedia media) { return requestAttributes(media, false); }
    /**
     * Requests a new set of PiPMediaAttributes for the passed PiPMedia.
     * 
     * @param media - the PiPMedia to determine the attributes of.
     * @param raw   - a boolean which, if <code>true</code>, bypasses user
     *              configuration and attributes the raw media source.
     * @return a PiPMediaAttributes object with the media's attributes.
     */
    public PiPMediaAttributes requestAttributes(PiPMedia media, boolean raw);
}
