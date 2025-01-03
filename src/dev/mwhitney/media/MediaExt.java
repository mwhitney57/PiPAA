package dev.mwhitney.media;

import java.util.Objects;

import dev.mwhitney.exceptions.InvalidMediaExtensionException;
import dev.mwhitney.main.PiPEnum;

/**
 * File extensions for different media types and containers. All extensions
 * should be in UPPERCASE format per language and application conventions.
 * 
 * @author mwhitney57
 */
public enum MediaExt implements PiPEnum<MediaExt> {
    /* PLAYLIST/STREAMING */
    M3U8,TS,
    /* VIDEO */
    MP4,MKV,WEBM,M4V,MOV,AVI,
    /* IMAGE */
    PNG,JPG,JPEG,TIFF,BMP,AVIF,
    /* GIF */
    GIF,WEBP,
    /* AUDIO */
    MP3,M4A,WAV,WMA,OGG,FLAC,OPUS;
    
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
        } catch(IllegalArgumentException e) {}
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
     *         <code>null</code> otherwise.
     * @see {@link #parse(String)} for unsafe parsing which may throw an exception.
     */
    public static MediaExt parseSafe(String str) {
        MediaExt ext = null;
        try {
            ext = MediaExt.parse(str);
        } catch(InvalidMediaExtensionException e) {}
        return ext;
    }
    
    /**
     * Checks if the passed MediaExt supports audio artwork.
     * Artwork is the cover or album art for songs or general
     * audio media. Therefore, if the passed MediaExt is not
     * audio, then this method will obviously return <code>false</code>.
     * 
     * @param ext - the audio MediaExt 
     * @return <code>true</code> if the MediaExt supports audio artwork; <code>false</code> otherwise.
     */
    public static boolean supportsArtwork(final MediaExt ext) {
        if (ext == null) return false;
        return switch (ext) {
        case MP3, WAV -> true;
        default -> false;
        };
    }
}
