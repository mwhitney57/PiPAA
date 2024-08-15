package dev.mwhitney.media;

/**
 * An enum with commands for media within PiPWindows.
 * 
 * @author mwhitney57
 */
public enum PiPMediaCMD {
    /** Sets the source of the media player(s). */
    SET_SRC,
    /** Uses the media's current state to do the inverse, playing if paused and pausing if playing. */
    PLAYPAUSE,
    /** Plays the media. */
    PLAY,
    /** Pauses the media. */
    PAUSE,
    /** Seeks the media by a singular frame. */
    SEEK_FRAME,
    /** Seeks the media by an offset or to a specific time. */
    SEEK,
    /** Adjusts the media's volume by an offset or sets it to a specific amount. */
    VOLUME_ADJUST,
    /** Uses the media's current state to do the inverse, unmuting if muted and muting if unmuted. */
    MUTEUNMUTE,
    /** Mutes the media. */
    MUTE,
    /** Unmutes the media. */
    UNMUTE,
    /** Adjusts the media's playback rate (speed) by an offset or sets it to a specific rate. */
    SPEED_ADJUST,
    /** Zooms in or out on the media. Only works when using the image viewer. */
    ZOOM,
    /** Pans around the viewport showing the media. Only works when zoomed in and when using the image viewer. */
    PAN,
    /** Enters or exits fullscreen mode for the picture-in-picture window showing the media. */
    FULLSCREEN,
    /** Reloads the media from its original source. */
    RELOAD,
    /** Closes the media. */
    CLOSE;
}
