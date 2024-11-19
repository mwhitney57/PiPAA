package dev.mwhitney.main;

import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.sun.jna.NativeLibrary;

import dev.mwhitney.exceptions.ExtractionException;
import dev.mwhitney.gui.InvertibleIcon;
import dev.mwhitney.gui.PiPWindowManager;
import dev.mwhitney.gui.TopDialog;
import dev.mwhitney.listeners.BinRunnable;
import dev.mwhitney.listeners.PiPTrayAdapter;
import dev.mwhitney.listeners.PropertyListener;
import dev.mwhitney.main.Binaries.Bin;
import dev.mwhitney.main.PiPProperty.PropDefault;
import dev.mwhitney.main.PiPProperty.TYPE_OPTION;
import dev.mwhitney.update.PiPUpdater;
import dev.mwhitney.update.PiPUpdater.PiPUpdateResult;
import dev.mwhitney.update.api.Build;
import dev.mwhitney.update.api.Version;
import net.codejava.utility.UnzipUtility;
import uk.co.caprica.vlcj.binding.support.runtime.RuntimeUtil;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;

/**
 * Initializes the PiP Anything Anywhere application.
 * 
 * @author mwhitney57
 */
public class Initializer {

    // Public Static Final Application Variables
    /** The current version of the application. */
    public static final Build  APP_BUILD = new Build(new Version(0,9,4), TYPE_OPTION.SNAPSHOT);
    /** The application name, but shortened to an acronym. */
    public static final String APP_NAME_SHORT = "PiPAA";
    /** The application name. */
    public static final String APP_NAME = "PiP Anything Anywhere";
    /** The application name, but in full. */
    public static final String APP_NAME_FULL = "Picture-in-Picture Anything Anywhere";
    /** A <tt>String</tt> with the application folder location. */
    public static final String APP_FOLDER = System.getProperty("user.home") + "/AppData/Roaming/PiPAA";
    /** A <tt>String</tt> with the application's bin folder location. */
    public static final String APP_BIN_FOLDER = APP_FOLDER + "/bin";
    /** A <tt>String</tt> with the application's cache folder location. */
    public static final String APP_CACHE_FOLDER = APP_FOLDER + "/cache";
    /** A <tt>String</tt> with the application's GIF folder location in the cache. */
    public static final String APP_GIF_FOLDER = APP_CACHE_FOLDER + "/gif";
    /** A <tt>String</tt> with the application's clipboard folder location in the cache. */
    public static final String APP_CLIPBOARD_FOLDER = APP_CACHE_FOLDER + "/clipboard";
    /** A <tt>String</tt> with the application's trimmed folder location in the cache. */
    public static final String APP_TRIMMED_FOLDER = APP_CACHE_FOLDER + "/trimmed";
    /** A <tt>String</tt> with the LibVlc version shipped with the application. */
    public static final String VLC_VERSION = "3.0.21 Vetinari";
    /** A <tt>String</tt> with the LibVlc plugins folder. */
    public static final String VLC_PLUGINS_FOLDER = APP_BIN_FOLDER + "/plugins";
    /** A <tt>String</tt> with the VLC artwork cache folder. */
    public static final String VLC_ART_CACHE_FOLDER = System.getProperty("user.home") + "/AppData/Roaming/vlc/art";
    /** The keyboard and mouse shortcuts guide for PiPAA. */
    public static final String SHORTCUTS = """
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
    0-9              -> Seek to 0%, 10%, ... 90% Through Video         | Mouse (Image/GIF):
    CTRL + S         -> Save Current Media to Cache                    |-------------------------------------------------------------------
    CTRL + ALT + S   -> Quick-Save Current Media to Cache (Inaccurate) | Scroll Up/Down                  -> Zoom
    T                -> Cycle Audio Track                              | CTRL + MMB Click                -> Reset Zoom
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
    CTRL + SHIFT + M -> Global Mute ON/OFF                             |
    CTRL + O         -> Open Cache Folder or Media's Folder Location   |
    CTRL + R         -> Reload Media                                   |
    """;
    
    /** A boolean for whether or not the backup LibVlc solution should be used. */
    public static boolean USING_BACKUP_LIBVLC = false;
    
    public static void main(String[] args) {
        final PropertiesManager propsManager = new PropertiesManager();
        
        // L&F
        setLookAndFeel();
        // Initialization Checks
        initChecks(propsManager, args);
        
        // Extract Resources if Necessary
        try {
            extractLibResources(propsManager);
        } catch (ExtractionException ee) {
            TopDialog.showMsg("Failed to start PiPAA!\n" + ee.getMessage(), "Initialization Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
            return;
        }
        
        // Shared PropertyListener Methods
        final PropertyListener propListener = new PropertyListener() {
            @Override
            public void propertyChanged(PiPProperty prop, String value) {}
            @SuppressWarnings("unchecked")
            @Override
            public <T> T propertyState(PiPProperty prop, Class<T> rtnType) {
                // Ensure the property has a value. If it doesn't, reset it to the default.
                final boolean hasValue = propsManager.get(prop) != null;
                if (!hasValue) propsManager.setDefault(prop);
                
                // Return a different type depending on the state request.
                if (rtnType == Boolean.class)
                    return (T) Boolean.valueOf(propsManager.get(prop));
                else if (rtnType == Float.class)
                    return (T) Float.valueOf(propsManager.get(prop));
                else if (rtnType == Integer.class)
                    return (T) Integer.valueOf(propsManager.get(prop));
                else {
                    return (T) propsManager.get(prop);
                }
            }
        };
        
        // Share PropertyListener with Binaries
        Binaries.setPropertyListener(propListener);
        
        // Create PiPWindowManager, which then opens one window by default.
        final PiPWindowManager windowManager = new PiPWindowManager() {
            @Override
            public <T> T propertyState(PiPProperty prop, Class<T> rtnType) { return propListener.propertyState(prop, rtnType); }
        };

        // Create the Tray object.
        final Tray tray = new Tray() {
            @Override
            public void propertyChanged(PiPProperty prop, String value) {
                // Only Set Prop Values for Certain PiPProperty Values
                switch(prop) {
                case SET_ALL_PAUSED, SET_ALL_MUTED, SET_ALL_PLAYBACK_RATE, SET_ALL_VOLUME -> {}
                case USE_SYS_BINARIES -> {
                    // Refresh available system binaries when property is enabled.
                    if (Boolean.valueOf(value))
                        CFExec.run(() -> Binaries.refreshOnSys()).excepts((i, e) -> e.printStackTrace());
                    
                    propsManager.set(prop.toString(), value);
                }
                default -> propsManager.set(prop.toString(), value);
                }
                windowManager.propertyChanged(prop, value);
            }
            @Override
            public <T> T propertyState(PiPProperty prop, Class<T> rtnType) { return propListener.propertyState(prop, rtnType); }
        };
        
        // Set Listeners
        windowManager.setWindowCountListener(() ->
            // Updates tray's status with window count.
            tray.updateStatus(windowManager.liveWindowCount() == 0 ? "No Windows Running..." : "Running Windows: " + windowManager.liveWindowCount())
        );
        tray.setTrayListener(new PiPTrayAdapter() {
            @Override
            public PiPWindowManager get() { return windowManager; }
        });
        
        PropertiesManager.MEDIATOR = new PropertyListener() {
            @Override
            public void propertyChanged(PiPProperty prop, String value) { tray.forwardPropertyChange(prop, value); }
            @Override
            public <T> T propertyState(PiPProperty prop, Class<T> rtnType) { return propListener.propertyState(prop, rtnType); }
        };
    }
    
    /**
     * Performs initialization checks, including the handling of command-line
     * arguments. Since many initialization checks require a non-<code>null</code>
     * {@link PropertiesManager}, this method will <b>only</b> handle command-line
     * arguments if the passed {@link PropertiesManager} is <code>null</code>.
     * 
     * @param propsManager - the application PropertiesManager.,
     * @param args         - any and all command-line arguments provided to the
     *                     application.
     */
    private static void initChecks(final PropertiesManager propsManager, String[] args) {
        // Handle Any Command-Line Arguments
        if (args.length > 0) handleArgs(args);
        
        // Only Handle Command-Line Arguments -- Return Early on Null PropertiesManager
        if (propsManager == null) return;
        
        // Check if Application Was Performing an Update
        if (propsManager.has(PiPProperty.APP_UPDATING_FROM)) {
            String updatingFrom = propsManager.get(PiPProperty.APP_UPDATING_FROM);
            final boolean forced = (updatingFrom.startsWith("FORCED-"));
            if (forced) updatingFrom = updatingFrom.replaceFirst("FORCED-", "");
            try {
                final String[] parts = updatingFrom.split("-");
                if (parts.length == 2) {
                    final Build fromBuild = new Build(Version.form(parts[0]), TYPE_OPTION.parseSafe(parts[1]));
                    if (APP_BUILD.equals(fromBuild) && forced)
                        CompletableFuture.runAsync(() -> JOptionPane.showMessageDialog(null, "PiPAA was forcefully updated to the same version.\n\nThe app may not be any different.", "Update Complete", JOptionPane.INFORMATION_MESSAGE));
                    else if (APP_BUILD.equals(fromBuild))
                        CompletableFuture.runAsync(() -> JOptionPane.showMessageDialog(null, "PiPAA failed to update.", "Update Error", JOptionPane.INFORMATION_MESSAGE));
                    else
                        CompletableFuture.runAsync(() -> JOptionPane.showMessageDialog(null, "PiPAA has been updated from " + fromBuild + " to " + APP_BUILD + ".", "Update Complete", JOptionPane.INFORMATION_MESSAGE));
                }
            } catch (Exception e) { /* Do Nothing -- Assume User Manual Configuration Error, Ultimately Deletes Invalid Properties */ }
            propsManager.getProperties().remove(PiPProperty.APP_UPDATING_FROM.toString());
        }
    }
    
    /**
     * Handles application command-line arguments.
     * 
     * @param args - a String[] of command-line arguments to handle.
     */
    private static void handleArgs(String[] args) {
        for (String arg : args) {
            if (arg == null || arg.isBlank()) continue;
            else arg = arg.trim().toLowerCase();
            
            // Do Nothing -- No Argument Handling Features Yet
//            switch (arg) {
//            case "-updated" -> JOptionPane.showMessageDialog(null, "PiPAA has been updated to " + APP_BUILD + ".", "Update Complete", JOptionPane.INFORMATION_MESSAGE);
//            }
        }
    }
    
    /**
     * Sets the application's Look and Feel to match the system.
     */
    private static void setLookAndFeel() {
        // Utilize L&F Library for slightly better system tray context menu.
        try {
            SwingUtilities.invokeAndWait(() -> {
                // Set L&F to Match System
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                        | UnsupportedLookAndFeelException e) {
                    // Error setting look and feel.
                    e.printStackTrace();
                }
                
                // Debug
//                UIManager.getLookAndFeelDefaults().forEach((e, e2) -> {
//                    System.out.println(e + ": " + e2);
//                });
                
                // Common Theme Defaults
                UIManager.getLookAndFeelDefaults().put("Menu.opaque", true);
                UIManager.getLookAndFeelDefaults().put("MenuItem.opaque", true);
                UIManager.getLookAndFeelDefaults().put("Menu.arrowIcon", new InvertibleIcon(((Icon) UIManager.getLookAndFeelDefaults().get("Menu.arrowIcon"))));
                UIManager.getLookAndFeelDefaults().put("TabbedPane.contentOpaque", false);
                UIManager.getLookAndFeelDefaults().put("TabbedPane.tabInsets", new Insets(2, 10, 0, 10));
            });
        } catch (InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Prepares the library/binary resources and extracts the necessary ones from
     * the JAR. This method will also create the bin and plugins directories if they
     * do not exist. The resources will not be extracted if the application is set
     * to use the system binaries, and they exist. If the application is set to use
     * the system binaries, they will be used in preference. If only some of the
     * binaries exist on the system, it will still prefer to use those, and PiPAA
     * will extract and use the missing, necessary binaries.
     * 
     * @param propsManager - the PropertiesManager used to check if the use system binaries property is set.
     * @throws IOException if there are issues reading or writing files.
     * @throws ExtractionException if another exception occurs during the extraction process.
     */
    private static void extractLibResources(PropertiesManager propsManager) throws ExtractionException {
        // Ensure bin folder exists.
        new File(APP_BIN_FOLDER).mkdirs();
        // Ensure plugins folder exists.
        new File(Binaries.YTDLP_PLUGINS_FOLDER).mkdirs();
        
        // Check if each binary exists within the application bin folder.
        final String useSysVLC      = propsManager.get(PiPProperty.USE_SYS_VLC);
        final String useSysBinaries = propsManager.get(PiPProperty.USE_SYS_BINARIES);
        final boolean LOCAL_YTDLP   = Binaries.exists(Bin.YT_DLP);
        final boolean LOCAL_GALDL   = Binaries.exists(Bin.GALLERY_DL);
        final boolean LOCAL_FFMPEG  = Binaries.exists(Bin.FFMPEG);
        final boolean LOCAL_IMGMAG  = Binaries.exists(Bin.IMGMAGICK);
        // If the application should use system binaries, refresh their existence status.
        if (useSysBinaries == null || Boolean.valueOf(useSysBinaries)) try {
            Binaries.refreshOnSys();
        } catch (InterruptedException ie) {
            throw new ExtractionException("Unexpected exception occurred while extracting binaries.");
        }
        
        // Extract any missing binaries to the application bin folder.
        CFExec.run((BinRunnable) () -> { if (!Binaries.HAS_YTDLP     && !LOCAL_YTDLP)  Binaries.extract(Bin.YT_DLP);     },
                   (BinRunnable) () -> { if (!Binaries.HAS_GALLERYDL && !LOCAL_GALDL)  Binaries.extract(Bin.GALLERY_DL); },
                   (BinRunnable) () -> { if (!Binaries.HAS_FFMPEG    && !LOCAL_FFMPEG) Binaries.extract(Bin.FFMPEG);     },
                   (BinRunnable) () -> { if (!Binaries.HAS_IMGMAGICK && !LOCAL_IMGMAG) Binaries.extract(Bin.IMGMAGICK);  })
              .throwIfAny(new ExtractionException("Unexpected exception occurred while extracting binaries."));
        
        // Automatically update application during startup depending on user configuration and date/time.
        final String frequencyApp  = propsManager.get(PiPProperty.APP_UPDATE_FREQUENCY);
        final String lastCheckApp  = propsManager.get(PiPProperty.APP_LAST_UPDATE_CHECK);
        final String appUpdateType = propsManager.get(PiPProperty.APP_UPDATE_TYPE);
        final PiPUpdateResult result = PiPUpdater.updateApp(frequencyApp, lastCheckApp, PropDefault.TYPE.matchAny(appUpdateType), false);
        if (result.checked()) propsManager.set(PiPProperty.APP_LAST_UPDATE_CHECK.toString(), LocalDateTime.now().toString());
        if (result.updated()) {
            propsManager.set(PiPProperty.APP_UPDATING_FROM.toString(), Initializer.APP_BUILD.toString());
            System.exit(0);
        }
        if (result.hasException()) System.err.println("Warning, app update process failed: " + result.exception().getTotalMessage());
        
        // Automatically update binaries during startup depending on user configuration and date/time.
        final String frequencyBin = propsManager.get(PiPProperty.BIN_UPDATE_FREQUENCY);
        final String lastCheckBin = propsManager.get(PiPProperty.BIN_LAST_UPDATE_CHECK);
        if (PiPUpdater.updateBin(frequencyBin, lastCheckBin)) // Update last update check time.
            propsManager.set(PiPProperty.BIN_LAST_UPDATE_CHECK.toString(), LocalDateTime.now().toString());
        
        // VLC is ready if configured to be used and installed on the system. Otherwise use PiPAA's version.
        boolean vlcReady = (useSysVLC != null && Boolean.valueOf(useSysVLC) ? new NativeDiscovery().discover() : false);
        final String vlcVersion = propsManager.get("LibVlc_BIN");
        // System VLC installation is not to be used and OS is Windows.
        if (!vlcReady && System.getProperty("os.name").startsWith("Windows")) {
            // Ensure that VLC files are extracted and present.
            final boolean vlcFilesPresent = (new File(APP_BIN_FOLDER + "/libvlc.dll").exists()
                             && new File(APP_BIN_FOLDER + "/libvlccore.dll").exists()
                             && new File(VLC_PLUGINS_FOLDER).exists());
            // VLC version not in configuration, or the versions don't match, or the required VLC files do not exist.
            if (vlcVersion == null || !vlcVersion.equals(VLC_VERSION) || !vlcFilesPresent) {
                // Extract Windows LibVlc DLLs to Bin Folder
                System.out.println("<!> Extracting VLC libraries...");
                CFExec.run((BinRunnable) () -> Files.copy(Initializer.class.getResourceAsStream("/dev/mwhitney/resources/bin/libvlc.dll"),
                                   Paths.get(APP_BIN_FOLDER + "/libvlc.dll"), StandardCopyOption.REPLACE_EXISTING),
                           (BinRunnable) () -> Files.copy(Initializer.class.getResourceAsStream("/dev/mwhitney/resources/bin/libvlccore.dll"),
                                   Paths.get(APP_BIN_FOLDER + "/libvlccore.dll"), StandardCopyOption.REPLACE_EXISTING),
                           (BinRunnable) () -> UnzipUtility.unzip(Initializer.class.getResourceAsStream("/dev/mwhitney/resources/bin/plugins.zip"), VLC_PLUGINS_FOLDER))
                      .throwIfAny(new ExtractionException("Unexpected exception occurred while extracting LibVlc libraries."));
                propsManager.set("LibVlc_BIN", VLC_VERSION);
            }
            // Change NativeLibrary search path to app bin folder, check if extracted already.
            NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), APP_BIN_FOLDER.replace('/', '\\'));
            vlcReady = true;
            USING_BACKUP_LIBVLC = true;
        }
        // VLC Installation Doesn't Exist and Windows LibVlc DLLs Cannot Be Used
        if (!vlcReady) throw new ExtractionException("Could not find or extract a LibVlc installation.");
    }
}
