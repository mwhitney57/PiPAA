package dev.mwhitney.media;

import java.awt.Dimension;
import java.util.Objects;

import dev.mwhitney.main.PiPEnum;

/**
 * A set of web media-specific attributes.
 * 
 * @author mwhitney57
 */
public class WebMediaFormat {
    /**
     * Web media format values.
     * Each defines how the application should handle/load the web media.
     */
    public static enum FORMAT implements PiPEnum<FORMAT> {
        HTTP,
        HLS,
        GALLERY_DL;
    }
    
    /** The set format value for this WebMediaFormat instance. */
    private FORMAT format;
    /** A String with the media's full title. */
    private String title;
    /** A String with the media's unique ID, if one exists. */
    private String id;
    /** An int with the media's item number, which is used when multiple media exist within one post. */
    private int playlistItem;
    /** A String with the media's source. */
    private String src;
    /** A {@link MediaExt} with the media's file extension. */
    private MediaExt extension;
    /** A Dimension which holds the x (width) and y (height) values for the media's resolution. */
    private Dimension resolution;
    /** A boolean which for whether or not the media is solely audio. */
    private boolean audioOnly;
    
    /**
     * Gets the {@link FORMAT} the media.
     * 
     * @return the {@link FORMAT} of the media.
     */
    public FORMAT format() {
        return this.format;
    }
    
    /**
     * Sets the {@link FORMAT} of the media.
     * 
     * @param format - a {@link FORMAT} for the media, which describes how it should be
     *               handled/loaded.
     * @return this WebMediaFormat instance.
     */
    public WebMediaFormat setFormat(FORMAT format) {
        this.format = format;
        return this;
    }
    
    /**
     * Checks if the web media format has a proper title. A proper title is defined
     * as not being <code>null</code> while also having a defined, not unknown, set
     * of characters. If the title reads as <code>Unknown</code>, then this method
     * will return <code>false</code>.
     * 
     * @return <code>true</code> if the title is not null and is known;
     *         <code>false</code> otherwise.
     */
    public boolean hasTitle() {
        return (title() != null && !title().equals("Unknown"));
    }
    
    /**
     * Gets the title of the media.
     * 
     * @return a String with the media title.
     */
    public String title() {
        return this.title;
    }
    
    /**
     * Sets the title of the media.
     * 
     * @param title - a String with the media title.
     * @return this WebMediaFormat instance.
     */
    public WebMediaFormat setTitle(String title) {
        this.title = title;
        return this;
    }
    
    /**
     * Gets the ID of the media. The ID, if available or present, is the number,
     * hash, or general set of characters that uniquely identifies a piece of media,
     * or the post it was found in. Not all media will have an ID, but most major
     * platforms will.
     * 
     * @return a String with the unique ID.
     */
    public String id() {
        return this.id;
    }
    
    /**
     * Sets the ID of the media. The ID, if available or present, is the number,
     * hash, or general set of characters that uniquely identifies a piece of media,
     * or the post it was found in. Not all media will have an ID, but most major
     * platforms will.
     * 
     * @param id - a String with the unique ID.
     * @return this WebMediaFormat instance.
     */
    public WebMediaFormat setID(String id) {
        this.id = id;
        return this;
    }
    
    /**
     * Checks if this web media is one item among others in the same post/thread.
     * 
     * @return <code>true</code> if this is one item among others in the same
     *         post/thread; <code>false</code> otherwise.
     */
    public boolean isItem() {
        return (item() > 0);
    }
    
    /**
     * Gets the item number of the media. The item number refers to the specific
     * item within a post containing multiple media. It helps to identify which
     * media in that bunch is being referenced by this WebMediaFormat.
     * 
     * @return an int with the item number of the media.
     */
    public int item() {
        return this.playlistItem;
    }
    
    /**
     * Sets the item number of the media. The item number refers to the specific
     * item within a post containing multiple media. It helps to identify which
     * media in that bunch is being referenced by this WebMediaFormat.
     * 
     * @param i - an int for the item number of the media.
     * @return this WebMediaFormat instance.
     */
    public WebMediaFormat setItem(int i) {
        this.playlistItem = i;
        return this;
    }
    
    /**
     * Gets the source of the media.
     * 
     * @return the media source as a String.
     */
    public String src() {
        return this.src;
    }
    
    /**
     * Sets the source of the media.
     * 
     * @param src - a String with the media source.
     * @return this WebMediaFormat instance.
     */
    public WebMediaFormat setSrc(String src) {
        this.src = src;
        return this;
    }
    
    /**
     * Gets the extension of the media.
     * 
     * @return the media's extension as a MediaExt.
     */
    public MediaExt extension() {
        return this.extension;
    }
    
    /**
     * Sets the extension of the media to be the passed MediaExt.
     * 
     * @param extension - the MediaExt to set.
     * @return this WebMediaFormat instance.
     */
    public WebMediaFormat setExtension(MediaExt extension) {
        this.extension = extension;
        return this;
    }
    
    /**
     * Sets the extension of the media using the passed String. If the passed String
     * does not match any of the valid {@link MediaExt} values, then the extension
     * will be set to <code>null</code>.
     * 
     * @param extension - a String with the extension to attempt to set.
     * @return this WebMediaFormat instance.
     */
    public WebMediaFormat setExtension(String extension) {
        try {
            this.extension = MediaExt.valueOf(extension.toUpperCase());
        } catch(Exception e) {
            this.extension = null;
        }
        return this;
    }
    
    /**
     * Gets the resolution of the media as a Dimension.
     * 
     * @return a Dimension with the resolution.
     */
    public Dimension resolution() {
        return this.resolution;
    }
    
    /**
     * Gets the resolution width of the media.
     * 
     * @return an int with the resolution width.
     */
    public int resolutionX() {
        return this.resolution.width;
    }
    
    /**
     * Gets the resolution height of the media.
     * 
     * @return an int with the resolution height.
     */
    public int resolutionY() {
        return this.resolution.height;
    }
    
    /**
     * Sets the resolution property to be the passed width and height.
     * 
     * @param w - an int for the width of the media.
     * @param h - an int for the height of the media.
     * @return this WebMediaFormat instance.
     */
    public WebMediaFormat setResolution(int w, int h) {
        this.resolution = new Dimension(w, h);
        return this;
    }
    
    /**
     * Gets the audio only property of this WebMediaFormat instance.
     * 
     * @return <code>true</code> if the media is just audio; <code>false</code>
     *         otherwise.
     */
    public boolean audioOnly() {
        return this.audioOnly;
    }
    
    /**
     * Sets the audio only property to the passed boolean.
     * 
     * @param audioOnly - a boolean for whether or not the media is just audio.
     * @return this WebMediaFormat instance.
     */
    public WebMediaFormat setAudioOnly(boolean audioOnly) {
        this.audioOnly = audioOnly;
        return this;
    }
    
    @Override
    public String toString() {
        return String.format("%s'%s'%n%20s: %s%n%20s: %s%n%20s: %s%n%20s: %s%n%20s: %s%n%20s: %s%n%20s: %s",
                "---- Web Media Format for ", Objects.toString(title(), "NONE"),
                "ID", Objects.toString(id(), "NONE"),
                "Src", Objects.toString(src(), "NONE"),
                "File Extension", Objects.toString(extension(), "NONE"),
                "Format", Objects.toString(format(), "NONE"),
                "Quality", Objects.toString(resolution(), "NONE"),
                "Playlist Item", (isItem() ? item() : "N/A"),
                "Audio Only", Objects.toString(audioOnly(), "NONE"));
    }
}
