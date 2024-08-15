package dev.mwhitney.main;

import java.awt.Color;

/**
 * Properties for the PiPAA application.
 * 
 * @author mwhitney57
 */
public enum PiPProperty {
    /** The current application theme for coloring. */
    THEME,
    /** The mode under which to play GIF media. */
    GIF_PLAYBACK_MODE,
    /** If Drag and Drop actions should prefer utilizing a link, if available. */
    DND_PREFER_LINK,
    /** A global mute toggle which supersedes a window's mute state and applies to all windows. */
    GLOBAL_MUTED,
    /** The default volume to use for media in newly-created windows. */
    DEFAULT_VOLUME,
    /** The default playback rate to use for media in newly-created windows. */
    DEFAULT_PLAYBACK_RATE,
    /** If PiPAA should prefer to use the required binaries already present on the system, if available. */
    USE_SYS_BINARIES,
    /** If downloaded media files should be deleted after being closed, erasing them from the cache. */
    DISABLE_CACHE,
    /** If incoming media already exists in the cache, ask if it should be overwritten instead of assuming it shouldn't. */
    OVERWRITE_CACHE,
    /** When the application should download web media. */
    DOWNLOAD_WEB_MEDIA,
    /** Attempt to retrieve direct media links from an indirect web link. */
    CONVERT_WEB_INDIRECT,
    /** Trim any fully-transparent pixel edges from image and GIF media sources. */
    TRIM_TRANSPARENCY,
    /** The option selection for how {@link TRIM_TRANSPARENCY} should operate. */
    TRIM_TRANSPARENCY_OPTION,
    /** How frequently the application should check for binary updates. */
    BIN_UPDATE_FREQUENCY,
    /** The last time a binary update check was performed, automatically or manually. */
    BIN_LAST_UPDATE_CHECK,
    /** A pseudo-property which is not stored, but causes all windows to be paused or played. */
    SET_ALL_PAUSED,
    /** A pseudo-property which is not stored, but causes all windows to be muted or unmuted. */
    SET_ALL_MUTED,
    /** A pseudo-property which is not stored, but sets the volume in all windows. */
    SET_ALL_VOLUME,
    /** A pseudo-property which is not stored, but sets the playback rate in all windows. */
    SET_ALL_PLAYBACK_RATE;
    
    /**
     * Gets the default value corresponding to this PiPProperty. If the PiPProperty
     * is a pseudo-property, meaning it has no value, or simply has no default, then
     * <code>null</code> will be returned.
     * 
     * @return a String with the default value, if one exists; <code>null</code>
     *         otherwise.
     */
    public String stock() {
        return switch(this) {
        case THEME                    -> PropDefault.THEME.toString();
        case DOWNLOAD_WEB_MEDIA       -> PropDefault.DOWNLOAD.toString();
        case TRIM_TRANSPARENCY_OPTION -> PropDefault.TRIM.toString();
        case GIF_PLAYBACK_MODE        -> PropDefault.PLAYBACK.toString();
        case OVERWRITE_CACHE          -> PropDefault.OVERWRITE.toString();
        case BIN_UPDATE_FREQUENCY     -> PropDefault.FREQUENCY.toString();
        case DND_PREFER_LINK,
             CONVERT_WEB_INDIRECT     -> "true";
        case TRIM_TRANSPARENCY,
             GLOBAL_MUTED,
             DISABLE_CACHE,
             USE_SYS_BINARIES         -> "false";
        case DEFAULT_VOLUME           -> "50";
        case DEFAULT_PLAYBACK_RATE    -> "1";
        // Do Not Have Stored Properties
        case SET_ALL_MUTED, SET_ALL_PAUSED, SET_ALL_PLAYBACK_RATE,
            SET_ALL_VOLUME, BIN_LAST_UPDATE_CHECK -> null;
        };
    }
    
    /**
     * Default values for certain PiPProperty options.
     * 
     * @author mwhitney57
     */
    public abstract class PropDefault {
        /** The default value for the {@link PiPProperty#THEME} property: {@link THEME_OPTION#LIGHT} */
        public static final THEME_OPTION     THEME     = THEME_OPTION.LIGHT;
        /** The default value for the {@link PiPProperty#DOWNLOAD_WEB_MEDIA} property: {@link DOWNLOAD_OPTION#NORMAL} */
        public static final DOWNLOAD_OPTION  DOWNLOAD  = DOWNLOAD_OPTION.NORMAL;
        /** The default value for the {@link PiPProperty#TRIM_TRANSPARENCY_OPTION} property: {@link TRIM_OPTION#NORMAL} */
        public static final TRIM_OPTION      TRIM      = TRIM_OPTION.NORMAL;
        /** The default value for the {@link PiPProperty#GIF_PLAYBACK_MODE} property: {@link PLAYBACK_OPTION#BASIC} */
        public static final PLAYBACK_OPTION  PLAYBACK  = PLAYBACK_OPTION.BASIC;
        /** The default value for the {@link PiPProperty#OVERWRITE_CACHE} property: {@link OVERWRITE_OPTION#NO} */
        public static final OVERWRITE_OPTION OVERWRITE = OVERWRITE_OPTION.NO;
        /** The default value for the {@link PiPProperty#BIN_UPDATE_FREQUENCY} property: {@link FREQUENCY_OPTION#WEEKLY} */
        public static final FREQUENCY_OPTION FREQUENCY = FREQUENCY_OPTION.WEEKLY;
    }
    
    /**
     * Options within the {@link PiPProperty#THEME} property for various color themes.
     */
    public enum THEME_OPTION implements PiPPropertyEnum<THEME_OPTION>{
        /** The light theme for the application. */
        LIGHT,
        /** The dark theme for the application. */
        DARK,
        /** The pink theme for the application. */
        PINK,
        /** A theme with colors based on the "Subnautica" video game, developed and published by Unknown Worlds Entertainment. */
        SUBNAUTICA;
        
        /**
         * Provides the stock, or default, enum property value.
         * 
         * @return the default PiPPropertyEnum value for this enum.
         */
        @Deprecated(since = "beta20", forRemoval = true)
        public static PiPPropertyEnum<THEME_OPTION> stock() { return LIGHT; }
        
        /**
         * Options within the {@link THEME_OPTION} sub-property which reference specific colors
         * for parts of the user interface.
         */
        public enum COLOR {
            /** The color of backgrounds. */
            BG,
            /** The accent color of backgrounds, often used as a border. */
            BG_ACCENT,
            /** The color of text. */
            TXT,
            /** The color of buttons. */
            BTN,
            /** The color of buttons when they are pressed. */
            BTN_PRESSED,
            /** The color of button borders. */
            BTN_BORDER,
            /** The color of the filled/progressed space in a slider. */
            SLIDER,
            /** The color of the empty space in a slider. */
            SLIDER_EMPTY;
        }
        
        /* LIGHT COLORS */
        public static final Color LIGHT_BG           = new Color(240, 240, 240);
        public static final Color LIGHT_BG_ACCENT    = new Color(210, 210, 210);
        public static final Color LIGHT_TXT          = Color.BLACK;
        public static final Color LIGHT_BTN          = new Color(0, 120, 215);
        public static final Color LIGHT_BTN_PRESSED  = new Color(0, 84, 150);
        public static final Color LIGHT_BTN_BORDER   = new Color(0, 60, 107);
        public static final Color LIGHT_SLIDER       = LIGHT_BTN.brighter();
        public static final Color LIGHT_SLIDER_EMPTY = LIGHT_BTN_BORDER.darker().darker().darker();
        /* DARK COLORS */
        public static final Color DARK_BG           = Color.DARK_GRAY;
        public static final Color DARK_BG_ACCENT    = new Color(50, 50, 50);
        public static final Color DARK_TXT          = Color.LIGHT_GRAY;
        public static final Color DARK_BTN          = new Color(35, 35, 35);
        public static final Color DARK_BTN_PRESSED  = DARK_BTN.darker();
        public static final Color DARK_BTN_BORDER   = Color.BLACK;
        public static final Color DARK_SLIDER       = DARK_BG_ACCENT;
        public static final Color DARK_SLIDER_EMPTY = Color.BLACK;
        /* PINK COLORS */
        public static final Color PINK_BG           = Color.PINK;
        public static final Color PINK_BG_ACCENT    = PINK_BG.darker();
        public static final Color PINK_TXT          = Color.WHITE;
        public static final Color PINK_BTN          = new Color(172, 193, 138);
        public static final Color PINK_BTN_PRESSED  = PINK_BTN.darker();
        public static final Color PINK_BTN_BORDER   = PINK_BTN_PRESSED.darker();
        public static final Color PINK_SLIDER       = PINK_BTN_PRESSED;
        public static final Color PINK_SLIDER_EMPTY = Color.WHITE;
        /* SUBNAUTICA COLORS */
        public static final Color SUBNAUTICA_BG           = new Color(247,135,59);
        public static final Color SUBNAUTICA_BG_ACCENT    = SUBNAUTICA_BG.darker();
        public static final Color SUBNAUTICA_TXT          = Color.WHITE;
        public static final Color SUBNAUTICA_BTN          = new Color(11, 153, 255);
        public static final Color SUBNAUTICA_BTN_PRESSED  = new Color(9, 122, 204);
        public static final Color SUBNAUTICA_BTN_BORDER   = new Color(8, 107, 179);
        public static final Color SUBNAUTICA_SLIDER       = SUBNAUTICA_BTN_PRESSED;
        public static final Color SUBNAUTICA_SLIDER_EMPTY = SUBNAUTICA_BTN_BORDER.darker().darker().darker();
        
        /**
         * Gets {@link THEME_OPTION}-specific Color for the specific component or element of
         * the UI specified by the passed {@link COLOR} value.
         * @param c - the {@link COLOR} value which determines the specific theme color
         *          to retrieve.
         * @return the theme Color relating to the specified component or element via
         *         the passed {@link COLOR}.
         */
        public Color color(final COLOR c) {
            return switch (this) {
            case LIGHT -> switch (c) {
                case BG           -> LIGHT_BG;
                case BG_ACCENT    -> LIGHT_BG_ACCENT;
                case TXT          -> LIGHT_TXT;
                case BTN          -> LIGHT_BTN;
                case BTN_BORDER   -> LIGHT_BTN_BORDER;
                case BTN_PRESSED  -> LIGHT_BTN_PRESSED;
                case SLIDER       -> LIGHT_SLIDER;
                case SLIDER_EMPTY -> LIGHT_SLIDER_EMPTY;
                };
            case DARK -> switch (c) {
                case BG           -> DARK_BG;
                case BG_ACCENT    -> DARK_BG_ACCENT;
                case TXT          -> DARK_TXT;
                case BTN          -> DARK_BTN;
                case BTN_BORDER   -> DARK_BTN_BORDER;
                case BTN_PRESSED  -> DARK_BTN_PRESSED;
                case SLIDER       -> DARK_SLIDER;
                case SLIDER_EMPTY -> DARK_SLIDER_EMPTY;
                };
            case PINK -> switch (c) {
                case BG           -> PINK_BG;
                case BG_ACCENT    -> PINK_BG_ACCENT;
                case TXT          -> PINK_TXT;
                case BTN          -> PINK_BTN;
                case BTN_BORDER   -> PINK_BTN_BORDER;
                case BTN_PRESSED  -> PINK_BTN_PRESSED;
                case SLIDER       -> PINK_SLIDER;
                case SLIDER_EMPTY -> PINK_SLIDER_EMPTY;
                };
            case SUBNAUTICA -> switch(c) {
                case BG           -> SUBNAUTICA_BG;
                case BG_ACCENT    -> SUBNAUTICA_BG_ACCENT;
                case TXT          -> SUBNAUTICA_TXT;
                case BTN          -> SUBNAUTICA_BTN;
                case BTN_BORDER   -> SUBNAUTICA_BTN_BORDER;
                case BTN_PRESSED  -> SUBNAUTICA_BTN_PRESSED;
                case SLIDER       -> SUBNAUTICA_SLIDER;
                case SLIDER_EMPTY -> SUBNAUTICA_SLIDER_EMPTY;
                };
            };
        }
    }
    /**
     * Options to go with {@link PiPProperty#DOWNLOAD_WEB_MEDIA} which determine
     * when web media should be downloaded.
     */
    public enum DOWNLOAD_OPTION implements PiPPropertyEnum<DOWNLOAD_OPTION> {
        /** Never even attempt to download web media, even if necessary for playback. Not recommended in most situations. */
        NEVER,
        /** Download web media in normal situations, especially when necessary for playback. */
        NORMAL,
        /** Always download web media, even when it's not necessary for playback. */
        ALWAYS;
        @Override
        public String description() {
            return switch (this) {
            case NEVER  -> "Never even attempt to download web media, even if necessary for playback. Not recommended in most situations.";
            case NORMAL -> "Download web media in normal situations, especially when necessary for playback.";
            case ALWAYS -> "Always download web media, even when it's not necessary for playback.";
            };
        }
    }
    /**
     * Options to go with {@link PiPProperty#TRIM_TRANSPARENCY} which determine how
     * the trimming operation should operate.
     */
    public enum TRIM_OPTION implements PiPPropertyEnum<TRIM_OPTION> {
        /** Trim nearly-invisible pixels and fully-transparent pixels. */
        NORMAL,
        /** Only trim fully-transparent pixels. */
        STRICT,
        /** Force the transparency trimming operation, skipping the pixels check entirely. */
        FORCE;
        
        /**
         * Provides the stock, or default, enum property value.
         * 
         * @return the default PiPPropertyEnum value for this enum.
         */
        @Deprecated(since = "beta20", forRemoval = true)
        public static PiPPropertyEnum<TRIM_OPTION> stock() { return NORMAL; }
        @Override
        public String description() {
            return switch (this) {
            case NORMAL -> "Trim nearly-invisible pixels and fully-transparent pixels.";
            case STRICT -> "Only trim fully-transparent pixels.";
            case FORCE  -> "Force the transparency trimming operation whenever possible. Uses the \"NORMAL\" mode parameters.";
            };
        }
    }
    /**
     * Options within the {@link PiPProperty#GIF_PLAYBACK_MODE} property.
     */
    public enum PLAYBACK_OPTION implements PiPPropertyEnum<PLAYBACK_OPTION>{
        /** The Basic GIF Playback mode which uses an image viewer component. */
        BASIC,
        /** The Advanced GIF Playback mode which downloads and converts GIFs to videos. */
        ADVANCED;
        
        /**
         * Provides the stock, or default, enum property value.
         * 
         * @return the default PiPPropertyEnum value for this enum.
         */
        @Deprecated(since = "beta20", forRemoval = true)
        public static PiPPropertyEnum<PLAYBACK_OPTION> stock() { return BASIC; }
        @Override
        public String description() {
            return switch (this) {
            case BASIC    -> "Basic playback uses an image viewer component instead of VLC to view GIFs. Smooth and fast, but has different controls (i.e. no play/pause).";
            case ADVANCED -> "Advanced playback uses the video player by first converting GIF media to a video before playing. Not as smooth, but has video controls.";
            };
        }
    }
    /**
     * Options within the {@link PiPProperty#OVERWRITE_CACHE} property.
     */
    public enum OVERWRITE_OPTION implements PiPPropertyEnum<OVERWRITE_OPTION> {
        /** The Ask option for if to overwrite the cache when there's a conflict. */
        ASK,
        /** The Yes option for if to overwrite the cache when there's a conflict. */
        YES,
        /** The No option for if to overwrite the cache when there's a conflict. */
        NO;
        
        /**
         * Provides the stock, or default, enum property value.
         * 
         * @return the default PiPPropertyEnum value for this enum.
         */
        @Deprecated(since = "beta20", forRemoval = true)
        public static PiPPropertyEnum<OVERWRITE_OPTION> stock() { return NO; }
        @Override
        public String description() {
            return switch (this) {
            case ASK -> "Ask if the cached media should be overwritten whenever there is a conflict.";
            case YES -> "Overwrite the cached media every time without asking.";
            case NO  -> "Load the cached media and do not overwrite anything.";
            };
        }
    }
    /**
     * Options within the {@link PiPProperty#BIN_UPDATE_FREQUENCY} property.
     */
    public enum FREQUENCY_OPTION implements PiPPropertyEnum<FREQUENCY_OPTION> {
        /** Always attempt to automatically update the binaries during application launch. */
        ALWAYS,
        /** Only attempt to automatically update the binaries once per day. */
        DAILY,
        /** Only attempt to automatically update the binaries once per week. */
        WEEKLY,
        /** Only attempt to automatically update the binaries once per month. */
        MONTHLY,
        /** Never attempt automatic updates of the binaries. Not recommended for most users. */
        NEVER;

        /**
         * Provides the stock, or default, enum property value.
         * 
         * @return the default PiPPropertyEnum value for this enum.
         */
        @Deprecated(since = "beta20", forRemoval = true)
        public static PiPPropertyEnum<FREQUENCY_OPTION> stock() { return WEEKLY; }
        @Override
        public String description() {
            return switch (this) {
            case NEVER   -> "Never attempt automatic updates of the binaries. Not recommended for most users.";
            case ALWAYS  -> "Always attempt to automatically update the binaries during application launch.";
            case DAILY   -> "Only attempt to automatically update the binaries once per day.";
            case WEEKLY  -> "Only attempt to automatically update the binaries once per week.";
            case MONTHLY -> "Only attempt to automatically update the binaries once per month.";
            };
        }
    }
}
