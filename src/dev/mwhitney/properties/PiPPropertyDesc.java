package dev.mwhitney.properties;

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
        GIF_PLAYBACK_MODE     = "Basic playback is smooth, uses less system resources, allows for transparency, and uses the image player with different controls. Advanced playback converts GIFs into videos before playing.",
        BASIC_GIF_PLAYBACK    = "Basic playback uses an image viewer component instead of VLC to view GIFs. This is smooth and uses less system resources, but has less controls (i.e. playback speed or play/pause).",
        ADV_GIF_PLAYBACK      = "Advanced playback downloads GIF media and converts it to a video format before playing. Only takes effect when \"Basic GIF Playback\" is disabled.",
        IMG_SCALING_QUALITY   = "Choose how to scale images when the window size changes. Smart is recommended. It switches between both modes while maintaining quality. Only affects GIF playback when in \"Basic\" mode.",
        DND_PREFER_LINK       = "Prefer to source drag and drop media from links, if able. The default is off, which typically results in PiPAA copy/pasting the media directly.",
        SINGLE_PLAY_MODE      = "Only allows one window to play media at a time. Playing media will automatically pause media in any other windows. Pausing the only window playing media will leave all windows paused.",
        GLOBAL_MUTED          = "While enabled, every window is muted. However, each window will remember its own mute state which will take effect when the global mute is disabled.",
        DEFAULT_VOLUME        = "The default volume level of each new window.",
        DEFAULT_PLAYBACK_RATE = "The default playback rate or speed of each new window.",
        USE_SYS_VLC           = "Prefer to use the VLC build installed on the system, as opposed to the one shipped with PiPAA. If not found, PiPAA will default to using its own. Leaving this off improves application startup time. Requires application restart to take effect.",
        USE_SYS_BINARIES      = "Prefer to use the yt-dlp, gallery-dl, and ffmpeg binaries installed on the system, as opposed to the ones shipped with PiPAA. For each binary, if it is not found, PiPAA will default to using its own.",
        USE_HW_DECODING       = "Configure new PiPAA windows to utilize hardware acceleration. May improve or destabilize performance, depending on the system and drivers.",
        USE_SUPER_RES         = "Configure new PiPAA windows to be capable of utilizing NVIDIA's RTX Video Super Resolution feature. Requires hardware-accelerated decoding. ONLY AVAILABLE ON LATEST VLC VERSIONS WITH SUPPORTED NVIDIA RTX (GPUs).",
        DISABLE_CACHE         = "Disables the caching of media. Media may still be downloaded in order to be played, but it will be automatically deleted when its window closes.",
        OVERWRITE_CACHE       = "If incoming media already exists under the exact same filename and path, how should PiPAA handle the conflict?",
        DOWNLOAD_WEB_MEDIA    = "Attempt to download non-local media before playback, which is often necessary. Downloaded media is put in the cache folder, but it will be deleted if the cache is disabled.",
        CONVERT_WEB_INDIRECT  = "Attempts to discover a direct source from an indirect media link. When enabled, more media can be played directly without having to cache it. Can be inconsistent across platforms.",
        CONFIRM_CLOSE_ALL     = "Require confirmation before closing all windows using the shortcut.",
        TRIM_TRANSPARENCY     = "Trims transparent edges from images or GIFs before playing, keeping window edges closer to media content. Trimming can take a while for big or long GIFs. Results are saved to the cache.",
        TRANSPARENT_PASS      = "Allows clicks to pass-through transparent parts of the window. Useful when viewing images with transparency. Borders remain clickable unless size is locked.",
        APP_UPDATES           = "Updates for the entire PiPAA application. Select the type of updates to check for below. Most users can stick with the default."
                + " For newer builds that are less stable, select Beta or Snapshot versions. You can set the automatic update schedule or manually update using the button below.",
        BIN_UPDATES           = "Updates for certain binaries or packages shipped with PiPAA. These include yt-dlp and gallery-dl, which are critical for downloading media."
                + " You can set the automatic update schedule or manually update using the button below.";
}
