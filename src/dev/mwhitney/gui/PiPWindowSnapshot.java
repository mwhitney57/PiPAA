package dev.mwhitney.gui;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;

import dev.mwhitney.media.PiPMedia;
import dev.mwhitney.media.PiPMediaAttributes;
import dev.mwhitney.util.interfaces.PiPEnum;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

/**
 * A snapshot of the state of a {@link PiPWindow} taken at any given time.
 * <p>
 * Snapshots only include <i>some</i> of the available data from a window. The
 * data that can be included in the snapshot is selected depending on the needs
 * of the application's features.
 * <p>
 * During construction, {@link SnapshotData} types can be specified to limit the
 * capture data based on the needs of the calling code.
 * 
 * @author mwhitney57
 * @since 0.9.4
 */
public class PiPWindowSnapshot {
    /**
     * Data available for capture within a snapshot.
     * The value {@link SnapshotData#ALL} includes all available snapshot data.
     */
    public enum SnapshotData implements PiPEnum<SnapshotData> {
        /**
         * All available snapshot data.
         * Does not refer to a specific piece of data itself.
         */
        ALL,
        /**
         * Data pertaining to the window's media player.
         */
        PLAYER,
        /**
         * Data pertaining to the window itself.
         */
        WINDOW,
        /**
         * Data pertaining to the media's various sources.
         */
        MEDIA_SOURCES,
        /**
         * Data pertaining to the media's attributes.
         */
        MEDIA_ATTRIBUTES;
    }

    /**
     * The internal snapshot data array which dictates what data types are captured
     * by this snapshot.
     */
    private SnapshotData[] data;
    /**
     * The internal snapshot data array which dictates what data types were captured
     * by this snapshot after {@link #capture(PiPWindow)} was called.
     * <p>
     * This may differ from {@link #data}, which simply designates what data
     * <i>can</i> be captured. If certain data was not able to be captured, it will
     * not be present here.
     */
    private SnapshotData[] captured;
    /** The position of the media player. */
    private float   playerPosition;
    /** The playback rate of the media player. */
    private float   playerRate;
    /** The volume of the media player. */
    private int     playerVolume;
    /** The mute status of the media player. */
    private boolean playerMuted;
    /** The playing status of the media player. */
    private boolean playerPlaying;
    /** The x-coordinate of the window. */
    private int     windowX;
    /** The y-coordinate of the window. */
    private int     windowY;
    /** The width of the window in pixels. */
    private int     windowWidth;
    /** The height of the window in pixels. */
    private int     windowHeight;
    /** The primary source of the media. */
    private String  mediaSrc;
    /** The cache source of the media. */
    private String  mediaCacheSrc;
    /** The trim source of the media. */
    private String  mediaTrimSrc;
    /** The convert source of the media. */
    private String  mediaConvertSrc;
    /** The attributes of the media. */
    private PiPMediaAttributes mediaAttributes;
    
    /**
     * Creates a new snapshot, intended to capture data from a {@link PiPWindow}
     * upon calling {@link #capture(PiPWindow)}.
     * 
     * @param exclude - a boolean for whether or not the passed {@link SnapshotData}
     *                values should be excluded from all available data options
     *                rather than included.
     * @param data    - one or more {@link SnapshotData} values to allow this
     *                snapshot to capture. Alternatively, these values are used as
     *                exclusions from the list of available data values.
     * @see {@link #PiPWindowSnapshot(SnapshotData...)} for a shorthand of calling
     *      with the <code>exclude</code> parameter value of false.
     */
    public PiPWindowSnapshot(final boolean exclude, final SnapshotData... data) {
        // Default to ALL data being captured by this snapshot if no specifics provided.
        if (data == null || data.length == 0)
            this.data = new SnapshotData[]{ SnapshotData.ALL };
        else if (exclude) {
            final ArrayList<SnapshotData> dataExclude = new ArrayList<>(Arrays.asList(SnapshotData.values()));
            dataExclude.remove(SnapshotData.ALL);
            for(int i = 0; i < data.length; i++) {
                dataExclude.remove(data[i]);
            }
            this.data = dataExclude.toArray(new SnapshotData[0]);
        }
        else this.data = data;
    }
    
    /**
     * Creates a new snapshot, intended to capture data from a {@link PiPWindow}
     * upon calling {@link #capture(PiPWindow)}.
     * 
     * @param data - one or more {@link SnapshotData} values to allow this snapshot
     *             to capture.
     * @see {@link #PiPWindowSnapshot(boolean, SnapshotData...)} to treat the passed
     *      {@link SnapshotData} values as exclusions from all available data
     *      options.
     */
    public PiPWindowSnapshot(final SnapshotData... data) {
        this(false, data);
    }
    
    /**
     * Checks if this snapshot is configured to capture all of the passed
     * {@link SnapshotData} types.
     * 
     * @param data - one or more {@link SnapshotData} types to check.
     * @return <code>true</code> if this snapshot can capture all of the passed
     *         types.
     */
    private boolean captures(final SnapshotData... data) {
        if (capturesAll()) return true;
        for (final SnapshotData dataItem : data) {
            if (!ArrayUtils.contains(this.data, dataItem))
                return false;
        }
        return true;
    }
    
    /**
     * Checks if this snapshot is configured to capture {@link SnapshotData#ALL}
     * data types.
     * 
     * @return <code>true</code> if this snapshot captures {@link SnapshotData#ALL}
     *         data types.
     */
    private boolean capturesAll() {
        return ArrayUtils.contains(this.data, SnapshotData.ALL);
    }
    
    /**
     * Checks if this snapshot has captured all of the passed {@link SnapshotData}
     * types.
     * 
     * @param data - one or more {@link SnapshotData} types to check.
     * @return <code>true</code> if this snapshot has captured all of the passed
     *         types.
     */
    private boolean captured(final SnapshotData... data) {
        if (capturedAll()) return true;
        for (final SnapshotData dataItem : data) {
            if (!ArrayUtils.contains(this.captured, dataItem))
                return false;
        }
        return true;
    }
    
    /**
     * Checks if this snapshot has captured {@link SnapshotData#ALL} data types.
     * 
     * @return <code>true</code> if this snapshot captured {@link SnapshotData#ALL}
     *         data types.
     */
    private boolean capturedAll() {
        return ArrayUtils.contains(this.captured, SnapshotData.ALL);
    }

    /**
     * Takes a snapshot of the passed {@link PiPWindow}, capturing available data of
     * all types supported by this snapshot.
     * 
     * @param window - the {@link PiPWindow} to capture.
     * @return this PiPWindowSnapshot instance.
     */
    public PiPWindowSnapshot capture(final PiPWindow window) {
        final ArrayList<SnapshotData> capped = new ArrayList<>();
        if (this.captures(SnapshotData.WINDOW)) {
            setWindowX(window.getX());
            setWindowY(window.getY());
            setWindowWidth(window.getInnerWidth());
            setWindowHeight(window.getInnerHeight());
            capped.add(SnapshotData.WINDOW);
        }
        final PiPMedia media = window.getMedia();
        if (this.captures(SnapshotData.MEDIA_SOURCES) && window.hasMedia()) {
            setMediaSrc(media.getSrc());
            setMediaCacheSrc(media.getCacheSrc());
            setMediaTrimSrc(media.getTrimSrc());
            setMediaConvertSrc(media.getConvertSrc());
            capped.add(SnapshotData.MEDIA_SOURCES);
        }
        if (this.captures(SnapshotData.MEDIA_ATTRIBUTES) && window.hasAttributedMedia()) {
            setMediaAttributes(media.getAttributes());
            capped.add(SnapshotData.MEDIA_ATTRIBUTES);
        }
        final MediaPlayer mediaPlayer = window.getMediaPlayer();
        if (this.captures(SnapshotData.PLAYER) && mediaPlayer != null) {
            setPlayerPosition(mediaPlayer.status().position());
            setPlayerRate(mediaPlayer.status().rate());
            setPlayerVolume(mediaPlayer.audio().volume());
            setPlayerMuted(mediaPlayer.audio().isMute());
            setPlayerPlaying(mediaPlayer.status().isPlaying());
            capped.add(SnapshotData.PLAYER);
        }
        
        // Update internal captured array to reflect what succeeded.
        this.captured = capped.toArray(new SnapshotData[0]);
        return this;
    }

    /**
     * Applies the captured data within this snapshot to the passed
     * {@link PiPWindow}.
     * <p>
     * Will not apply anything if this snapshot failed to capture the corresponding
     * data type, regardless of how this snapshot was configured.
     * <p>
     * <b>Important:</b> Since this method makes Swing calls, it <b>should be called
     * from the EDT</b>.
     * 
     * @param window - the {@link PiPWindow} to apply this snapshot to.
     * @return this PiPWindowSnapshot instance.
     */
    public PiPWindowSnapshot apply(final PiPWindow window) {
        Objects.requireNonNull(window, "Cannot apply window snapshot to <null> window.");
        if (this.captured(SnapshotData.WINDOW)) {
            window.setLocation(this.windowX, this.windowY);
            window.changeSize(new Dimension(this.windowWidth, this.windowHeight));
        }
        return this;
    }
    /**
     * Applies the captured data within this snapshot to the passed
     * {@link PiPMedia}.
     * <p>
     * Will not apply anything if this snapshot failed to capture the corresponding
     * data type, regardless of how this snapshot was configured.
     * 
     * @param media - the {@link PiPMedia} to apply this snapshot to.
     * @return this PiPWindowSnapshot instance.
     */
    public PiPWindowSnapshot apply(final PiPMedia media) {
        Objects.requireNonNull(media, "Cannot apply window snapshot to <null> window media.");
        if (this.captured(SnapshotData.MEDIA_SOURCES)) {
            media.setSrc(this.mediaSrc);
            media.setCacheSrc(this.mediaCacheSrc);
            media.setTrimSrc(this.mediaTrimSrc);
            media.setConvSrc(this.mediaConvertSrc);
        }
        if (this.captured(SnapshotData.MEDIA_ATTRIBUTES)) {
            media.setAttributes(this.mediaAttributes);
            if (this.mediaAttributes != null) media.setAttributed();
        }
        return this;
    }

    /**
     * Applies the captured data within this snapshot to the passed
     * {@link MediaPlayer}.
     * <p>
     * Will not apply anything if this snapshot failed to capture the corresponding
     * data type, regardless of how this snapshot was configured.
     * 
     * @param mediaPlayer - the {@link MediaPlayer} to apply this snapshot to.
     * @return this PiPWindowSnapshot instance.
     */
    public PiPWindowSnapshot apply(final MediaPlayer mediaPlayer) {
        Objects.requireNonNull(mediaPlayer, "Cannot apply window snapshot to <null> window media player.");
        if (this.captured(SnapshotData.PLAYER)) {
            mediaPlayer.controls().setPosition(this.playerPosition);
            mediaPlayer.controls().setRate(this.playerRate);
            mediaPlayer.audio().setVolume(this.playerVolume);
            mediaPlayer.audio().setMute(this.playerMuted);
            mediaPlayer.controls().setPause(!this.playerPlaying);
        }
        return this;
    }

    /**
     * Applies all types of available capture data within this snapshot to the
     * passed {@link PiPWindow} and its individual parts.
     * <p>
     * Will not apply certain data if this snapshot failed to capture that data
     * type, regardless of how this snapshot was configured.
     * 
     * @param window - the {@link PiPWindow} to apply this snapshot to.
     */
    public void applyAll(final PiPWindow window) {
        apply(window);
        if (window.hasMedia()) apply(window.getMedia());
        if (window.getMediaPlayer() != null) apply(window.getMediaPlayer());
    }
    /**
     * Gets the media player position data.
     * 
     * @return a float with the position.
     */
    public float getPlayerPosition() {
        return playerPosition;
    }
    /**
     * Sets the media player position data.
     * 
     * @param playerPosition - a float with the position.
     */
    public void setPlayerPosition(float playerPosition) {
        this.playerPosition = playerPosition;
    }
    
    /**
     * Gets the media player playback rate data.
     * 
     * @return a float with the playback rate.
     */
    public float getPlayerRate() {
        return playerRate;
    }
    /**
     * Sets the media player playback rate data.
     * 
     * @param playerRate - a float with the playback rate.
     */
    public void setPlayerRate(float playerRate) {
        this.playerRate = playerRate;
    }
    
    /**
     * Gets the media player volume data.
     * 
     * @return an int with the volume.
     */
    public int getPlayerVolume() {
        return playerVolume;
    }
    /**
     * Sets the media player volume data.
     * 
     * @param playerVolume - an int with the volume.
     */
    public void setPlayerVolume(int playerVolume) {
        this.playerVolume = playerVolume;
    }
    
    /**
     * Checks the media player muted state data.
     * 
     * @return <code>true</code> if the player was muted; <code>false</code>
     *         otherwise.
     */
    public boolean isPlayerMuted() {
        return playerMuted;
    }
    /**
     * Sets the media player muted state data.
     * 
     * @param playerMuted - a boolean for the muted state.
     */
    public void setPlayerMuted(boolean playerMuted) {
        this.playerMuted = playerMuted;
    }
    
    /**
     * Checks the media player playing state data.
     * 
     * @return <code>true</code> if the player was playing; <code>false</code>
     *         otherwise.
     */
    public boolean isPlayerPlaying() {
        return playerPlaying;
    }
    /**
     * Sets the media player playing state data.
     * 
     * @param playerPlaying - a boolean for the playing state.
     */
    public void setPlayerPlaying(boolean playerPlaying) {
        this.playerPlaying = playerPlaying;
    }
    
    /**
     * Gets the window x-coordinate data.
     * 
     * @return an int with the x-coordinate.
     */
    public int getWindowX() {
        return windowX;
    }
    /**
     * Sets the window x-coordinate data.
     * 
     * @param windowX - an int with the x-coordinate.
     */
    public void setWindowX(int windowX) {
        this.windowX = windowX;
    }
    
    /**
     * Gets the window y-coordinate data.
     * 
     * @return an int with the y-coordinate.
     */
    public int getWindowY() {
        return windowY;
    }
    /**
     * Sets the window y-coordinate data.
     * 
     * @param windowY - an int with the y-coordinate.
     */
    public void setWindowY(int windowY) {
        this.windowY = windowY;
    }
    
    /**
     * Gets the window width data.
     * 
     * @return an int with the window width.
     */
    public int getWindowWidth() {
        return windowWidth;
    }
    /**
     * Sets the window width data.
     * 
     * @param windowWidth - an int with the window width.
     */
    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }
    
    /**
     * Gets the window height data.
     * 
     * @return an int with the window height.
     */
    public int getWindowHeight() {
        return windowHeight;
    }
    /**
     * Sets the window height data.
     * 
     * @param windowHeight - an int with the window height.
     */
    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
    }
    
    /**
     * Checks if this snapshot contains a media source.
     * 
     * @return <code>true</code> if this snapshot contains a media source;
     *         <code>false</code> otherwise.
     */
    public boolean hasMediaSrc() {
        return (getMediaSrc() != null);
    }
    /**
     * Gets the media source data.
     * 
     * @return a String with the media source.
     */
    public String getMediaSrc() {
        return mediaSrc;
    }
    /**
     * Sets the media source data.
     * 
     * @param mediaSrc - a String with the media source.
     */
    public void setMediaSrc(String mediaSrc) {
        this.mediaSrc = mediaSrc;
    }

    /**
     * Checks if this snapshot contains a media cache source.
     * 
     * @return <code>true</code> if this snapshot contains a media cache source;
     *         <code>false</code> otherwise.
     */
    public boolean hasMediaCacheSrc() {
        return (getMediaCacheSrc() != null);
    }
    /**
     * Gets the media cache source data.
     * 
     * @return a String with the media cache source.
     */
    public String getMediaCacheSrc() {
        return mediaCacheSrc;
    }
    /**
     * Sets the media cache source data.
     * 
     * @param mediaCacheSrc - a String with the media cache source.
     */
    public void setMediaCacheSrc(String mediaCacheSrc) {
        this.mediaCacheSrc = mediaCacheSrc;
    }
    
    /**
     * Checks if this snapshot contains a media trim source.
     * 
     * @return <code>true</code> if this snapshot contains a media trim source;
     *         <code>false</code> otherwise.
     */
    public boolean hasMediaTrimSrc() {
        return (getMediaTrimSrc() != null);
    }
    /**
     * Gets the media trim source data.
     * 
     * @return a String with the media trim source.
     */
    public String getMediaTrimSrc() {
        return mediaTrimSrc;
    }
    /**
     * Sets the media trim source data.
     * 
     * @param mediaTrimSrc - a String with the media trim source.
     */
    public void setMediaTrimSrc(String mediaTrimSrc) {
        this.mediaTrimSrc = mediaTrimSrc;
    }
    
    /**
     * Checks if this snapshot contains a media convert source.
     * 
     * @return <code>true</code> if this snapshot contains a media convert source;
     *         <code>false</code> otherwise.
     */
    public boolean hasMediaConvertSrc() {
        return (getMediaConvertSrc() != null);
    }
    /**
     * Gets the media convert source data.
     * 
     * @return a String with the media convert source.
     */
    public String getMediaConvertSrc() {
        return mediaConvertSrc;
    }
    /**
     * Sets the media convert source data.
     * 
     * @param mediaConvertSrc - a String with the media convert source.
     */
    public void setMediaConvertSrc(String mediaConvertSrc) {
        this.mediaConvertSrc = mediaConvertSrc;
    }
    
    /**
     * Gets the media attributes data.
     * 
     * @return the {@link PiPMediaAttributes} data.
     */
    public PiPMediaAttributes getMediaAttributes() {
        return mediaAttributes;
    }
    /**
     * Sets the media attributes data.
     * 
     * @param mediaAttributes - the {@link PiPMediaAttributes} data.
     */
    public void setMediaAttributes(PiPMediaAttributes mediaAttributes) {
        this.mediaAttributes = mediaAttributes;
    }

    /**
     * Nullifies all non-primitive data within this snapshot, allowing for it to be
     * garbage collected sooner if this snapshot is still in use.
     */
//    public void erase() {
//        this.mediaSrc        = null;
//        this.mediaCacheSrc   = null;
//        this.mediaTrimSrc    = null;
//        this.mediaConvertSrc = null;
//    }
}
