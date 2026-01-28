package dev.mwhitney.media;

import java.util.Objects;

import dev.mwhitney.media.exceptions.InvalidMediaExtensionException;
import dev.mwhitney.util.PiPAAUtils;
import dev.mwhitney.util.interfaces.PiPEnum;

/**
 * File extensions for different media types and containers. All extensions
 * should be in UPPERCASE format per language and application conventions.
 * 
 * @author mwhitney57
 */
public enum MediaExt implements PiPEnum<MediaExt> {
    /* PLAYLIST/STREAMING */
    M3U8,MPD,TS,
    /* VIDEO */
    MP4,MKV,WEBM,M2V,M4V,MOV,AVI,WMV,
    /* IMAGE */
    PNG,JPG,JPEG,TIFF,BMP,AVIF,
    /* GIF */
    GIF,WEBP,
    /* AUDIO */
    MP3,M4A,MKA,WAV,WMA,OGG,FLAC,OPUS;
    
    /**
     * Checks whether the passed String matches any {@link MediaExt}. This method
     * will return {@code false} if the passed String is {@code null}.
     * 
     * @param ext - a String with the extension to check.
     * @return {@code true} if an extension match is found; {@code false} otherwise.
     */
    public static boolean matchesAny(String ext) {
        // Check if extension matches any media extensions.
        return ext != null && parseSafe(ext) != null;
    }
    
    /**
     * Attempts to parse the passed String and match it to a valid MediaExt value.
     * 
     * @param str - a String with the MediaExt value.
     * @return the MediaExt that matches the passed String value, if one exists.
     * @throws InvalidMediaExtensionException if the passed String does not match
     *                                        any MediaExt value.
     */
    public static MediaExt parse(String str) throws InvalidMediaExtensionException {
        try {
            if (str != null) return MediaExt.valueOf(str.toUpperCase());
        } catch(IllegalArgumentException e) {}  // Catch if no match, which leads to InvalidMediaExtensionException being thrown below.
        throw new InvalidMediaExtensionException("No valid media extension value for '" + Objects.toString(str, "NULL").toUpperCase() + "'");
    }
    
    /**
     * Attempts to safely parse the passed String and match it to a valid MediaExt
     * value. Unlike {@link #parse(String)}, this method does not throw any
     * exceptions. If you this is not desired, consider using
     * {@link #parse(String)}.
     * 
     * @param str - a String with the MediaExt value.
     * @return the MediaExt that matches the passed String value, if one exists;
     *         {@code null} otherwise.
     * @see {@link #parse(String)} for unsafe parsing which may throw an exception.
     */
    public static MediaExt parseSafe(String str) {
        try {
            return MediaExt.parse(str);
        } catch(InvalidMediaExtensionException e) {
            return null;
        }
    }
    
    /**
     * Checks if the passed MediaExt supports audio artwork. Artwork is the cover or
     * album art for songs or general audio media. Therefore, if the passed MediaExt
     * is not audio, then this method will obviously return {@code false}.
     * 
     * @param ext - the audio MediaExt
     * @return {@code true} if the MediaExt supports audio artwork;
     *         {@code false} otherwise.
     * @see {@link #supportedAsArtwork(MediaExt)} to see if a particular image media
     *      extension supports being embedded as artwork.
     */
    public static boolean supportsArtwork(final MediaExt ext) {
        return switch (ext) {
        case MP3, WAV -> true;
        case null, default -> false;
        };
    }
    
    /**
     * Checks if the passed MediaExt is supported as artwork. Only a certain few
     * image extensions are supported as audio media artwork.
     * 
     * @param ext - the image MediaExt
     * @return {@code true} if the MediaExt is supported as audio artwork;
     *         {@code false} otherwise.
     * @see {@link #supportsArtwork(MediaExt)} to see if a particular audio media
     *      extension supports the embedding of artwork within it.
     */
    public static boolean supportedAsArtwork(final MediaExt ext) {
        return switch (ext) {
        case JPEG, JPG, PNG -> true;
        case null, default -> false;
        };
    }
    
    /**
     * Gets the MediaExt values as a single String, separated by the passed
     * {@link CharSequence}.
     * 
     * @param separator - the {@link CharSequence} to separate each MediaExt value.
     * @return a String with the values, separated by the passed
     *         {@link CharSequence}.
     * @since 0.9.4
     */
    public static String values(final CharSequence separator) {
        return String.join(separator, PiPAAUtils.toStringArray(MediaExt.class));
    }
    
    /**
     * Gets the MediaExt values as a single String, separated by the passed
     * character.
     * 
     * @param separator - the char to separate each MediaExt value.
     * @return a String with the values, separated by the passed char.
     * @since 0.9.4
     * @see {@link #values(CharSequence)} for using multiple separation characters.
     */
    public static String values(final char separator) {
        return values(String.valueOf(separator));
    }
}
