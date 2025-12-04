package dev.mwhitney.main;

import static dev.mwhitney.resources.AppRes.APP_BIN_FOLDER;
import static dev.mwhitney.resources.AppRes.APP_BUILD;
import static dev.mwhitney.resources.AppRes.FILE_LIBVLC;
import static dev.mwhitney.resources.AppRes.FILE_LIBVLCCORE;
import static dev.mwhitney.resources.AppRes.FILE_LIBVLCPLUGINS;
import static dev.mwhitney.resources.AppRes.NAME_LIBVLC;
import static dev.mwhitney.resources.AppRes.NAME_LIBVLCCORE;
import static dev.mwhitney.resources.AppRes.VERS_VLC;
import static dev.mwhitney.resources.AppRes.VLC_PLUGINS_FOLDER;
import static dev.mwhitney.resources.AppRes.YTDLP_PLUGINS_FOLDER;

import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.InsetsUIResource;

import com.sun.jna.NativeLibrary;

import dev.mwhitney.exceptions.ExtractionException;
import dev.mwhitney.gui.PiPWindowManager;
import dev.mwhitney.gui.ProgressWindow;
import dev.mwhitney.gui.binds.BindController;
import dev.mwhitney.gui.popup.TopDialog;
import dev.mwhitney.listeners.BinRunnable;
import dev.mwhitney.listeners.PiPTrayAdapter;
import dev.mwhitney.main.Binaries.Bin;
import dev.mwhitney.properties.PiPProperty;
import dev.mwhitney.properties.PiPProperty.PropDefault;
import dev.mwhitney.properties.PiPProperty.TYPE_OPTION;
import dev.mwhitney.properties.PropertiesManager;
import dev.mwhitney.properties.PropertyListener;
import dev.mwhitney.resources.AppRes;
import dev.mwhitney.update.PiPUpdater;
import dev.mwhitney.update.PiPUpdater.PiPUpdateResult;
import dev.mwhitney.update.api.Build;
import dev.mwhitney.update.api.Version;
import dev.mwhitney.util.PiPAAUtils;
import net.codejava.utility.UnzipUtility;
import uk.co.caprica.vlcj.binding.support.runtime.RuntimeUtil;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;

/**
 * Initializes the PiP Anything Anywhere application.
 * 
 * @author mwhitney57
 */
public class Initializer {
    /** The {@link ProgressWindow} displayed during initialization to show loading progress. */
    private static ProgressWindow progressWin;
    
    // Main Method
    public static void main(String[] args) {
        // Initialize Properties First
        final PropertiesManager propsManager = new PropertiesManager();
        
        // Shared PropertyListener Methods
        final PropertyListener propListener = new PropertyListener() {
            @Override
            public void propertyChanged(PiPProperty prop, String value) {}
            @SuppressWarnings("unchecked")
            @Override
            public <T> T propertyState(PiPProperty prop, Class<T> rtnType) {
                // Ensure the property has a value. If it doesn't, reset it to the default.
                final boolean hasValue = propsManager.has(prop);
                if (!hasValue) propsManager.setDefault(prop);
                
                // Return a different type depending on the state request.
                final String value = propsManager.get(prop);
                return (T) switch (rtnType) {
                case Class<T> c when c == Boolean.class -> Boolean.valueOf(value);
                case Class<T> c when c == Integer.class -> Integer.valueOf(value);
                case Class<T> c when c == Float.class   -> Float.valueOf(value);
                case null, default -> value;
                };
            }
        };
        // Share PropertyListener with Binaries
        Binaries.setPropertyListener(propListener);
        
        // Get Theme for Loading Styling
//        final THEME_OPTION theme = PropDefault.THEME.matchAny(propListener.propertyState(PiPProperty.THEME, String.class));
        // Set L&F Before Managing Any GUI
        setLookAndFeel();
        
        // Create window for displaying initialization progress. 
        progressWin = PiPAAUtils.makeOnEDT(() -> 
            new ProgressWindow(new ImageIcon(Initializer.class.getResource(AppRes.BANNER_APP_LOADING)))
                .useTitle("Starting PiPAA...")
                .useIcon(AppRes.IMG_APP_32_WORK)
                .display()
        );
        
        loadingProgress("Setting up binds...", 5);
        
        final BindController bindController = new BindController();
        
        loadingProgress("Performing initialization checks...", 10);
        
        // Initialization Checks
        initChecks(propsManager, args);
        
        loadingProgress("Extracting application resources...", 20);
        
        // Extract Resources if Necessary
        try {
            extractLibResources(propsManager);
        } catch (ExtractionException ee) {
            loadingProgress("Failed to initialize!");
            TopDialog.showMsg("Failed to start PiPAA!\n" + ee.getMessage(), "Initialization Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
            return;
        }
        
        loadingProgress("Creating window manager...", 75);
        
        // Create PiPWindowManager, which then opens one window by default.
        final PiPWindowManager windowManager = new PiPWindowManager() {
            @Override
            public <T> T propertyState(PiPProperty prop, Class<T> rtnType) { return propListener.propertyState(prop, rtnType); }
            @Override
            public BindController getController() { return bindController; }
        };
        // Add a window when the loading process completes, if configured to.
        if (propListener.propertyState(PiPProperty.OPEN_WINDOW_AT_LAUNCH, Boolean.class))
            progressWin.whenComplete(windowManager::addWindow);

        loadingProgress("Setting up the tray...", 90);
        
        // Create the Tray object.
        final Tray tray = new Tray() {
            @Override
            public void propertyChanged(PiPProperty prop, String value) {
                // Only Set Prop Values for Certain PiPProperty Values
                switch(prop) {
                case SET_ALL_PAUSED, SET_ALL_MUTED, SET_ALL_PLAYBACK_RATE, SET_ALL_VOLUME -> {}
                case USE_SYS_BINARIES -> {
                    // Refresh available system binaries when property is enabled.
                    if (Boolean.valueOf(value)) try {
                        Binaries.refreshOnSys();
                    } catch (InterruptedException ignore) {}

                    propsManager.set(prop, value);
                }
                default -> propsManager.set(prop, value);
                }
                windowManager.propertyChanged(prop, value);
            }
            @Override
            public <T> T propertyState(PiPProperty prop, Class<T> rtnType) { return propListener.propertyState(prop, rtnType); }
        };
        
        loadingProgress("Finishing up...", 95);
        
        // Set Listeners
        windowManager.setWindowCountListener(() ->
            // Updates tray's status with window count.
            tray.updateStatus(windowManager.liveWindowCount() == 0 ? AppRes.TRAY_NO_WINDOWS_STATUS : "Running Windows: " + windowManager.liveWindowCount())
        );
        tray.setTrayListener(new PiPTrayAdapter() {
            @Override
            public PiPWindowManager get() { return windowManager; }
        });
        
        PropertiesManager.mediator = new PropertyListener() {
            @Override
            public void propertyChanged(PiPProperty prop, String value) { tray.forwardPropertyChange(prop, value); }
            @Override
            public <T> T propertyState(PiPProperty prop, Class<T> rtnType) { return propListener.propertyState(prop, rtnType); }
        };
        
        loadingProgress("Finished loading!", 100);
    }
    
    /**
     * Safely updates the {@link ProgressWindow} that is displayed during
     * initialization with the passed information.
     * <p>
     * This version of the method only updates the message without changing the
     * progress value.
     * 
     * @param msg - a String with the new status message to display.
     * @since 0.9.5
     */
    private static void loadingProgress(String msg) {
        if (progressWin != null) progressWin.setMessage(msg);
    }
    
    /**
     * Safely updates the {@link ProgressWindow} that is displayed during
     * initialization with the passed information.
     * 
     * @param msg      - a String with the new status message to display.
     * @param progress - a float with the new progress value.
     * @since 0.9.5
     */
    private static void loadingProgress(String msg, float progress) {
        if (progressWin != null) progressWin.update(msg, progress);
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
                    final String msg, title;
                    if (APP_BUILD.equals(fromBuild)) {
                        msg   = forced ? "PiPAA was forcefully updated to the same version.\n\nThe app may not be any different." : "PiPAA failed to update.";
                        title = forced ? "Update Complete" : "Update Error";
                    } else {
                        msg   = "PiPAA has been updated from " + fromBuild + " to " + APP_BUILD + ".";
                        title = "Update Complete";
                    }
                    CompletableFuture.runAsync(() -> JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE));
                }
            } catch (Exception e) { /* Do Nothing -- Assume User Manual Configuration Error, Ultimately Deletes Invalid Properties */ }
            propsManager.remove(PiPProperty.APP_UPDATING_FROM);
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
                UIManager.getLookAndFeelDefaults().put("TabbedPane.contentOpaque", false);
                UIManager.getLookAndFeelDefaults().put("TabbedPane.tabInsets", new Insets(2, 10, 0, 10));
                UIManager.getLookAndFeelDefaults().put("TabbedPane.selectedTabPadInsets", new InsetsUIResource(3, 0, 3, 0));
                UIManager.getLookAndFeelDefaults().put("ToolTip.font", AppRes.FONT_TOOLTIP);
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
     * @throws ExtractionException if another exception occurs during the extraction process.
     */
    private static void extractLibResources(PropertiesManager propsManager) throws ExtractionException {
        // Ensure bin and plugins folders exists.
        PiPAAUtils.ensureExistence(APP_BIN_FOLDER, YTDLP_PLUGINS_FOLDER);
        
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
        
        loadingProgress("Extracting missing binaries...", 25);
        
        // Extract any missing binaries to the application bin folder.
        CFExec.run(!Binaries.HAS_YTDLP     && !LOCAL_YTDLP  ? (BinRunnable) () -> Binaries.extract(Bin.YT_DLP)     : null,
                   !Binaries.HAS_GALLERYDL && !LOCAL_GALDL  ? (BinRunnable) () -> Binaries.extract(Bin.GALLERY_DL) : null,
                   !Binaries.HAS_FFMPEG    && !LOCAL_FFMPEG ? (BinRunnable) () -> Binaries.extract(Bin.FFMPEG)     : null,
                   !Binaries.HAS_IMGMAGICK && !LOCAL_IMGMAG ? (BinRunnable) () -> Binaries.extract(Bin.IMGMAGICK)  : null)
              .throwIfAny(new ExtractionException("Unexpected exception occurred while extracting binaries."));
        
        loadingProgress("Checking for app update...", 35);  // Does not guarantee that a check occurs. Just a general progress statement.
        
        // Automatically update application during startup depending on user configuration and date/time.
        final String frequencyApp  = propsManager.get(PiPProperty.APP_UPDATE_FREQUENCY);
        final String lastCheckApp  = propsManager.get(PiPProperty.APP_LAST_UPDATE_CHECK);
        final String appUpdateType = propsManager.get(PiPProperty.APP_UPDATE_TYPE);
        final PiPUpdateResult result = PiPUpdater.updateApp(frequencyApp, lastCheckApp, PropDefault.TYPE.matchAny(appUpdateType), false);
        if (result.checked()) propsManager.set(PiPProperty.APP_LAST_UPDATE_CHECK, LocalDateTime.now().toString());
        if (result.updated()) {
            propsManager.set(PiPProperty.APP_UPDATING_FROM, APP_BUILD.toString());
            System.exit(0);
        }
        if (result.hasException()) System.err.println("Warning, app update process failed: " + result.exception().getTotalMessage());
        
        loadingProgress("Checking for updates to binaries...", 50); // Does not guarantee that a check occurs, as with app update.
        
        // Automatically update binaries during startup depending on user configuration and date/time.
        final String frequencyBin = propsManager.get(PiPProperty.BIN_UPDATE_FREQUENCY);
        final String lastCheckBin = propsManager.get(PiPProperty.BIN_LAST_UPDATE_CHECK);
        
        if (PiPUpdater.updateBin(frequencyBin, lastCheckBin)) // Update last update check time.
            propsManager.set(PiPProperty.BIN_LAST_UPDATE_CHECK, LocalDateTime.now().toString());
        
        loadingProgress("Picking VLC instance...", 60);
        
        // VLC is ready if configured to be used and installed on the system. Otherwise use PiPAA's version.
        boolean vlcReady = (useSysVLC != null && Boolean.valueOf(useSysVLC) ? new NativeDiscovery().discover() : false);
        final String vlcVersion = propsManager.get("LibVlc_BIN");
        // System VLC installation is not to be used and OS is Windows.
        if (!vlcReady && System.getProperty("os.name").startsWith("Windows")) {
            // Ensure that VLC files are extracted and present.
            final boolean vlcFilesPresent = (new File(APP_BIN_FOLDER + "/" + NAME_LIBVLC).exists()
                             && new File(APP_BIN_FOLDER + "/" + NAME_LIBVLCCORE).exists()
                             && new File(VLC_PLUGINS_FOLDER).exists());
            // VLC version not in configuration, or the versions don't match, or the required VLC files do not exist.
            if (vlcVersion == null || !vlcVersion.equals(VERS_VLC) || !vlcFilesPresent) {
                // Extract Windows LibVlc DLLs to Bin Folder
                System.out.println("<!> Extracting VLC libraries...");
                loadingProgress("Extracting VLC libraries...", 65);
                // Use try-with-resources to ensure closing of streams.
                try (final InputStream libvlc     = Initializer.class.getResourceAsStream(FILE_LIBVLC);
                     final InputStream libvlccore = Initializer.class.getResourceAsStream(FILE_LIBVLCCORE);
                     final InputStream plugins    = Initializer.class.getResourceAsStream(FILE_LIBVLCPLUGINS)) {
                    
                    // Attempt to copy and unzip all LibVlc-related files. Throw extraction exception if any had errors.
                    CFExec.run((BinRunnable) () -> Files.copy(libvlc, Paths.get(APP_BIN_FOLDER + "/" + NAME_LIBVLC), StandardCopyOption.REPLACE_EXISTING),
                               (BinRunnable) () -> Files.copy(libvlccore, Paths.get(APP_BIN_FOLDER + "/" + NAME_LIBVLCCORE), StandardCopyOption.REPLACE_EXISTING),
                               (BinRunnable) () -> UnzipUtility.unzip(plugins, VLC_PLUGINS_FOLDER))
                        .throwIfAny(new ExtractionException("Unexpected exception occurred while extracting LibVlc libraries."));
                    propsManager.set("LibVlc_BIN", VERS_VLC);
                } catch (IOException ioe) { /* Thrown while closing streams. Ignore. */
                } catch (ExtractionException ee) { throw ee; } // Forward exception throw.
            }
            // Change NativeLibrary search path to app bin folder, check if extracted already.
            NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), PiPAAUtils.slashFix(APP_BIN_FOLDER));
            vlcReady = true;
            AppRes.USING_BACKUP_LIBVLC = true;
        }
        // VLC Installation Doesn't Exist and Windows LibVlc DLLs Cannot Be Used
        if (!vlcReady) throw new ExtractionException("Could not find or extract a LibVlc installation.");
    }
}
