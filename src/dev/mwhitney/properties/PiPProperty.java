package dev.mwhitney.properties;

import java.awt.Color;

import dev.mwhitney.resources.AppRes;

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
    /** The quality of the images in the Swing component viewer when displayed at different resolutions. */
    IMG_SCALING_QUALITY,
    /** If Drag and Drop actions should prefer utilizing a link, if available. */
    DND_PREFER_LINK,
    /** A mode which only allows a single window to play at any given time. */
    SINGLE_PLAY_MODE,
    /** A global mute toggle which supersedes a window's mute state and applies to all windows. */
    GLOBAL_MUTED,
    /** The default volume to use for media in newly-created windows. */
    DEFAULT_VOLUME,
    /** The default playback rate to use for media in newly-created windows. */
    DEFAULT_PLAYBACK_RATE,
    /** If PiPAA should prefer to use the required VLC build already present on the system, if available. */
    USE_SYS_VLC,
    /** If PiPAA should prefer to use the required binaries already present on the system, if available. */
    USE_SYS_BINARIES,
    /** If PiPAA should attempt to use hardware decoding and configures Direct3D11 VLC options. */
    USE_HW_DECODING,
    /** If PiPAA should attempt to use NVIDIA's RTX Video Super Resolution. Requires {@link #USE_HW_DECODING}. */
    USE_SUPER_RES,
    /** If downloaded media files should be deleted after being closed, erasing them from the cache. */
    DISABLE_CACHE,
    /** If incoming media already exists in the cache, ask if it should be overwritten instead of assuming it shouldn't. */
    OVERWRITE_CACHE,
    /** When the application should download web media. */
    DOWNLOAD_WEB_MEDIA,
    /** Attempt to retrieve direct media links from an indirect web link. */
    CONVERT_WEB_INDIRECT,
    /** Require confirmation before closing all windows via shortcut execution. */
    CONFIRM_CLOSE_ALL,
    /** Open an empty window after application initialization has completed. */
    OPEN_WINDOW_AT_LAUNCH,
    /** Allows the window background to become fully transparent so that clicks can pass-through. */
    TRANSPARENT_PASS,
    /** Trim any fully-transparent pixel edges from image and GIF media sources. */
    TRIM_TRANSPARENCY,
    /** The option selection for how {@link TRIM_TRANSPARENCY} should operate. */
    TRIM_TRANSPARENCY_OPTION,
    /** How frequently the application should check for updates. */
    APP_UPDATE_FREQUENCY,
    /** What type of updates should be checked for? Regular releases, beta versions, etc. */
    APP_UPDATE_TYPE,
    /** Should the application "force" an update prompt on the user, even if the latest version is the same? */
    APP_UPDATE_FORCE,
    /** The last time an application update check was performed, automatically or manually. */
    APP_LAST_UPDATE_CHECK,
    /** The build that the application was last updating from. Only present during an update cycle restart. */
    APP_UPDATING_FROM,
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
        case IMG_SCALING_QUALITY      -> PropDefault.SCALING.toString();
        case OVERWRITE_CACHE          -> PropDefault.OVERWRITE.toString();
        case APP_UPDATE_TYPE          -> PropDefault.TYPE.toString();
        case APP_UPDATE_FREQUENCY     -> PropDefault.FREQUENCY_APP.toString();
        case BIN_UPDATE_FREQUENCY     -> PropDefault.FREQUENCY_BIN.toString();
        case DND_PREFER_LINK,
             OPEN_WINDOW_AT_LAUNCH,
             TRANSPARENT_PASS,
             CONFIRM_CLOSE_ALL,
             CONVERT_WEB_INDIRECT     -> "true";
        case TRIM_TRANSPARENCY,
             GLOBAL_MUTED,
             SINGLE_PLAY_MODE,
             DISABLE_CACHE,
             APP_UPDATE_FORCE,
             USE_SYS_VLC,
             USE_SYS_BINARIES,
             USE_HW_DECODING,
             USE_SUPER_RES            -> "false";
        case DEFAULT_VOLUME           -> "50";
        case DEFAULT_PLAYBACK_RATE    -> "1";
        // Do Not Have Stored Properties
        case SET_ALL_MUTED, SET_ALL_PAUSED, SET_ALL_PLAYBACK_RATE,
            SET_ALL_VOLUME, APP_LAST_UPDATE_CHECK, APP_UPDATING_FROM, BIN_LAST_UPDATE_CHECK -> null;
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
        /** The default value for the {@link PiPProperty#IMG_SCALING_QUALITY} property: {@link SCALING_OPTION#SMART} */
        public static final SCALING_OPTION   SCALING   = SCALING_OPTION.SMART;
        /** The default value for the {@link PiPProperty#OVERWRITE_CACHE} property: {@link OVERWRITE_OPTION#NO} */
        public static final OVERWRITE_OPTION OVERWRITE = OVERWRITE_OPTION.NO;
        /** The default value for the {@link PiPProperty#APP_UPDATE_FREQUENCY} property: {@link FREQUENCY_OPTION#WEEKLY} */
        public static final FREQUENCY_OPTION FREQUENCY_APP = FREQUENCY_OPTION.WEEKLY;
        /** The default value for the {@link PiPProperty#BIN_UPDATE_FREQUENCY} property: {@link FREQUENCY_OPTION#DAILY} */
        public static final FREQUENCY_OPTION FREQUENCY_BIN = FREQUENCY_OPTION.DAILY;
        /** The default value for the {@link PiPProperty#APP_UPDATE_TYPE} property: {@link TYPE_OPTION#RELEASE} */
        public static final TYPE_OPTION TYPE = TYPE_OPTION.RELEASE;
    }
    
    /**
     * Options within the {@link PiPProperty#THEME} property for various color themes.
     */
    public enum THEME_OPTION implements PiPPropertyEnum<THEME_OPTION>{
        /** The light theme for the application. */
        LIGHT,
        /** The dark theme for the application. */
        DARK,
        /** A theme with colors resembling PiPAA's icon. */
        PIPAA,
        /** The pink theme for the application. */
        PINK,
        /** A theme with colors based on the "Subnautica" video game, developed and published by Unknown Worlds Entertainment. */
        SUBNAUTICA,
        /** A blue-heavy ocean theme for the application. */
        OCEAN,
        /** A red-heavy fire theme for the application. */
        FIRE;
        
        /**
         * Checks if this theme uses inverted icons.
         * 
         * @return <code>true</code> if this theme uses inverted icons;
         *         <code>false</code> if this theme uses normal icons.
         */
        public boolean usesInvertedIcons() {
            return switch (this) {
            case LIGHT -> false;
            case SUBNAUTICA, PINK, DARK, PIPAA, OCEAN, FIRE -> true;
            };
        }
        
        @Override
        public String label() {
            return switch(this) {
            case LIGHT      -> "☀️ Light";
            case DARK       -> "🌑 Dark";
            case PIPAA      -> "🖼️ PiPAA";
            case PINK       -> "🌸 Pink";
            case SUBNAUTICA -> "🚀 Subnautica";
            case OCEAN      -> "🌊 Ocean";
            case FIRE       -> "🔥 Fire";
            };
        }

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
            /** The color of buttons when hovered over or highlighted. */
            BTN_HOVER,
            /** The color of buttons when they are pressed. */
            BTN_PRESSED,
            /** The color of button borders. */
            BTN_BORDER,
            /** The color of the button text. */
            BTN_TXT,
            /** The color of the filled/progressed space in a slider. */
            SLIDER,
            /** The color of the knob or thumb in a slider. */
            SLIDER_KNOB,
            /** The color of the empty space in a slider. */
            SLIDER_EMPTY;
        }
        
        /* LIGHT COLORS */
        public static final Color LIGHT_BG           = AppRes.COLOR_OFF_WHITE;
        public static final Color LIGHT_BG_ACCENT    = new Color(210, 210, 210);
        public static final Color LIGHT_TXT          = AppRes.COLOR_OFF_BLACK;
        public static final Color LIGHT_BTN          = new Color(0, 120, 215);
        public static final Color LIGHT_BTN_HOVER    = new Color(0, 140, 255);
        public static final Color LIGHT_BTN_PRESSED  = new Color(0, 84, 150);
        public static final Color LIGHT_BTN_BORDER   = new Color(0, 60, 107);
        public static final Color LIGHT_BTN_TXT      = AppRes.COLOR_NEAR_WHITE;
        public static final Color LIGHT_SLIDER       = LIGHT_BTN.brighter();
        public static final Color LIGHT_SLIDER_KNOB  = LIGHT_BTN;
        public static final Color LIGHT_SLIDER_EMPTY = LIGHT_BTN_BORDER.darker().darker().darker();
        /* DARK COLORS */
        public static final Color DARK_BG           = Color.DARK_GRAY;
        public static final Color DARK_BG_ACCENT    = new Color(50, 50, 50);
        public static final Color DARK_TXT          = Color.LIGHT_GRAY;
        public static final Color DARK_BTN          = new Color(35, 35, 35);
        public static final Color DARK_BTN_HOVER    = DARK_BG_ACCENT;
        public static final Color DARK_BTN_PRESSED  = DARK_BTN.darker();
        public static final Color DARK_BTN_BORDER   = AppRes.COLOR_NEAR_BLACK;
        public static final Color DARK_BTN_TXT      = AppRes.COLOR_NEAR_WHITE;
        public static final Color DARK_SLIDER       = DARK_BG_ACCENT;
        public static final Color DARK_SLIDER_KNOB  = DARK_BTN;
        public static final Color DARK_SLIDER_EMPTY = Color.BLACK;
        /* PIPAA COLORS */
        public static final Color PIPAA_BG           = new Color(0, 34, 53);
        public static final Color PIPAA_BG_ACCENT    = new Color(0, 24, 38);
        public static final Color PIPAA_TXT          = AppRes.COLOR_OFF_WHITE;
        public static final Color PIPAA_BTN          = new Color(0, 122, 188);
        public static final Color PIPAA_BTN_HOVER    = PIPAA_BTN.brighter();
        public static final Color PIPAA_BTN_PRESSED  = new Color(0, 52, 84);
        public static final Color PIPAA_BTN_BORDER   = new Color(0, 76, 117);
        public static final Color PIPAA_BTN_TXT      = AppRes.COLOR_NEAR_WHITE;
        public static final Color PIPAA_SLIDER       = new Color(0, 69, 107);
        public static final Color PIPAA_SLIDER_KNOB  = PIPAA_BTN;
        public static final Color PIPAA_SLIDER_EMPTY = new Color(0, 5, 8);
        /* PINK COLORS */
        public static final Color PINK_BG           = Color.PINK;
        public static final Color PINK_BG_ACCENT    = PINK_BG.darker();
        public static final Color PINK_TXT          = Color.WHITE;
        public static final Color PINK_BTN          = new Color(172, 193, 138);
        public static final Color PINK_BTN_HOVER    = new Color(196, 211, 171);
        public static final Color PINK_BTN_PRESSED  = PINK_BTN.darker();
        public static final Color PINK_BTN_BORDER   = PINK_BTN_PRESSED.darker();
        public static final Color PINK_BTN_TXT      = Color.WHITE;
        public static final Color PINK_SLIDER       = PINK_BTN_PRESSED;
        public static final Color PINK_SLIDER_KNOB  = PINK_BTN;
        public static final Color PINK_SLIDER_EMPTY = PINK_BTN_BORDER.darker().darker();
        /* SUBNAUTICA COLORS */
        public static final Color SUBNAUTICA_BG           = new Color(247, 135, 59);
        public static final Color SUBNAUTICA_BG_ACCENT    = SUBNAUTICA_BG.darker();
        public static final Color SUBNAUTICA_TXT          = Color.WHITE;
        public static final Color SUBNAUTICA_BTN          = new Color(11, 153, 255);
        public static final Color SUBNAUTICA_BTN_HOVER    = new Color(51, 170, 255);
        public static final Color SUBNAUTICA_BTN_PRESSED  = new Color(9, 122, 204);
        public static final Color SUBNAUTICA_BTN_BORDER   = new Color(8, 107, 179);
        public static final Color SUBNAUTICA_BTN_TXT      = Color.WHITE;
        public static final Color SUBNAUTICA_SLIDER       = SUBNAUTICA_BTN_PRESSED;
        public static final Color SUBNAUTICA_SLIDER_KNOB  = SUBNAUTICA_BTN;
        public static final Color SUBNAUTICA_SLIDER_EMPTY = SUBNAUTICA_BTN_BORDER.darker().darker().darker();
        /* OCEAN COLORS */
        public static final Color OCEAN_BG           = new Color(0, 64, 128);
        public static final Color OCEAN_BG_ACCENT    = OCEAN_BG.darker();
        public static final Color OCEAN_TXT          = AppRes.COLOR_NEAR_WHITE;
        public static final Color OCEAN_BTN          = new Color(0, 26, 51);
        public static final Color OCEAN_BTN_HOVER    = new Color(0, 40, 77);
        public static final Color OCEAN_BTN_PRESSED  = OCEAN_BTN.darker();
        public static final Color OCEAN_BTN_BORDER   = OCEAN_BTN_PRESSED.darker();
        public static final Color OCEAN_BTN_TXT      = AppRes.COLOR_NEAR_WHITE;
        public static final Color OCEAN_SLIDER       = new Color(0, 51, 102).darker();
        public static final Color OCEAN_SLIDER_KNOB  = OCEAN_SLIDER.darker();
        public static final Color OCEAN_SLIDER_EMPTY = OCEAN_SLIDER_KNOB.darker().darker().darker();
        /* FIRE COLORS */
        public static final Color FIRE_BG           = new Color(221, 22, 0);
        public static final Color FIRE_BG_ACCENT    = FIRE_BG.darker();
        public static final Color FIRE_TXT          = AppRes.COLOR_NEAR_WHITE;
        public static final Color FIRE_BTN          = new Color(127, 0, 0);
        public static final Color FIRE_BTN_HOVER    = new Color(153, 0, 0);
        public static final Color FIRE_BTN_PRESSED  = FIRE_BTN.darker();
        public static final Color FIRE_BTN_BORDER   = FIRE_BTN_PRESSED.darker();
        public static final Color FIRE_BTN_TXT      = AppRes.COLOR_NEAR_WHITE;
        public static final Color FIRE_SLIDER       = new Color (255, 47, 24).darker();
        public static final Color FIRE_SLIDER_KNOB  = FIRE_SLIDER.darker();
        public static final Color FIRE_SLIDER_EMPTY = FIRE_SLIDER_KNOB.darker().darker().darker();
        
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
                case BTN_HOVER    -> LIGHT_BTN_HOVER;
                case BTN_PRESSED  -> LIGHT_BTN_PRESSED;
                case BTN_TXT      -> LIGHT_BTN_TXT;
                case SLIDER       -> LIGHT_SLIDER;
                case SLIDER_KNOB  -> LIGHT_SLIDER_KNOB;
                case SLIDER_EMPTY -> LIGHT_SLIDER_EMPTY;
                };
            case DARK -> switch (c) {
                case BG           -> DARK_BG;
                case BG_ACCENT    -> DARK_BG_ACCENT;
                case TXT          -> DARK_TXT;
                case BTN          -> DARK_BTN;
                case BTN_BORDER   -> DARK_BTN_BORDER;
                case BTN_HOVER    -> DARK_BTN_HOVER;
                case BTN_PRESSED  -> DARK_BTN_PRESSED;
                case BTN_TXT      -> DARK_BTN_TXT;
                case SLIDER       -> DARK_SLIDER;
                case SLIDER_KNOB  -> DARK_SLIDER_KNOB;
                case SLIDER_EMPTY -> DARK_SLIDER_EMPTY;
                };
            case PIPAA -> switch (c) {
                case BG           -> PIPAA_BG;
                case BG_ACCENT    -> PIPAA_BG_ACCENT;
                case TXT          -> PIPAA_TXT;
                case BTN          -> PIPAA_BTN;
                case BTN_BORDER   -> PIPAA_BTN_BORDER;
                case BTN_HOVER    -> PIPAA_BTN_HOVER;
                case BTN_PRESSED  -> PIPAA_BTN_PRESSED;
                case BTN_TXT      -> PIPAA_BTN_TXT;
                case SLIDER       -> PIPAA_SLIDER;
                case SLIDER_KNOB  -> PIPAA_SLIDER_KNOB;
                case SLIDER_EMPTY -> PIPAA_SLIDER_EMPTY;
                };
            case PINK -> switch (c) {
                case BG           -> PINK_BG;
                case BG_ACCENT    -> PINK_BG_ACCENT;
                case TXT          -> PINK_TXT;
                case BTN          -> PINK_BTN;
                case BTN_BORDER   -> PINK_BTN_BORDER;
                case BTN_HOVER    -> PINK_BTN_HOVER;
                case BTN_PRESSED  -> PINK_BTN_PRESSED;
                case BTN_TXT      -> PINK_BTN_TXT;
                case SLIDER       -> PINK_SLIDER;
                case SLIDER_KNOB  -> PINK_SLIDER_KNOB;
                case SLIDER_EMPTY -> PINK_SLIDER_EMPTY;
                };
            case SUBNAUTICA -> switch(c) {
                case BG           -> SUBNAUTICA_BG;
                case BG_ACCENT    -> SUBNAUTICA_BG_ACCENT;
                case TXT          -> SUBNAUTICA_TXT;
                case BTN          -> SUBNAUTICA_BTN;
                case BTN_BORDER   -> SUBNAUTICA_BTN_BORDER;
                case BTN_HOVER    -> SUBNAUTICA_BTN_HOVER;
                case BTN_PRESSED  -> SUBNAUTICA_BTN_PRESSED;
                case BTN_TXT      -> SUBNAUTICA_BTN_TXT;
                case SLIDER       -> SUBNAUTICA_SLIDER;
                case SLIDER_KNOB  -> SUBNAUTICA_SLIDER_KNOB;
                case SLIDER_EMPTY -> SUBNAUTICA_SLIDER_EMPTY;
                };
            case OCEAN -> switch(c) {
                case BG           -> OCEAN_BG;
                case BG_ACCENT    -> OCEAN_BG_ACCENT;
                case TXT          -> OCEAN_TXT;
                case BTN          -> OCEAN_BTN;
                case BTN_BORDER   -> OCEAN_BTN_BORDER;
                case BTN_HOVER    -> OCEAN_BTN_HOVER;
                case BTN_PRESSED  -> OCEAN_BTN_PRESSED;
                case BTN_TXT      -> OCEAN_BTN_TXT;
                case SLIDER       -> OCEAN_SLIDER;
                case SLIDER_KNOB  -> OCEAN_SLIDER_KNOB;
                case SLIDER_EMPTY -> OCEAN_SLIDER_EMPTY;
                };
            case FIRE -> switch(c) {
                case BG           -> FIRE_BG;
                case BG_ACCENT    -> FIRE_BG_ACCENT;
                case TXT          -> FIRE_TXT;
                case BTN          -> FIRE_BTN;
                case BTN_BORDER   -> FIRE_BTN_BORDER;
                case BTN_HOVER    -> FIRE_BTN_HOVER;
                case BTN_PRESSED  -> FIRE_BTN_PRESSED;
                case BTN_TXT      -> FIRE_BTN_TXT;
                case SLIDER       -> FIRE_SLIDER;
                case SLIDER_KNOB  -> FIRE_SLIDER_KNOB;
                case SLIDER_EMPTY -> FIRE_SLIDER_EMPTY;
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
        public String label() {
            return switch (this) {
            case NEVER  -> "❌ Never";
            case NORMAL -> "💾 Normal";
            case ALWAYS -> "✔️ Always";
            };
        }
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
        
        @Override
        public String label() {
            return switch (this) {
            case NORMAL -> "Normal";
            case STRICT -> "Strict";
            case FORCE  -> "Force";
            };
        }
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
    public enum PLAYBACK_OPTION implements PiPPropertyEnum<PLAYBACK_OPTION> {
        /** The Basic GIF Playback mode which uses an image viewer component. */
        BASIC,
        /** The Advanced GIF Playback mode which downloads and converts GIFs to videos. */
        ADVANCED;
        
        @Override
        public String label() {
            return switch (this) {
            case BASIC    -> "🍎 Basic";
            case ADVANCED -> "🚀 Advanced";
            };
        }
        @Override
        public String description() {
            return switch (this) {
            case BASIC    -> "Basic playback uses an image viewer component instead of VLC to view GIFs. Smooth and fast, but has different controls (i.e. no play/pause).";
            case ADVANCED -> "Advanced playback uses the video player by first converting GIF media to a video before playing. Not as smooth, but has video controls.";
            };
        }
    }
    /**
     * Options to go with {@link PiPProperty#IMG_SCALING_QUALITY} which determines
     * scaling quality.
     */
    public enum SCALING_OPTION implements PiPPropertyEnum<SCALING_OPTION> {
        /** The quality mode forces a higher quality scaling algorithm at all times, even when illogical or unnecessary, at the cost of performance. */
        QUALITY,
        /** The smart mode prefers {@link #QUALITY} at all times, only switching to {@link #FAST} momentarily during zoom or window resize operations. */
        SMART,
        /** The fast mode forces the standard, faster, but lower quality scaling algorithm at all times, heavily sacrificing quality. */
        FAST;
        
        @Override
        public String label() {
            return switch (this) {
            case QUALITY -> "⭐ Force Quality";
            case SMART   -> "💡 Smart";
            case FAST    -> " ⚡  Fast";
            };
        }
        @Override
        public String description() {
            return switch (this) {
            case QUALITY -> "Forces a higher quality scaling algorithm at all times, even when unnecessary, at the cost of performance. Not recommended for hi-res media.";
            case SMART   -> "Intelligently switches between the quality and fast algorithms to maximize quality and maintain performance. Recommended.";
            case FAST    -> "Forces the faster, lower quality scaling algorithm at all times. Images may look worse, especially at small window sizes.";
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
        
        @Override
        public String label() {
            return switch (this) {
            case ASK -> "❔ Ask";
            case YES -> "✔️ Yes";
            case NO  -> "❌ No";
            };
        }
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
     * Options within the {@link PiPProperty#APP_UPDATE_FREQUENCY} and {@link PiPProperty#BIN_UPDATE_FREQUENCY} properties.
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
        
        @Override
        public String label() {
            return switch (this) {
            case ALWAYS  -> "✔️ Always";
            case DAILY   -> "☀️ Daily";
            case WEEKLY  -> "📏 Weekly";
            case MONTHLY -> "📅 Monthly";
            case NEVER   -> "❌ Never";
            };
        }
        @Override
        public String description() {
            return switch (this) {
            case ALWAYS  -> "Always attempt to automatically update during application launch.";
            case DAILY   -> "Only attempt to automatically update once per day.";
            case WEEKLY  -> "Only attempt to automatically update once per week.";
            case MONTHLY -> "Only attempt to automatically update once per month.";
            case NEVER   -> "Never attempt automatic updates. Not recommended for most users.";
            };
        }
    }
    /**
     * Options within the {@link PiPProperty#APP_UPDATE_TYPE} property. Release types such as {@link #RELEASE}, {@link #BETA}, or {@link #SNAPSHOT}.
     */
    public enum TYPE_OPTION implements PiPPropertyEnum<TYPE_OPTION> {
        /** Standard releases. Most stable. */
        RELEASE(0),
        /** Beta versions, which may contain glaring bugs. */
        BETA(1),
        /** Snapshot versions, which are undergoing testing. May contain bugs, crashes, and incomplete features. */
        SNAPSHOT(2);
        
        /** An int which indicates stability relative to other release types. Lower is more stable. */
        private int stability;
        
        TYPE_OPTION(int stability) {
            this.stability = stability;
        }
        
        /**
         * Gets this release type's stability value. Lower values indicate greater
         * stability.
         * 
         * @return an int with the stability value.
         */
        public int stability() {
            return this.stability;
        }
        
        /**
         * Checks if this TYPE_OPTION is considered more stable than the passed
         * instance.
         * 
         * @param type - another TYPE_OPTION to compare stability against.
         * @return <code>true</code> if this TYPE_OPTION is more stable than the passed
         *         TYPE_OPTION; <code>false</code> otherwise.
         */
        public boolean stablerThan(TYPE_OPTION type) {
            if (type == null) return true;
            
            return (this.stability < type.stability());
        }
        
        /**
         * Checks if this TYPE_OPTION is considered as stable as the passed instance.
         * 
         * @param type - another TYPE_OPTION to compare stability against.
         * @return <code>true</code> if this TYPE_OPTION is as stable as the passed
         *         TYPE_OPTION; <code>false</code> otherwise.
         */
        public boolean asStableAs(TYPE_OPTION type) {
            if (type == null) return true;
            
            return (this.stability == type.stability());
        }
        
        /**
         * Checks if this TYPE_OPTION is considered less stable than the passed
         * instance.
         * 
         * @param type - another TYPE_OPTION to compare stability against.
         * @return <code>true</code> if this TYPE_OPTION is less stable than the passed
         *         TYPE_OPTION; <code>false</code> otherwise.
         */
        public boolean lessStableThan(TYPE_OPTION type) {
            if (type == null) return true;
            
            return (this.stability > type.stability());
        }
        
        /**
         * Checks if this type "covers" the passed type. A type covers another if it is
         * less than or equal to the other's stability. For example, {@link #SNAPSHOT}
         * covers both {@link #BETA} and {@link #RELEASE}, since it is less stable than
         * both.
         * <p>
         * The term <b>covers</b> defines what types are subject to inclusion during the
         * update process. If the user, say, configures the application to check for
         * {@link SNAPSHOT} updates, that setting includes, <b>covers</b>, or spans
         * across the aforementioned types. Those other types would then be considered
         * for possible updates under configuration.
         * <p>
         * Inversely, if the configuration is set to {@link #RELEASE}, then only
         * {@link #RELEASE} updates will be offered to the user, as that type only
         * covers itself, given its stability value.
         * 
         * @param type - the {@link TYPE_OPTION} to check for coverage on.
         * @return <code>true</code> if this type covers the passed type;
         *         <code>false</code> otherwise.
         */
        public boolean covers(TYPE_OPTION type) {
            if (type == null) return false;
            
            // i.e. RELEASE (0) is covered by BETA (1)
            return (type.stability <= this.stability);
        }
        
        /**
         * Attempts to safely parse the passed String and match it to a valid
         * TYPE_OPTION value.
         * 
         * @param str - a String with the TYPE_OPTION value.
         * @return the TYPE_OPTION that matches the passed String value, or
         *         <code>null</code> if no match exists.
         */
        public static TYPE_OPTION parseSafe(String str) {
            TYPE_OPTION ext = null;
            try {
                ext = TYPE_OPTION.valueOf(str);
            } catch(IllegalArgumentException e) {}
            return ext;
        }
        
        @Override
        public String label() {
            return switch (this) {
            case RELEASE  -> "🔥 Release";
            case BETA     -> "🔨 Beta";
            case SNAPSHOT -> "🛠️ Snapshot";
            };
        }
        @Override
        public String description() {
            return switch (this) {
            case RELEASE  -> "Standard application releases.";
            case BETA     -> "Beta versions are still being tested and may contain more bugs.";
            case SNAPSHOT -> "Snapshot versions are being heavily tested with new changes. May contain major bugs or incomplete features.";
            };
        }
    }
}
