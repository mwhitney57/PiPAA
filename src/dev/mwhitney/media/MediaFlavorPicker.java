package dev.mwhitney.media;

import java.awt.datatransfer.DataFlavor;
import java.util.Objects;

/**
 * A class which simplifies picking the next supported media flavor, in a
 * preferred order. The order stays true, but it is not guaranteed that every
 * media flavor will be picked. If the media flavor is not supported, it will be
 * skipped over during the picking process.
 * 
 * @author mwhitney57
 */
public class MediaFlavorPicker {
    /**
     * A flavor (or type) of media, often represented by the {@link DataFlavor} class.
     */
    public static enum MediaFlavor {
        /** A file, which could equate to any type of media during loading. */
        FILE,
        /** A raw image, not to be confused with an image file. */
        IMAGE,
        /** A string of text, often equating to a local or remote link to media. */
        STRING,
        /** An easily-recognizable web URL. */
        WEB_URL;
    }
    
    /** The default preferred pick order for media flavors, with most preferred starting at index zero. */
    public static final MediaFlavor[] DEFAULT_PICK_ORDER = { MediaFlavor.FILE, MediaFlavor.IMAGE, MediaFlavor.WEB_URL, MediaFlavor.STRING };
    
    /** The preferred pick order for media flavors in this instance. */
    private MediaFlavor[] pickOrder = null;
    /** The current pick index within the {@link #pickOrder}. */
    private int currentPick = -1;
    
    /** A boolean for whether or not the STRING media data flavor is supported. */
    private boolean STRING_SUPPORTED = false;
    /** A boolean for whether or not the IMAGE media data flavor is supported. */
    private boolean    IMG_SUPPORTED = false;
    /** A boolean for whether or not the FILE media data flavor is supported. */
    private boolean   FILE_SUPPORTED = false;
    /** A boolean for whether or not the WEB_URL media data flavor is supported. */
    private boolean WEBURL_SUPPORTED = false;
    
    /**
     * Creates a new MediaFlavorPicker with one or more {@link MediaFlavor}s to pick
     * from, in the order they are provided. This determines the order in which
     * supported flavors should be picked. If a {@link MediaFlavor} option is left
     * out, it cannot be picked. <b>Be sure to use {@link #support(MediaFlavor...)}
     * to designate which flavors are supported (compatible) with the media.</b>
     * 
     * @param flavors - one or more {@link MediaFlavor}s to pick from in the order
     *                they are passed.
     */
    public MediaFlavorPicker(MediaFlavor... flavors) {
        // Ensure non-null objects and assign pick order.
        for (final MediaFlavor flavor : Objects.requireNonNull(flavors, "MediaFlavor pick order array cannot be null.")) {
            Objects.requireNonNull(flavor, "MediaFlavor pick order cannot contain null objects.");
        }
        pickOrder = flavors;
    }
    
    /**
     * Designates support for each passed {@link MediaFlavor}. Unsupported flavors
     * are ignored during the picking process.
     * 
     * @param flavors - one or more {@link MediaFlavor}s to support.
     * @return this MediaFlavorPicker instance.
     */
    public MediaFlavorPicker support(MediaFlavor... flavors) {
        // Support the passed flavors.
        for (final MediaFlavor flavor : Objects.requireNonNull(flavors, "Supported MediaFlavors array cannot be null.")) {
            switch (Objects.requireNonNull(flavor, "Supported MediaFlavor in array cannot be null.")) {
            case STRING    -> STRING_SUPPORTED   = true;
            case IMAGE     -> IMG_SUPPORTED      = true;
            case FILE      -> FILE_SUPPORTED     = true;
            case WEB_URL   -> WEBURL_SUPPORTED   = true;
            };
//            System.out.println(flavor + " flavor supported.");  //Debug
        }
        return this;
    }
    
    /**
     * Sets support for the passed {@link MediaFlavor} using the passed boolean.
     * Unlike {@link #support(MediaFlavor...)}, this method allows for the disabling
     * of support for a particular flavor by passing a <code>false</code> boolean
     * value.
     * 
     * @param flavor    - the {@link MediaFlavor} to set support for.
     * @param supported - a boolean for whether or not the passed flavor should be
     *                  supported.
     * @return this MediaFlavorPicker instance.
     */
    public MediaFlavorPicker support(MediaFlavor flavor, boolean supported) {
        if (supported) support(flavor);
        return this;
    }
    
    /**
     * Checks if the passed {@link MediaFlavor} is supported.
     * 
     * @param flavor - the {@link MediaFlavor} to check.
     * @return <code>true</code> if the flavor is supported; <code>false</code>
     *         otherwise.
     */
    public boolean supports(MediaFlavor flavor) {
        return switch (flavor) {
        case STRING  -> this.STRING_SUPPORTED;
        case IMAGE   -> this.IMG_SUPPORTED;
        case FILE    -> this.FILE_SUPPORTED;
        case WEB_URL -> this.WEBURL_SUPPORTED;
        };
    }
    
    /**
     * Gets the {@link MediaFlavor} under the current pick. <b>This method CAN
     * increment the pick</b>, but will only do so when the current pick is invalid.
     * If the current pick is beyond the length of the internal pick order array,
     * <code>null</code> is returned.
     * 
     * @return the current {@link MediaFlavor} pick; <code>null</code> if there are
     *         no picks left.
     */
    public MediaFlavor currentPick() {
        // Continuously increment pick while within the pick order array length and picking unsupported flavors.
        while (currentPick < this.pickOrder.length && !supports(pickOrder[currentPick])) currentPick++;
        // Return null if ran outside the bounds of the pick order array.
        if (currentPick > this.pickOrder.length) return null;
        // Return next supported pick.
        else return pickOrder[currentPick];
    }
    
    /**
     * Picks the next supported {@link MediaFlavor} based on the internal pick
     * order.
     * 
     * @return the next supported {@link MediaFlavor}, if there is one;
     *         <code>null</code> otherwise.
     */
    public MediaFlavor pick() {
        // Increment pick.
        currentPick++;
        
        return currentPick();
    }
}
