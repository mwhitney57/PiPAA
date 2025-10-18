package dev.mwhitney.media.attribution;

import dev.mwhitney.media.PiPMedia;
import dev.mwhitney.media.PiPMediaAttributes;

/**
 * An interface for requesting attributes for media.
 * 
 * @author mwhitney57
 */
public interface AttributeRequestListener {
    /**
     * Requests a new set of {@link PiPMediaAttributes} using the information
     * provided in the passed {@link AttributionRequest}.
     * <p>
     * The {@link AttributeRequestListener} provides default methods for
     * flexibility when requesting attribution, such as
     * {@link #requestAttributes(PiPMedia, AttributionFlag...)} and
     * {@link #requestAttributes(Object, PiPMedia, AttributionFlag...)}. All of
     * those methods form an {@link AttributionRequest} and ultimately point to this
     * method. Therefore, this is the only method that must be defined.
     * 
     * @param req - the {@link AttributionRequest} containing information about the
     *            requested attribution.
     * @return a {@link PiPMediaAttributes} instance with the media's attributes.
     * @since 0.9.5
     */
    public PiPMediaAttributes requestAttributes(AttributionRequest req);
    /**
     * Requests a new set of {@link PiPMediaAttributes} for the passed
     * {@link PiPMedia} with no defined source object.
     * 
     * @param media - the {@link PiPMedia} to determine the attributes of.
     * @param flags - any number of attribution {@link AttributionFlag} values which
     *              affect how the attribution process executes.
     * @return a {@link PiPMediaAttributes} instance with the media's attributes.
     */
    public default PiPMediaAttributes requestAttributes(PiPMedia media, AttributionFlag... flags) {
        // Point directly to constructor. Allow it to decide the default no source value.
        return requestAttributes(new AttributionRequest(media, flags));
    }
    /**
     * Requests a new set of {@link PiPMediaAttributes} for the passed
     * {@link PiPMedia} with the passed source object.
     * 
     * @param src   - the object source requesting attribution.
     * @param media - the {@link PiPMedia} to determine the attributes of.
     * @param flags - any number of attribution {@link AttributionFlag} values which
     *              affect how the attribution process executes.
     * @return a {@link PiPMediaAttributes} instance with the media's attributes.
     * @since 0.9.5
     */
    public default PiPMediaAttributes requestAttributes(Object src, PiPMedia media, AttributionFlag... flags) {
        return requestAttributes(new AttributionRequest(src, media, flags));
    }
}
