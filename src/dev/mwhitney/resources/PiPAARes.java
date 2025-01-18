package dev.mwhitney.resources;

/**
 * PiPAA internal resource information, including names, versions, and paths.
 * The ability to refactor the project, or at least the ease of doing so, is
 * greatly improved through the use of this class.
 * <p>
 * Mainly contains information regarding <b>internal</b> resources, meaning
 * resources packed within PiPAA. Other classes may define information about
 * external resources.
 * 
 * @author mwhitney57
 * @since 0.9.4
 */
public abstract class PiPAARes {
    /*
     * DEVELOPER NOTE:
     * Refraining from defining documentation for each variable in this class.
     * An excess of documentation for a class like this runs counter to its core purpose.
     * In most cases, the name should give enough clarity as to the purpose of the variable.
     * If not, check where the variable is used to get the context.
     */
    
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
    public static final String VERS_YTDLP                   = "stable@2024-12-23";
    public static final String VERS_GALLERYDL               = "1.28.3";
    public static final String VERS_FFMPEG                  = "7.1-120";
    public static final String VERS_IMAGEMAGICK             = "7.1.1-43";
    
    // Internal Files
    public static final String FILE_LIBVLC                  = PATH_BIN + "/" + NAME_LIBVLC;
    public static final String FILE_LIBVLCCORE              = PATH_BIN + "/" + NAME_LIBVLCCORE;
    public static final String FILE_LIBVLCPLUGINS           = PATH_BIN + "/" + NAME_LIBVLCPLUGINS;
}
