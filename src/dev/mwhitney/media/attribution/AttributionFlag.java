package dev.mwhitney.media.attribution;

import dev.mwhitney.media.PiPMediaAttributes.SRC_TYPE;
import dev.mwhitney.properties.PiPProperty;
import dev.mwhitney.util.interfaces.PiPEnum;

/**
 * A flag for attribution which determines how the attributor behaves.
 * 
 * @author mwhitney57
 * @since 0.9.4
 */
public enum AttributionFlag implements PiPEnum<AttributionFlag> {
    // Specific attribution parts. Commented out -- not used for now.
    //   EXTENSION,
    //   TITLE,
    //   ID,
    //   TYPE,
    //   SRC_TYPE,
    //   SRC_PLATFORM,
    
    /**
     * A flag representing a typical, full attribution. A full attribution is the
     * norm, attributing everything it needs or may need so that the application can
     * use the resulting media to the best of its ability.
     */
    FULL,
    /**
     * A flag representing a quicker, less-detailed attribution. Since quick
     * attributions skip over a few time-consuming processes, they save time, but
     * they may not save important properties of the media. Use this type of
     * attribution sparingly, when you just need the simple things.
     * <p>
     * <b>Note:</b> Quick attributions will likely fail if the source is
     * {@link SRC_TYPE#WEB_INDIRECT}, since core attributes cannot be determined
     * quickly.
     */
    QUICK,
    /**
     * A flag which tells the attributor to attribute the raw media without
     * converting the source, ignoring the user configuration entirely.
     * <p>
     * Therefore, using this flag will prevent
     * {@link PiPProperty#CONVERT_WEB_INDIRECT} from having any effect, as this
     * logic will be skipped during attribution.
     */
    RAW_ATTRIBUTION;
}