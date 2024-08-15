package dev.mwhitney.main;

/**
 * The descriptions for each {@link PiPProperty}.
 * 
 * @author mwhitney57
 */
public final class PiPPropertyDesc {
    /**
     * A description for a {@link PiPProperty} counterpart.
     * 
     * @see {@link PiPProperty}
     * */
    public static final String
        DARK_MODE             = "N/A",
        GIF_PLAYBACK_MODE     = "Basic playback is smooth, uses less system resources, allows for transparency, and uses the image player with different controls. Advanced playback converts GIFs into videos before playing.",
        BASIC_GIF_PLAYBACK    = "Basic playback uses an image viewer component instead of VLC to view GIFs. This is smooth and uses less system resources, but has less controls (i.e. playback speed or play/pause).",
        ADV_GIF_PLAYBACK      = "Advanced playback downloads GIF media and converts it to a video format before playing. Only takes effect when \"Basic GIF Playback\" is disabled.",
        DND_PREFER_LINK       = "Prefer to source drag and drop media from links, if able. The default is off, which typically results in PiPAA copy/pasting the media directly.",
        GLOBAL_MUTED          = "While enabled, every window is muted. However, each window will remember its own mute state, which will take effect when the global mute is disabled.",
        DEFAULT_VOLUME        = "The default volume level of each new window.",
        DEFAULT_PLAYBACK_RATE = "The default playback rate or speed of each new window.",
        USE_SYS_BINARIES      = "Prefer to use the yt-dlp, gallery-dl, and ffmpeg binaries installed on the system, as opposed to the ones shipped with PiPAA. For each binary, if it is not found, PiPAA will default to using its included version.",
        DISABLE_CACHE         = "Disables the caching of media. Media may still be downloaded in order to be played, but it will be automatically deleted when its window closes.",
        OVERWRITE_CACHE       = "If incoming media already exists under the exact same filename and path, how should PiPAA handle the conflict?",
        DOWNLOAD_WEB_MEDIA    = "Attempt to download non-local media before playback, which is often necessary. Downloaded media is put in the cache folder, but it will be deleted if the cache is disabled.",
        CONVERT_WEB_INDIRECT  = "Attempts to discover a direct source from an indirect media link. This allows for direct playback of more content, without having to cache the media. Can be inconsistent across platforms.",
        TRIM_TRANSPARENCY     = "Trims transparent edges from images or GIFs before playing, keeping window edges closer to media content. Trimming can take a while for big or long GIFs and is stored in the cache.",
        BIN_UPDATE_FREQUENCY  = "How frequently should PiPAA check for updates to the binaries it uses for downloading and converting media? The automatic updates are run at launch and marginally increase startup time.";
}
