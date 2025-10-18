package dev.mwhitney.media.attribution;

import dev.mwhitney.media.PiPMedia;

/**
 * A series of request data for media attribution. Typically used to attribute
 * media with {@link PiPMediaAttributor}.
 * <p>
 * The {@link #src()} usually references a {@link PiPWindow} where the media
 * will ultimately display. That window is used as a link for the attribution
 * processes, which allows the window's closure to trigger an interruption of
 * any of its pending attribution processes without affecting other windows.
 * This is helpful for preventing unnecessary or rogue background processes.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public record AttributionRequest(
    /**
     * The source of the attribution request. Typically a {@link PiPWindow} where
     * the media would ultimately be displayed. The source may be used as a link for
     * certain attribution processes, allowing those specific linked processes to be
     * interrupted if necessary.
     */
    Object src,
    /** The unattributed media which is being requested for attribution. */
    PiPMedia media,
    /** One or more {@link AttributionFlag} flags which determine how the attribution process should proceed. */
    AttributionFlag... flags
) {
    /**
     * Creates an attribution request for the passed media with no source object.
     * <p>
     * If no flags are provided, the defaults will be used.
     * 
     * @param media - the {@link PiPMedia} to attribute.
     * @param flags - one or more {@link AttributionFlag} flags.
     */
    public AttributionRequest(PiPMedia media, AttributionFlag... flags) {
        this(null, media, flags);
    }
}
