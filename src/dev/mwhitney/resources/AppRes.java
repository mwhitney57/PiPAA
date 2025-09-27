package dev.mwhitney.resources;

import java.awt.Color;
import java.awt.Font;

import dev.mwhitney.main.PiPProperty.TYPE_OPTION;
import dev.mwhitney.update.api.Build;
import dev.mwhitney.update.api.Version;

/**
 * Application internal resource information, including names, versions, paths,
 * and other application-wide resources. The ability to refactor the project, or
 * at least the ease of doing so, is greatly improved through the use of this
 * class.
 * <p>
 * Mainly contains information regarding <b>internal</b> resources, meaning
 * resources packed within the application. Other classes may define information
 * about external resources.
 * 
 * @author mwhitney57
 * @since 0.9.4
 */
public abstract class AppRes {
    /*
     * DEVELOPER NOTE:
     * Refraining from defining documentation for *every* variable in this class.
     * An excess of documentation for a class like this runs counter to its core purpose.
     * In most cases, the name should give enough clarity as to the purpose of the variable.
     * If not, check where the variable is used to get the context.
     */
    
    // Switches
    /** A boolean for whether or not the backup LibVlc solution should be used. */
    public static volatile boolean USING_BACKUP_LIBVLC = false;
    
    // Application
    public static final String APP_NAME_SHORT               = "PiPAA";
    public static final String APP_NAME                     = "PiP Anything Anywhere";
    public static final String APP_NAME_FULL                = "Picture-in-Picture Anything Anywhere";
    public static final Build  APP_BUILD                    = new Build(new Version(0,9,5), TYPE_OPTION.SNAPSHOT);
    
    // Application Folders
    public static final String APP_FOLDER                   = System.getProperty("user.home") + "/AppData/Roaming/PiPAA";
    public static final String APP_BIN_FOLDER               = APP_FOLDER       + "/bin";
    public static final String APP_CACHE_FOLDER             = APP_FOLDER       + "/cache";
    public static final String APP_CONVERTED_FOLDER         = APP_CACHE_FOLDER + "/converted";
    public static final String APP_CLIPBOARD_FOLDER         = APP_CACHE_FOLDER + "/clipboard";
    public static final String APP_TRIMMED_FOLDER           = APP_CACHE_FOLDER + "/trimmed";
    
    // Binary Folders
    public static final String VLC_ART_CACHE_FOLDER         = System.getProperty("user.home") + "/AppData/Roaming/vlc/art";
    public static final String VLC_PLUGINS_FOLDER           = APP_BIN_FOLDER   + "/plugins";
    public static final String YTDLP_PLUGINS_FOLDER         = APP_BIN_FOLDER   + "/yt-dlp-plugins";
    
    // Binary Path Arguments
    /** A String with the path to the cookies file, which is used as the value to the cookies argument for some binaries. */
    public static final String COOKIES_PATH_ARG             = "\"" + APP_FOLDER     + "/cookies.txt\"";
    /** A String with the path containing app ffmpeg binaries, which is used as the value to the '--ffmpeg-location' argument for some binaries. */
    public static final String FFMPEG_LOC_ARG               = "\"" + APP_BIN_FOLDER + "/ffmpeg/bin\"";
    
    // Source Strings
    public static final String MEDIA_TRIM_EXT               = "_PiPAACrop";
    public static final String MEDIA_TRIM_EXT_STRICT        = "_PiPAACropS";
    
    // Internal Paths
    public static final String PATH_BIN                     = "/dev/mwhitney/resources/bin";
    public static final String PATH_SUB_FFMPEG              = "ffmpeg/bin/";
    public static final String PATH_SUB_IMAGEMAGICK         = "imagemagick/";
    
    // Internal Icons
    public static final String ICON_APP_16                  = "/dev/mwhitney/images/icon16.png";
    public static final String ICON_APP_32                  = "/dev/mwhitney/images/icon32.png";
    public static final String ICON_APP_32_WORKING          = "/dev/mwhitney/images/icon32Working.png";
    public static final String ICON_APP_32_DOWNLOADING      = "/dev/mwhitney/images/icon32Downloading.png";
    public static final String ICON_APP_32_TRIMMING         = "/dev/mwhitney/images/icon32Trimming.png";
    
    public static final String ICON_TRAY_INFO               = "/dev/mwhitney/images/iconInfo24.png";
    public static final String ICON_TRAY_CONFIG             = "/dev/mwhitney/images/iconConfig32.png";
    public static final String ICON_TRAY_GLOBE              = "/dev/mwhitney/images/iconGlobe24.png";
    public static final String ICON_TRAY_PAUSE              = "/dev/mwhitney/images/iconPause24.png";
    public static final String ICON_TRAY_PLAY               = "/dev/mwhitney/images/iconPlay24.png";
    public static final String ICON_TRAY_MUTE               = "/dev/mwhitney/images/iconMute32.png";
    public static final String ICON_TRAY_UNMUTE             = "/dev/mwhitney/images/iconUnmute32.png";
    public static final String ICON_TRAY_AUDIO              = "/dev/mwhitney/images/iconAudio32.png";
    public static final String ICON_TRAY_PLAYBACK           = "/dev/mwhitney/images/iconPlayback32.png";
    public static final String ICON_TRAY_MINIMIZE           = "/dev/mwhitney/images/iconMinimize24.png";
    public static final String ICON_TRAY_RESTORE            = "/dev/mwhitney/images/iconRestore24.png";
    public static final String ICON_TRAY_HIDE               = "/dev/mwhitney/images/iconHide24.png";
    public static final String ICON_TRAY_SHOW               = "/dev/mwhitney/images/iconShow24.png";
    public static final String ICON_TRAY_ADD                = "/dev/mwhitney/images/iconAdd24.png";
    public static final String ICON_TRAY_REMOVE             = "/dev/mwhitney/images/iconRemove24.png";
    public static final String ICON_TRAY_CLEAR              = "/dev/mwhitney/images/iconClear24.png";
    public static final String ICON_TRAY_EXIT               = "/dev/mwhitney/images/iconExit24.png";
    public static final String ICON_CHECKBOX                = "/dev/mwhitney/images/iconUICheck.png";
    public static final String ICON_CHECKBOX_SELECTED       = "/dev/mwhitney/images/iconUICheckFill.png";
    public static final String ICON_AUDIO_128               = "/dev/mwhitney/resources/audio128.png";
    
    // Internal Filenames
    public static final String NAME_YTDLP                   = "yt-dlp.exe";
    public static final String NAME_GALLERYDL               = "gallery-dl.exe";
    public static final String NAME_FFMPEG                  = "ffmpeg.exe";
    public static final String NAME_IMAGEMAGICK             = "magick.exe";
    public static final String NAME_IMAGEMAGICKLICENSE      = "LICENSE.txt";
    public static final String NAME_IMAGEMAGICKNOTICE       = "NOTICE.txt";
    public static final String NAME_LIBVLC                  = "libvlc.dll";
    public static final String NAME_LIBVLCCORE              = "libvlccore.dll";
    public static final String NAME_LIBVLCPLUGINS           = "plugins.zip";
    
    // Internal File Versions
    public static final String VERS_VLC                     = "3.0.21 Vetinari";
    public static final String VERS_YTDLP                   = "stable@2025-03-31";
    public static final String VERS_GALLERYDL               = "1.29.3";
    public static final String VERS_FFMPEG                  = "7.1-120";
    public static final String VERS_IMAGEMAGICK             = "7.1.1-47";
    
    // Internal Files
    public static final String FILE_LIBVLC                  = PATH_BIN + "/" + NAME_LIBVLC;
    public static final String FILE_LIBVLCCORE              = PATH_BIN + "/" + NAME_LIBVLCCORE;
    public static final String FILE_LIBVLCPLUGINS           = PATH_BIN + "/" + NAME_LIBVLCPLUGINS;
    
    // Colors
    public static final Color COLOR_TRANSPARENT             = new Color(0, 0, 0, 0);
    public static final Color COLOR_OFF_WHITE               = new Color(240, 240, 240);
    
    // Fonts
    public static final Font FONT_POPUP                     = new Font("Dialog", Font.BOLD, 16);
    public static final Font FONT_TEXT_COMPONENT            = new Font("Monospaced", Font.BOLD, 14);
    
    // Shortcuts Printout - Temporary Until Shortcuts Customization
    public static final String SHORTCUTS                    = """
                                                       PiPAA Keyboard and Mouse Controls
                                     _____________________________________________________________________
                                              LMB / MMB / RMB = Left, Middle, Right Mouse Button
                                                *Controls that respect the following modifiers:
                                        (Hold CTRL = More | Hold SHIFT = Less | Hold BOTH = Max Amount)
    
    Keyboard (Video/Audio/Adv. GIF):                                   | Mouse (Video/Audio/Adv. GIF):
    -------------------------------------------------------------------|-------------------------------------------------------------------
    Spacebar         -> Play/Pause                                     | LMB Click                       -> Play/Pause
    L-Arrow        * -> Seek Backwards                                 | MMB (Hold) then LMB Click       -> Seek Backwards
    R-Arrow        * -> Seek Forwards                                  | MMB (Hold) then RMB Click       -> Seek Forwards
    Up-Arrow       * -> Volume Up                                      | Scroll Up/Down                  -> Volume Adjust
    Down-Arrow     * -> Volume Down                                    | RMB (Hold) then Scroll Up/Down  -> Playback Rate Adjust
    Minus (-)      * -> Slower Playback Rate                           | RMB (Hold) then MMB Click       -> Playback Rate Reset
    Plus (+)       * -> Faster Playback Rate                           |
    Period (.)       -> Frame-by-Frame Forward                         |
    M                -> Mute/Unmute                                    |
    CTRL + SHIFT + M -> Global Mute/Unmute                             |
    0-9              -> Seek to 0%, 10%, ... 90% Through Video         |
    S                -> Cycle Subtitle Track                           |
    CTRL + S         -> Save Current Media to Cache                    | Mouse (Image/GIF):
    CTRL + ALT + S   -> Quick-Save Current Media to Cache (Inaccurate) |-------------------------------------------------------------------
    T                -> Cycle Audio Track                              | Scroll Up/Down                  -> Zoom
                                                                       | CTRL + MMB Click                -> Reset Zoom
                                                                       | LMB (Hold) & Drag               -> Pan (while zoomed)
    Keyboard (Audio):                                                  |
    -------------------------------------------------------------------|
    A                -> Add Artwork to Audio File                      |
                                                                       |
                                                                       |
    Keyboard (All):                                                    | Mouse (All):
    -------------------------------------------------------------------|-------------------------------------------------------------------
    B                -> Flash Borders of Window                        | RMB on Window & Drag            -> Move Window
    F                -> Fullscreen ON/OFF                              | Double-Click LMB                -> Fullscreen ON/OFF
    I                -> Show Window/Media Information                  | Double-Click LMB (Empty Window) -> Paste Shortcut
    L                -> Relocate Window In-Screen (if off-screen)      | Double-Click MMB                -> Add a Window
    Escape           -> Close Window                                   | Triple-Click RMB                -> Close Media in Window
    SHIFT + Escape   -> Close All Windows                              | Triple-Click RMB (Empty Window) -> Close Window
    SHIFT + A        -> Add a Window                                   |
    SHIFT + D        -> Duplicate Window                               |
    CTRL + C         -> Close Media in Window                          |
    CTRL + SHIFT + D -> Close then Delete Media from Cache (if cached) |
    CTRL + H         -> Hide Window                                    |
    CTRL + SHIFT + H -> Hide All Windows                               |
    CTRL + L         -> Open Window Lock Menu                          |
    CTRL + SHIFT + L -> Enable Window Size and Position Locks          |
    CTRL + ALT + L   -> Disable All Window Locks                       |
    CTRL + O         -> Open Cache Folder or Media's Folder Location   |
    CTRL + R         -> Reload Media                                   |
    CTRL + SHIFT + R -> Quick-Reload Media                             |
    """;
}
