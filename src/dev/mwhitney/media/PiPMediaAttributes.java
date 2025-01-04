package dev.mwhitney.media;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Objects;

import dev.mwhitney.listeners.AttributeUpdateListener;

/**
 * The type of PiPMedia playing within the PiPWindow.
 * 
 * @author mwhitney57
 */
public class PiPMediaAttributes {
    /**
     * The media type.
     * A media type is a common format of media, such as VIDEO, GIF, or IMAGE.
     */
    public static enum TYPE {
        /** Playlist media, such as <code>m3u8</code> files which contain links to numerous media segments. */
        PLAYLIST,
        /** Video media, such as <code>mp4</code> files. */
        VIDEO,
        /** GIF media, which almost exclusively means files ending in <code>gif</code>, but includes animated <code>webp</code> files. */
        GIF,
        /** Image media, such as <code>jpg</code> or <code>png</code> files. Though GIFs are technically images, they have their own {@link TYPE} value. */
        IMAGE,
        /** Audio media, such as <code>mp3</code> or <code>wav</code> files. */
        AUDIO;
    }
    /**
     * The type of media source location.
     * These values define where the media's source is located:
     * Is it a local file? Is it a direct link to media? etc.
     */
    public static enum SRC_TYPE {
        /** A media source that is passed to PiPAA locally and not from the web. */
        LOCAL,
        /** A web media source which PiPAA has a direct link/URL to. */
        WEB_DIRECT,
        /** A web media source which PiPAA does not have a direct link/URL to. For example, YouTube video links are indirect. */
        WEB_INDIRECT;
    }
    /**
     * The platform which the media's source is located at.
     * If the desired platform is not an available option,
     * the <code>GENERIC</code> value is a default/fallback.
     */
    public static enum SRC_PLATFORM {
        GENERIC,
        X
        ("twitter.com", "x.com", "fxtwitter.com", "vxtwitter.com", "fixvx.com", "fixupx.com", "twimg.com"),
        YOUTUBE
        ("youtube.com", "youtu.be"),
        REDDIT
        ("reddit.com", "redd.it");
        
        /** A String array of hosts or domains that are recognized under an instance of SRC_PLATFORM. */
        private String[] hosts;
        
        /**
         * An enum constructor for SRC_PLATFORM that takes one or more String hosts to
         * be associated with itself.
         * 
         * @param s - one or more Strings with hosts linked to this SRC_PLATFORM.
         */
        SRC_PLATFORM(String... s) {
            this.hosts = s;
        }
        
        /**
         * Gets the String array of hosts/domains recognized under the SRC_PLATFORM.
         * 
         * @return a String[] of hosts.
         */
        public String[] hosts() {
            return this.hosts;
        }
        
        /**
         * Check if the passed String host matches a host in this SRC_PLATFORM.
         * 
         * @param h - a String with the host to look for.
         * @return <code>true</code> if the host matches this SRC_PLATFORM;
         *         <code>false</code> otherwise.
         */
        public boolean hostMatches(final String h) {
            for (final String host : hosts()) {
                if (h.equalsIgnoreCase(host))
                    return true;
            }
            return false;
        }
        
        /**
         * Checks if the passed String host matches a host in any SRC_PLATFORM. If a
         * match is found, the SRC_PLATFORM containing the matching host is returned.
         * 
         * @param h - a String with the host to look for.
         * @return the SRC_PLATFORM which the passed host is recognized under, or
         *         <code>null</code> if no match was found.
         */
        public static SRC_PLATFORM hostMatchesAny(String h) {
            for (final SRC_PLATFORM plat : SRC_PLATFORM.values()) {
                if (plat.hostMatches(h))
                    return plat;
            }
            return null;
        }
    }
    
    /** The type/format of media, such as VIDEO. */
    private PiPMediaAttributes.TYPE type;
    /** The location of the media's source, such as LOCAL. */
    private PiPMediaAttributes.SRC_TYPE srcType;
    /** The platform of the media's source, which is only defined when: <code>SRC_TYPE != LOCAL</code> */
    private PiPMediaAttributes.SRC_PLATFORM srcPlatform;
    
    /** The attribute update listener that fires when certain attributes are updated. */
    private AttributeUpdateListener listener;
    
    private WebMediaFormat wmf;
    /** The WebMediaFormat of the media, which is a subset of attributes specifically for web media. */
    private WebMediaFormat.FORMAT webFormat;
    /** The identifier/tag of the media on its respective platform. */
    private String webID;
    /** The modified media source, adjusted for indirect web links. */
    private String webSrc;
    /** The media source's domain/site name, directly retrieved from the URL. */
    private String webSrcDomain;
    
    /** The title of the media. */
    private String title;
    /** The file extension of the media. */
    private MediaExt fileExtension;
    /** The size of the media, both width and height, in pixels. */
    private Dimension size;
    /** The size ratio of the media, which is a ratio between the width and height. */
    private float sizeRatio;
    /** A boolean for whether or not the media is using Advanced GIF Playback mode. */
    private boolean usesAdvGIFPlayback;
    
    /**
     * Creates a blank set of PiPMediaAttributes.
     * This method does not determine or set any of the attributes.
     * These attributes can be automatically determined using the {@link PiPMediaAttributor}.
     */
    public PiPMediaAttributes() {}
    
    /**
     * Creates a copy of the passed PiPMediaAttributes.
     * 
     * @param attr - the PiPMediaAttributes to copy.
     */
    public PiPMediaAttributes(PiPMediaAttributes attr) {
        this.type               =  attr.type;
        this.srcType            =  attr.srcType;
        this.srcPlatform        =  attr.srcPlatform;
        this.listener           =  attr.listener;
        this.wmf                =  attr.wmf;
        this.webFormat          =  attr.webFormat;
        this.webID              = (attr.webID        == null ? null : new String(attr.webID));
        this.webSrc             = (attr.webSrc       == null ? null : new String(attr.webSrc));
        this.webSrcDomain       = (attr.webSrcDomain == null ? null : new String(attr.webSrcDomain));
        this.title              = (attr.title        == null ? null : new String(attr.title));
        this.fileExtension      =  attr.fileExtension;
        this.size               = (attr.size         == null ? null : new Dimension(attr.size));
        this.sizeRatio          =  attr.sizeRatio;
        this.usesAdvGIFPlayback =  attr.usesAdvGIFPlayback;
    }
    
    /**
     * Gets a file name representation of this media, including its web ID if
     * applicable.
     * <p>
     * This specific method ensures that if {@link #isPlaylist()} were
     * to return <code>true</code>, then the returned String will instead have the
     * {@link MediaExt#MP4} extension. Downloading from {@link TYPE#PLAYLIST} media
     * should not return a playlist extension, as that would be redundant and
     * incorrect.
     * <p>
     * Otherwise, this method simply appends the file extension and web
     * ID to the end of the media's title. The web ID is the unique identifier
     * pointing to the web media on its respective platform. If the media is not of
     * the source type <code>WEB_INDIRECT</code>, this method is equivalent to
     * <code>getFileName()</code>. Example:
     * <pre>getFileNameID() --> How to tie a tie - Quick and Easy (9BMhFmNzw-o).mp4</pre>
     * <p>
     * If the media is not <code>WEB_INDIRECT</code> and you still choose to use
     * this function, it would instead look like this, with no web ID:
     * <pre>getFileNameID() --> How to tie a tie - Quick and Easy.mp4</pre>
     * 
     * @return a String with the full file name.
     */
    public String getDownloadFileNameID() {
        // Return with MP4 extension if playlist. Otherwise behave similarly to getFileNameID().
        return this.getFileTitleID()
                + (this.getFileExtension() != null ? "." + (isPlaylist() ? "mp4" : this.getFileExtension().lower()) : "");
    }
    
    /**
     * Gets a file name representation of this media, including its web ID if applicable.
     * This simply appends the file extension and web ID to the end of the media's title.
     * The web ID is the unique identifier pointing to the web media on its respective platform.
     * If the media is not of the source type <code>WEB_INDIRECT</code>, this method is equivalent to <code>getFileName()</code>.
     * Example:
     * <pre>getFileNameID() --> How to tie a tie - Quick and Easy (9BMhFmNzw-o).mp4</pre>
     * <p>
     * If the media is not <code>WEB_INDIRECT</code> and you still choose to use this function, it would instead look like this, with no web ID:
     * <pre>getFileNameID() --> How to tie a tie - Quick and Easy.mp4</pre>
     * 
     * @return a String with the full file name.
     */
    public String getFileNameID() {
        return this.getFileTitleID() + (this.getFileExtension() != null ? "." + this.getFileExtension().lower() : "");
    }
    
    /**
     * Gets a file name representation of this media.
     * This simply appends a <code>.</code> and the file extension to the title String.
     * Example:
     * <ore>getFileName() --> myVideo.mp4</pre>
     * 
     * @return a String with the full file name.
     */
    public String getFileName() {
        return this.getFileTitle() + (this.getFileExtension() != null ? "." + this.getFileExtension().lower() : "");
    }
    
    /**
     * Gets the file title representation of this media, including its web ID if applicable.
     * The file title differs from the standard title or the file name.
     * It is the media title, but formatted to not exceed <code>32</code> characters in length.
     * The file title does NOT include the file extension.
     * To retrieve both as a single String, call <code>getFileName()</code> or <code>getFileNameID()</code>.
     * This method also appends the web ID to the end of the title.
     * The web ID is the unique identifier pointing to the web media on its respective platform.
     * If the media is not of the source type <code>WEB_INDIRECT</code>, this method is equivalent to <code>getFileTitle()</code>.
     * Example:
     * <pre>getFileTitleID() --> How to tie a tie - Quick and Easy (9BMhFmNzw-o)</pre>
     * <p>
     * If the media is not <code>WEB_INDIRECT</code> and you still choose to use this function, it would instead look like this, with no web ID:
     * <pre>getFileNameID() --> How to tie a tie - Quick and Easy</pre>
     * 
     * @return a String with the file title and its web ID, if applicable.
     */
    public String getFileTitleID() {
        return (getFileTitle() + " " + getFileID()).trim() + (getWMF().isItem() ? "_" + getWMF().item() : "");
    }
    
    /**
     * Gets the file title representation of this media.
     * The file title differs from the standard title or the file name.
     * It is the media title, but formatted to not exceed <code>48</code> characters in length.
     * The file title does NOT include the file extension.
     * To retrieve both as a single String, call <code>getFileName()</code> or <code>getFileNameID()</code>.
     * 
     * @return a String with the file title.
     */
    public String getFileTitle() {
        return this.getTitle().substring(0, Math.min(this.getTitle().length(), 48)).trim();
    }
    
    /**
     * Gets the file name representation of this media's ID, if any exists.
     * This call differs from getting the raw web ID, as it concatenates parentheses to the beginning and end.
     * Additionally, however, if there is <b>no web ID</b>, then this method will simply return an empty String.
     * 
     * @return a String with the file representation of the web ID.
     */
    public String getFileID() {
        return (this.getWebID() == null || this.getWebID().length() == 0 ? "" : ("(" + getWebID() + ")"));
    }
    
    /**
     * Checks if the {@link TYPE} is <code>PLAYLIST</code>.
     * 
     * @return <code>true</code> if the type if <code>PLAYLIST</code>; <code>false</code> otherwise.
     */
    public boolean isPlaylist() {
        return (this.type == TYPE.PLAYLIST);
    }
    
    /**
     * Checks if the {@link TYPE} is <code>VIDEO</code>.
     * 
     * @return <code>true</code> if the type if <code>VIDEO</code>; <code>false</code> otherwise.
     */
    public boolean isVideo() {
        return (this.type == TYPE.VIDEO);
    }
    
    /**
     * Checks if the {@link TYPE} is <code>GIF</code>.
     * 
     * @return <code>true</code> if the type if <code>GIF</code>; <code>false</code> otherwise.
     */
    public boolean isGIF() {
        return (this.type == TYPE.GIF);
    }
    
    /**
     * Checks if the {@link TYPE} is <code>IMAGE</code>.
     * 
     * @return <code>true</code> if the type if <code>IMAGE</code>; <code>false</code> otherwise.
     */
    public boolean isImage() {
        return (this.type == TYPE.IMAGE);
    }
    
    /**
     * Checks if the {@link TYPE} is <code>AUDIO</code>.
     * 
     * @return <code>true</code> if the type if <code>AUDIO</code>; <code>false</code> otherwise.
     */
    public boolean isAudio() {
        return (this.type == TYPE.AUDIO);
    }
    
    /**
     * Gets the media TYPE attribute.
     * The TYPE is an enum with application-accepted types of media as the values.
     * <p>
     * Ex: <code>VIDEO</code> or <code>GIF</code>
     * 
     * @return a media TYPE enum value.
     * @see PiPMediaAttributes.TYPE
     */
    public PiPMediaAttributes.TYPE getType() {
        return this.type;
    }
    
    /**
     * Sets the media TYPE attribute.
     * The TYPE is an enum with application-accepted types of media as the values.
     * <p>
     * Ex: <code>VIDEO</code> or <code>GIF</code>
     * 
     * @param type - the new media TYPE enum value.
     * @return a reference to this PiPMediaAttributes instance.
     */
    public PiPMediaAttributes setType(PiPMediaAttributes.TYPE type) {
        this.type = type;
        return this;
    }
    
    /**
     * Gets the file extension, returning <code>null</code> if it is not set.
     * 
     * @return a {@link MediaExt} with the extension, or <code>null</code>.
     */
    public MediaExt getFileExtension() {
        return this.fileExtension;
    }
    
    /**
     * Sets the file extension to be the passed {@link MediaExt}.
     * 
     * @param extension - the {@link MediaExt} to set.
     * @return <code>this</code> PiPMediaAttributes object.
     */
    public PiPMediaAttributes setFileExtension(MediaExt extension) {
        this.fileExtension = extension;
        return this;
    }
    
    /**
     * Checks if the {@link SRC_TYPE} is <code>LOCAL</code>.
     * 
     * @return <code>true</code> if the source type is <code>LOCAL</code>; <code>false</code> otherwise.
     */
    public boolean isLocal() {
        return (this.srcType == SRC_TYPE.LOCAL);
    }
    
    /**
     * Checks if the {@link SRC_TYPE} is <code>WEB_DIRECT</code>.
     * 
     * @return <code>true</code> if the source type is <code>WEB_DIRECT</code>; <code>false</code> otherwise.
     */
    public boolean isWebDirect() {
        return (this.srcType == SRC_TYPE.WEB_DIRECT);
    }
    
    /**
     * Checks if the {@link SRC_TYPE} is <code>WEB_INDIRECT</code>.
     * 
     * @return <code>true</code> if the source type is <code>WEB_INDIRECT</code>; <code>false</code> otherwise.
     */
    public boolean isWebIndirect() {
        return (this.srcType == SRC_TYPE.WEB_INDIRECT);
    }
    
    /**
     * Gets the source type, returning <code>null</code> if it is not set.
     * 
     * @return a {@link SRC_TYPE} with the type, or <code>null</code>.
     */
    public SRC_TYPE getSrcType() {
        return this.srcType;
    }
    
    /**
     * Sets the source type to be the passed {@link SRC_TYPE}.
     * 
     * @param type - the {@link SRC_TYPE} to set.
     * @return <code>this</code> PiPMediaAttributes object.
     */
    public PiPMediaAttributes setSrcType(SRC_TYPE type) {
        this.srcType = type;
        return this;
    }
    
    /**
     * Checks if the {@link SRC_PLATFORM} is <code>GENERIC</code>.
     * 
     * @return <code>true</code> if the platform is <code>GENERIC</code>, <code>false</code> otherwise.
     */
    public boolean isGenericPlatform() {
        return (this.srcPlatform == SRC_PLATFORM.GENERIC);
    }
    
    /**
     * Gets the source platform, returning <code>null</code> if it is not set.
     * 
     * @return a {@link SRC_PLATFORM} with the platform, or <code>null</code>.
     */
    public SRC_PLATFORM getSrcPlatform() {
        return this.srcPlatform;
    }
    
    /**
     * Sets the source platform to be the passed {@link SRC_PLATFORM}.
     * 
     * @param plat - the {@link SRC_PLATFORM} to set.
     * @return <code>this</code> PiPMediaAttributes object.
     */
    public PiPMediaAttributes setSrcPlatform(SRC_PLATFORM plat) {
        this.srcPlatform = plat;
        return this;
    }
    
    /**
     * Checks if there is a title set, meaning it is non-null and has at least one character.
     * 
     * @return <code>true</code> if there is a title; <code>false</code> otherwise.
     */
    public boolean hasTitle() {
        return (this.title != null && this.title.length() > 0);
    }
    
    /**
     * Gets the title, returning <code>null</code> if it is not set.
     * 
     * @return a String with the title, or <code>null</code>.
     */
    public String getTitle() {
        return this.title;
    }
    
    /**
     * Sets the title to be the passed String.
     * 
     * @param title - the String to use as the title.
     * @return <code>this</code> PiPMediaAttributes object.
     */
    public PiPMediaAttributes setTitle(String title) {
        this.title = title;
        if(listener != null) listener.titleUpdated(title);
        return this;
    }
    
    /**
     * Gets the WebMediaFormat pertaining to this set of attributes.
     * If the WebMediaFormat is not set when this method is called,
     * a blank WebMediaFormat is created, set, then returned.
     * This way, any changes made to the returned object will
     * be saved and reflected in the attributes.
     * 
     * @return a WebMediaFormat, which is possibly blank.
     */
    public WebMediaFormat getWMF() {
        if (this.wmf == null)
            setWMF(new WebMediaFormat());
        return this.wmf;
    }
    
    /**
     * Sets the WebMediaFormat pertaining to this set of attributes.
     * 
     * @param wmf - the WebMediaFormat to set.
     * @return <code>this</code> PiPMediaAttributes object.
     */
    public PiPMediaAttributes setWMF(WebMediaFormat wmf) {
        this.wmf = wmf;
        return this;
    }
    
    /**
     * Gets the web format, or <code>null</code> if it is not set.
     * 
     * @return a {@link WebMediaFormat.FORMAT} with the web format, or <code>null</code>.
     */
    public WebMediaFormat.FORMAT getWebFormat() {
        return (this.wmf != null ? this.wmf.format() : null);
    }
    
    /**
     * Gets the web ID, or <code>null</code> if it is not set.
     * 
     * @return a String with the web ID, or <code>null</code>.
     */
    public String getWebID() {
        return (this.wmf != null ? this.wmf.id() : null);
    }
    
    /**
     * Gets the web source, or <code>null</code> if it is not set.
     * 
     * @return a String with the web source, or <code>null</code>.
     */
    public String getWebSrc() {
        return (this.wmf != null ? this.wmf.src() : null);
    }
    
    /**
     * Gets the web source domain, or <code>null</code> if it is not set.
     * 
     * @return a String with the web source domain, or <code>null</code>.
     */
    public String getWebSrcDomain() {
        return this.webSrcDomain;
    }
    
    /**
     * Sets the web source domain to be the passed String.
     * 
     * @param d - the String with the new web source domain.
     * @return <code>this</code> PiPMediaAttributes object.
     */
    public PiPMediaAttributes setWebSrcDomain(String d) {
        this.webSrcDomain = d;
        return this;
    }
    
    /**
     * Gets the size of the media.
     * 
     * @return a {@link Dimension} with the size of the media.
     */
    public Dimension getSize() {
        return this.size;
    }
    
    /**
     * Sets the size of the media.
     * The media's size ratio (Calculated as WIDTH / HEIGHT) is then calculated internally.
     * The size ratio is accessible via the getSizeRatio() method.
     * @param x - the width of the media
     * @param y - the height of the media
     */
    public void setSize(int x, int y) {
        this.size = new Dimension(x, y);
        determineSizeRatio();
    }
    
    /**
     * Gets a scaled version of the current size up to the passed length.
     * Neither the width nor the height will exceed the passed length.
     * <p>
     * Most importantly, this method <b>maintains the aspect ratio</b> of the media.
     * This is an example of the method and what to expect:
     * <pre>
     *              Media = 1920, 1080
     * getScaledSize(720) =  720, 405
     * </pre>
     * 
     * @param maxLen - an int for the maximum value of the width and/or height values.
     * @return a Dimension containing the aspect ratio-scaled size.
     */
    public Dimension getScaledSize(int maxLen) {
        // Scale window based on media size while respecting passed maximum.
        int x = getSize().width, y = getSize().height;
        if(x > y) {
            x = Math.min(x, maxLen);
            y = (int) (x / getSizeRatio());
        } else if (y > x){
            y = Math.min(y, maxLen);
            x = (int) (y * getSizeRatio());
        } else {
            x = Math.min(x, maxLen);
            y = Math.min(y, maxLen);
        }
        return new Dimension((int) x, (int) y);
    }
    
    /**
     * Gets a scaled version of the current size up to the screen size.
     * Neither the width nor the height will exceed that of the screen's.
     * <p>
     * Most importantly, this method <b>maintains the aspect ratio</b> of the media.
     * This is an example of the method and what to expect:
     * <pre>
     *                     Media = 480, 720
     * getScaledFullscreenSize() = 720, 1080
     * </pre>
     * 
     * @param maxLen - an int for the maximum value of the width and/or height values.
     * @return a Dimension containing the aspect ratio-scaled size.
     */
    public Dimension getScaledFullscreenSize() {
        // Get current screen size.
        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        
        // Scale window based on media size while respecting passed maximum.
        int x = this.size.width, y = this.size.height;
        if (x >= y) {
            y = (int) (y * (screen.width / (float) x));
            x = screen.width;
        } else {
            x = (int) (x * (screen.height / (float) y));
            y = screen.height;
        }
        return new Dimension((int) x, (int) y);
    }
    
    /**
     * Determines the size ratio based on the current Dimension size.
     * This method does nothing if that variable is null.
     */
    private void determineSizeRatio() {
        if(this.size == null) return;
        
        // Scale window based on media size while respecting default maximums.
        final float sizeRatio = (float) this.size.width / (float) this.size.height;
        setSizeRatio(sizeRatio);
    }
    
    /**
     * Gets the size ratio of the media.
     * Calculated as (x / y)
     * @return a float with the size ratio.
     */
    public float getSizeRatio() {
        return this.sizeRatio;
    }
    
    /**
     * Sets the size ratio of the media.
     * @param sizeRatio - a float for the size aspect ratio.
     */
    private void setSizeRatio(float sizeRatio) {
        this.sizeRatio = sizeRatio;
    }
    
    /**
     * Checks if the media is set to use advanced GIF playback (when applicable).
     * @return <code>true</code> if enabled; <code>false</code> if disabled.
     */
    public boolean usesAdvancedGIFPlayback() {
        return this.usesAdvGIFPlayback;
    }
    
    /**
     * Sets whether or not this media should use advanced GIF playback (when applicable).
     * @param advanced - ai boolean for if advanced GIF playback should be used.
     */
    public void setUseAdvancedGIFPlayback(boolean advanced) {
        this.usesAdvGIFPlayback = advanced;
    }
    
    /**
     * Sets the AttributeUpdateListener which fires when certain attributes are updated.
     * @param aul - the new AttributeUpdateListener.
     */
    public void setAttributeUpdateListener(AttributeUpdateListener aul) {
        this.listener = aul;
    }
    
    @Override
    public String toString() {
        return String.format("%s'%s'%n%20s: %s%n%20s: %s%n%20s: %s%n%20s: %s%n%20s: %s%n%20s: %s%n%20s: %s%n%s",
                "Media Attributes for ", Objects.toString(this.title, "NONE"),
                "File Extension", Objects.toString(this.fileExtension, "NONE"),
                "Type", Objects.toString(this.type, "NONE"),
                "Source Type", Objects.toString(this.srcType, "NONE"),
                "Source Platform", Objects.toString(this.srcPlatform, "NONE"),
                "Size", Objects.toString(this.size, "NONE"),
                "Size Ratio", Objects.toString(this.sizeRatio, "NONE"),
                "Adv. GIF Playback", this.usesAdvGIFPlayback,
                Objects.toString(this.wmf, ""));
    }
}
