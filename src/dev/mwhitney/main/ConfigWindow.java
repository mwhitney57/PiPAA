package dev.mwhitney.main;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;

import dev.mwhitney.gui.BetterButton;
import dev.mwhitney.gui.BetterCheckbox;
import dev.mwhitney.gui.BetterComboBox;
import dev.mwhitney.gui.BetterLabel;
import dev.mwhitney.gui.BetterPanel;
import dev.mwhitney.gui.BetterSlider;
import dev.mwhitney.gui.BetterTabbedPane;
import dev.mwhitney.gui.BetterTextArea;
import dev.mwhitney.gui.EasyTopDialog;
import dev.mwhitney.gui.Themed;
import dev.mwhitney.gui.TopDialog;
import dev.mwhitney.listeners.PropertyListener;
import dev.mwhitney.main.PiPProperty.DOWNLOAD_OPTION;
import dev.mwhitney.main.PiPProperty.FREQUENCY_OPTION;
import dev.mwhitney.main.PiPProperty.OVERWRITE_OPTION;
import dev.mwhitney.main.PiPProperty.PLAYBACK_OPTION;
import dev.mwhitney.main.PiPProperty.PropDefault;
import dev.mwhitney.main.PiPProperty.THEME_OPTION;
import dev.mwhitney.main.PiPProperty.THEME_OPTION.COLOR;
import dev.mwhitney.main.PiPProperty.TRIM_OPTION;
import dev.mwhitney.main.PiPProperty.TYPE_OPTION;
import dev.mwhitney.update.PiPUpdater;
import dev.mwhitney.update.PiPUpdater.PiPUpdateResult;
import dev.mwhitney.util.PiPAAUtils;
import net.miginfocom.swing.MigLayout;

/**
 * The configuration window for PiPAA.
 * 
 * @author mwhitney57
 */
public class ConfigWindow extends JFrame implements PropertyListener, Themed {
    /** The randomly-generated, unique serial ID for SettingsWindows. */
    private static final long serialVersionUID = 7828132102847858358L;
    
    /** The JLabel with the configuration header. */
    private JLabel header;
    /** The BetterPanel with the uppermost content pane. */
    private BetterPanel contentPane;
    /** The BetterTabbedPane with the config-specific panes. */
    private final BetterTabbedPane tabbedPane;
    /** The BetterPanel with the General config options. */
    private final BetterPanel paneGeneral;
    /** The BetterPanel with the Playback config options. */
    private final BetterPanel panePlayback;
    /** The BetterPanel with the Cache config options. */
    private final BetterPanel paneCache;
    /** The BetterPanel with the Update config options. */
    private final BetterPanel paneUpdates;
    /** The BetterPanel with the Advanced config options. */
    private final BetterPanel paneAdvanced;
    /** The BetterComboBox for the {@link PiPProperty#THEME} property. */
    private BetterComboBox comboTheme;
    /** The BetterComboBox for the {@link PiPProperty#GIF_PLAYBACK_MODE} property. */
    private BetterComboBox comboGIFPlayback;
    /** The BetterCheckbox for the {@link PiPProperty#USE_SYS_BINARIES} property. */
    private BetterCheckbox chkSystemBin;
    /** The BetterCheckbox for the {@link PiPProperty#DND_PREFER_LINK} property. */
    private BetterCheckbox chkPreferLinkDND;
    /** The BetterCheckbox for the {@link PiPProperty#DOWNLOAD_WEB_MEDIA} property. */
    private BetterComboBox comboDLWebMedia;
    /** The BetterCheckbox for the {@link PiPProperty#CONVERT_WEB_INDIRECT} property. */
    private BetterCheckbox chkConvertIndWeb;
    /** The BetterCheckbox for the {@link PiPProperty#TRIM_TRANSPARENCY} property. */
    private BetterCheckbox chkTrimTransparency;
    /** The BetterComboBox for the {@link PiPProperty#THEME} property. */
    private BetterComboBox comboTrim;
    /** The BetterCheckbox for the {@link PiPProperty#GLOBAL_MUTED} property. */
    private BetterCheckbox chkGlobMute;
    /** The BetterLabel for the {@link PiPProperty#DEFAULT_VOLUME} property. */
    private BetterLabel lblDefVolTitle;
    /** The BetterSlider for the {@link PiPProperty#DEFAULT_VOLUME} property. */
    private BetterSlider sliderDefVol;
    /** The BetterLabel for the {@link PiPProperty#DEFAULT_PLAYBACK_RATE} property. */
    private BetterLabel lblDefRateTitle;
    /** The BetterSlider for the {@link PiPProperty#DEFAULT_PLAYBACK_RATE} property. */
    private BetterSlider sliderDefRate;
    /** The BetterCheckbox for the {@link PiPProperty#DISABLE_CACHE} property. */
    private BetterCheckbox chkDisCache;
    /** The BetterComboBox for the {@link PiPProperty#OVERWRITE_CACHE} property. */
    private BetterComboBox comboOverwriteCache;
    /** The BetterComboBox for the {@link PiPProperty#APP_UPDATE_FREQUENCY} property. */
    private BetterComboBox comboAppUpdateFreq;
    /** The BetterComboBox for the {@link PiPProperty#APP_UPDATE_TYPE} property. */
    private BetterComboBox comboAppUpdateType;
    /** The BetterCheckbox for the {@link PiPProperty#APP_UPDATE_FORCE} property. */
    private BetterCheckbox chkForceAppUpdate;
    /** The BetterComboBox for the {@link PiPProperty#BIN_UPDATE_FREQUENCY} property. */
    private BetterComboBox comboBinUpdateFreq;
    
    /**
     * Creates a ConfigWindow with the passed title.
     * 
     * @param title - a String with the window title.
     */
    public ConfigWindow(String title) {
        super(title);
        
        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        
        setAlwaysOnTop(true);
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setResizable(false);
        setBackground(new Color(0, 0, 0, 0));
        setBounds(Math.max(0, screen.width - 530), Math.max(0, screen.height - 650), 520, 600);
        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent e) { setVisible(false); }
        });
        
        // Component Setup
        final Font textFont = new Font(null, Font.PLAIN, 12);
        final Font titleFont = textFont.deriveFont(Font.BOLD, 16f);
        final Map<TextAttribute, Object> attributes = new HashMap<>();
        attributes.put(TextAttribute.TRACKING, 0.04);
        final Font headerFont = textFont.deriveFont(Font.BOLD, 18f).deriveFont(attributes);
        
//        final BetterPanel contentPane = new BetterPanel(new MigLayout("debug"));
        contentPane  = new BetterPanel(new MigLayout("fill, insets 10 10 10 10"));
        tabbedPane   = new BetterTabbedPane(titleFont);
        paneGeneral  = new BetterPanel(new MigLayout(), true);
        panePlayback = new BetterPanel(new MigLayout(), true);
        paneCache    = new BetterPanel(new MigLayout(), true);
        paneUpdates  = new BetterPanel(new MigLayout(), true);
        paneAdvanced = new BetterPanel(new MigLayout(), true);
        tabbedPane.addTab("General", paneGeneral);
        tabbedPane.addTab("Playback", panePlayback);
        tabbedPane.addTab("Cache", paneCache);
        tabbedPane.addTab("Updates", paneUpdates);
        tabbedPane.addTab("Advanced", paneAdvanced);
        setContentPane(contentPane);
        
        header = new JLabel("Configuration", SwingConstants.CENTER);
        header.setFont(titleFont.deriveFont(24f));
        
        // -------------------- General Panel --------------------
        final BetterLabel lblThemeTitle = new BetterLabel("Theme", titleFont);
        comboTheme = new BetterComboBox(new String[] {"☀️ Light", "🌑 Dark", "🌸 Pink", "🌊 Subnautica"}, titleFont, (e) -> {
            final int selection = (int) ((BetterComboBox) e.getSource()).getSelectedIndex();
            switch (selection) {
            case 0 -> propertyChanged(PiPProperty.THEME, THEME_OPTION.LIGHT.toString());
            case 1 -> propertyChanged(PiPProperty.THEME, THEME_OPTION.DARK.toString());
            case 2 -> propertyChanged(PiPProperty.THEME, THEME_OPTION.PINK.toString());
            case 3 -> propertyChanged(PiPProperty.THEME, THEME_OPTION.SUBNAUTICA.toString());
            }
            pickTheme(THEME_OPTION.LIGHT.matchAny(propertyState(PiPProperty.THEME, String.class)));
        });
        chkPreferLinkDND = new BetterCheckbox("🔗 Prefer Links with Drag and Drop", true, titleFont);
        chkPreferLinkDND.addActionListener((e) -> propertyChanged(PiPProperty.DND_PREFER_LINK, Boolean.toString(((BetterCheckbox) e.getSource()).isSelected())));
        final BetterLabel lblPreferLinkDND = new BetterLabel(PiPPropertyDesc.DND_PREFER_LINK, textFont);
        final BetterLabel lblDLWebMediaTitle = new BetterLabel("Download Web Media", titleFont);
        comboDLWebMedia = new BetterComboBox(new String[] {"❌ Never", "💾 Normal", "✔️ Always"}, titleFont, (e) -> {
            final int selection = (int) ((BetterComboBox) e.getSource()).getSelectedIndex();
            final DOWNLOAD_OPTION download = switch (selection) {
            case 0  -> DOWNLOAD_OPTION.NEVER;
            case 1  -> DOWNLOAD_OPTION.NORMAL;
            case 2  -> DOWNLOAD_OPTION.ALWAYS;
            default -> throw new IllegalArgumentException("Unexpected value: " + selection);
            };
            propertyChanged(PiPProperty.DOWNLOAD_WEB_MEDIA, download.toString());
            ((BetterComboBox) e.getSource()).setToolTipText(download.description());
        });
        final BetterLabel lblDLWebMedia = new BetterLabel(PiPPropertyDesc.DOWNLOAD_WEB_MEDIA, textFont);
        chkConvertIndWeb = new BetterCheckbox("📦 Convert Indirect Web Links to Direct", true, titleFont);
        chkConvertIndWeb.addActionListener((e) -> propertyChanged(PiPProperty.CONVERT_WEB_INDIRECT, Boolean.toString(((BetterCheckbox) e.getSource()).isSelected())));
        final BetterLabel lblConvIndWeb = new BetterLabel(PiPPropertyDesc.CONVERT_WEB_INDIRECT, textFont);
        chkTrimTransparency = new BetterCheckbox("✂️ Trim Transparent Edges", true, titleFont);
        chkTrimTransparency.addActionListener((e) -> propertyChanged(PiPProperty.TRIM_TRANSPARENCY, Boolean.toString(((BetterCheckbox) e.getSource()).isSelected())));
        comboTrim = new BetterComboBox(new String[] {"Normal", "Strict", "Force"}, titleFont, (e) -> {
            final int selection = (int) ((BetterComboBox) e.getSource()).getSelectedIndex();
            final TRIM_OPTION option = switch (selection) {
            case 0  -> TRIM_OPTION.NORMAL;
            case 1  -> TRIM_OPTION.STRICT;
            case 2  -> TRIM_OPTION.FORCE;
            default -> throw new IllegalArgumentException("Unexpected value: " + selection);
            };
            propertyChanged(PiPProperty.TRIM_TRANSPARENCY_OPTION, option.toString());
            ((BetterComboBox) e.getSource()).setToolTipText(option.description());
        });
        final BetterLabel lblTrimTransparency = new BetterLabel(PiPPropertyDesc.TRIM_TRANSPARENCY, textFont);
        final BetterButton btnOpenFolder = new BetterButton("Open Application Folder", titleFont, (e) -> {
            try {
                Desktop.getDesktop().open(new File(Initializer.APP_FOLDER));
            } catch (IOException ioe) { ioe.printStackTrace(); }
        });
        final BetterButton btnShowShortcuts = new BetterButton("Show Keyboard and Mouse Shortcuts", titleFont, (e) -> {
            final BetterTextArea shortcutsComp = new BetterTextArea(Initializer.SHORTCUTS);
            TopDialog.showMsg(shortcutsComp, "Keyboard and Mouse Shortcuts", JOptionPane.PLAIN_MESSAGE);
        });
        
        // -------------------- Playback Panel --------------------
        final BetterLabel lblGIFPlaybackTitle = new BetterLabel("GIF Playback Mode", titleFont);
        comboGIFPlayback = new BetterComboBox(new String[] {"🍎 Basic", "🚀 Advanced"}, titleFont, (e) -> {
            final int selection = (int) ((BetterComboBox) e.getSource()).getSelectedIndex();
            final PLAYBACK_OPTION playback = switch (selection) {
            case 0  -> PLAYBACK_OPTION.BASIC;
            case 1  -> PLAYBACK_OPTION.ADVANCED;
            default -> throw new IllegalArgumentException("Unexpected value: " + selection);
            };
            propertyChanged(PiPProperty.GIF_PLAYBACK_MODE, playback.toString());
            ((BetterComboBox) e.getSource()).setToolTipText(playback.description());
        });
        final BetterLabel lblGIFPlayback = new BetterLabel(PiPPropertyDesc.GIF_PLAYBACK_MODE, textFont);
        chkGlobMute = new BetterCheckbox("🔇 Global Mute", true, titleFont);
        chkGlobMute.addActionListener((e) -> propertyChanged(PiPProperty.GLOBAL_MUTED, Boolean.toString(((BetterCheckbox) e.getSource()).isSelected())));
        final BetterLabel lblGlobMute = new BetterLabel(PiPPropertyDesc.GLOBAL_MUTED, textFont);
        lblDefVolTitle = new BetterLabel("Default Volume: 100", titleFont);
        sliderDefVol = new BetterSlider(0, 100, 50);
        sliderDefVol.setMinorTickSpacing(1);
        sliderDefVol.setMajorTickSpacing(5);
        sliderDefVol.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    propertyChanged(PiPProperty.DEFAULT_VOLUME, Integer.toString(50));
                    handlePropertyChange(PiPProperty.DEFAULT_VOLUME);
                } else
                    propertyChanged(PiPProperty.DEFAULT_VOLUME, Integer.toString(((JSlider) e.getSource()).getValue()));
            }
        });
        sliderDefVol.addChangeListener((e) -> lblDefVolTitle.setText(volTitleTxt()));
        final BetterLabel lblDefVol = new BetterLabel(PiPPropertyDesc.DEFAULT_VOLUME, textFont);
        lblDefRateTitle = new BetterLabel("Default Speed: 1.00x", titleFont);
        sliderDefRate = new BetterSlider(10, 400, 100);
        sliderDefRate.setMinorTickSpacing(2);
        sliderDefRate.setMajorTickSpacing(50);
        sliderDefRate.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    propertyChanged(PiPProperty.DEFAULT_PLAYBACK_RATE, Float.toString(1.00f));
                    handlePropertyChange(PiPProperty.DEFAULT_PLAYBACK_RATE);
                } else
                    propertyChanged(PiPProperty.DEFAULT_PLAYBACK_RATE, Float.toString(((JSlider) e.getSource()).getValue() / 100.0f));
            }
        });
        sliderDefRate.addChangeListener((e) -> lblDefRateTitle.setText(rateTitleTxt()));
        final BetterLabel lblDefRate = new BetterLabel(PiPPropertyDesc.DEFAULT_PLAYBACK_RATE, textFont);
        
        // -------------------- Cache Panel --------------------
        chkDisCache = new BetterCheckbox("❌ Disable Cache", true, titleFont);
        chkDisCache.addActionListener((e) -> propertyChanged(PiPProperty.DISABLE_CACHE, Boolean.toString(((BetterCheckbox) e.getSource()).isSelected())));
        final BetterLabel lblDisCache = new BetterLabel(PiPPropertyDesc.DISABLE_CACHE, textFont);
        final BetterLabel lblOverwriteCacheTitle = new BetterLabel("Overwrite Cached Media", titleFont);
        comboOverwriteCache = new BetterComboBox(new String[] {"❔ Ask Each Time", "✔️ Yes", "❌ No"}, titleFont, (e) -> {
            final int selection = (int) ((BetterComboBox) e.getSource()).getSelectedIndex();
            final OVERWRITE_OPTION overwrite = switch (selection) {
            case 0  -> OVERWRITE_OPTION.ASK;
            case 1  -> OVERWRITE_OPTION.YES;
            case 2  -> OVERWRITE_OPTION.NO;
            default -> throw new IllegalArgumentException("Unexpected value: " + selection);
            };
            propertyChanged(PiPProperty.OVERWRITE_CACHE, overwrite.toString());
            ((BetterComboBox) e.getSource()).setToolTipText(overwrite.description());
        });
        final BetterLabel lblAskCache = new BetterLabel(PiPPropertyDesc.OVERWRITE_CACHE, textFont);
        final BetterLabel lblMediaCacheTitle = new BetterLabel("Media Cache", titleFont);
        final BetterLabel lblMediaCache = new BetterLabel("Open, prune, or delete the media cache. Prune deletes empty folders and subfolders from the cache.", textFont);
        final BetterButton btnOpenCache = new BetterButton("Open", titleFont, (e) -> {
            // Ensure cache folder exists before trying to open it.
            final File cacheFolder = new File(Initializer.APP_CACHE_FOLDER);
            cacheFolder.mkdirs();
            
            try {
                Desktop.getDesktop().open(cacheFolder);
            } catch (IOException ioe) { ioe.printStackTrace(); }
        });
        final BetterButton btnPruneCache = new BetterButton("Prune", titleFont, (e) -> {
            CompletableFuture.runAsync(() -> {
                try {
                    PiPAAUtils.pruneCacheFolder();
                } catch (IOException ioe) {
                    EasyTopDialog.showMsg(this, "Error while pruning folders in cache.\nThis is unexpected, but should not break anything.", PropDefault.THEME.matchAny(propertyState(PiPProperty.THEME, String.class)), 5000, false);
                    return;
                }
                EasyTopDialog.showMsg(this, "Pruning Completed.", PropDefault.THEME.matchAny(propertyState(PiPProperty.THEME, String.class)), 1000, false);
            });
        });
        final BetterButton btnClearCache = new BetterButton("Clear", titleFont, (e) -> {
            final File cacheDir = new File(Initializer.APP_CACHE_FOLDER);
            cacheDir.mkdirs();
            CompletableFuture.runAsync(() -> {
                if (JOptionPane.YES_OPTION == TopDialog.showConfirm("Are you sure you want to irreversibly clear your media cache?\nSize: "
                        + PiPAAUtils.humanReadableByteCountSI(FileUtils.sizeOfDirectory(cacheDir)), "Clear Cache?", JOptionPane.YES_NO_OPTION)) {
                    try {
                        FileUtils.deleteDirectory(cacheDir);
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                        TopDialog.showMsg("Failed to clear media cache.\nPlease close any and all windows playing cached media and try again.", "Error!", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        });
        final BetterLabel lblVLCCacheTitle = new BetterLabel("VLC Artwork Cache", titleFont);
        final BetterLabel lblVLCCache      = new BetterLabel("Open or delete the VLC artwork cache. Artwork (or album art) is cached here occasionally when media is played.", textFont);
        final BetterButton btnOpenVLCCache = new BetterButton("Open", titleFont, (e) -> {
            // Ensure cache folder exists before trying to open it.
            final File vlcFolder = new File(Initializer.VLC_ART_CACHE_FOLDER);
            vlcFolder.mkdirs();
            
            try {
                Desktop.getDesktop().open(vlcFolder);
            } catch (IOException ioe) { ioe.printStackTrace(); }
        });
        final BetterButton btnClearVLCCache = new BetterButton("Clear", titleFont, (e) -> {
            final File vlcFolder = new File(Initializer.VLC_ART_CACHE_FOLDER);
            vlcFolder.mkdirs();
            CompletableFuture.runAsync(() -> {
                if (JOptionPane.YES_OPTION == TopDialog.showConfirm("Are you sure you want to irreversibly clear your VLC artwork cache?\nSize: "
                        + PiPAAUtils.humanReadableByteCountSI(FileUtils.sizeOfDirectory(vlcFolder)), "Clear Cache?", JOptionPane.YES_NO_OPTION)) {
                    try {
                        FileUtils.deleteDirectory(vlcFolder);
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                        TopDialog.showMsg("Failed to clear media cache.\nPlease close any and all windows playing cached media and try again.", "Error!", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        });
        
        // -------------------- Updates Panel --------------------
        final BetterLabel lblAppUpdateTitle = new BetterLabel("Application Updates", headerFont);
        final BetterLabel lblAppUpdateDesc  = new BetterLabel(PiPPropertyDesc.APP_UPDATES, textFont);
        final BetterLabel lblAppUpdateTypeTitle = new BetterLabel("Application Update Builds:", titleFont);
        comboAppUpdateType = new BetterComboBox(new String[] {"🔥 Release", "🔨 Beta", "🛠️ Snapshot"}, titleFont, (e) -> {
            final int selection = (int) ((BetterComboBox) e.getSource()).getSelectedIndex();
            final TYPE_OPTION type = switch (selection) {
            case 0  -> TYPE_OPTION.RELEASE;
            case 1  -> TYPE_OPTION.BETA;
            case 2  -> TYPE_OPTION.SNAPSHOT;
            default -> throw new IllegalArgumentException("Unexpected value: " + selection);
            };
            propertyChanged(PiPProperty.APP_UPDATE_TYPE, type.toString());
            ((BetterComboBox) e.getSource()).setToolTipText(type.description());
        });
        final BetterLabel lblAppUpdateFreqTitle = new BetterLabel("Automatically Check for Updates:", titleFont);
        comboAppUpdateFreq = new BetterComboBox(new String[] {"✔️ Always", "☀️ Daily", "📏 Weekly", "📅 Monthly", "❌ Never"}, titleFont, (e) -> {
            final int selection = (int) ((BetterComboBox) e.getSource()).getSelectedIndex();
            final FREQUENCY_OPTION frequency = switch (selection) {
            case 0  -> FREQUENCY_OPTION.ALWAYS;
            case 1  -> FREQUENCY_OPTION.DAILY;
            case 2  -> FREQUENCY_OPTION.WEEKLY;
            case 3  -> FREQUENCY_OPTION.MONTHLY;
            case 4  -> FREQUENCY_OPTION.NEVER;
            default -> throw new IllegalArgumentException("Unexpected value: " + selection);
            };
            propertyChanged(PiPProperty.APP_UPDATE_FREQUENCY, frequency.toString());
            ((BetterComboBox) e.getSource()).setToolTipText(frequency.description());
        });
        chkForceAppUpdate = new BetterCheckbox("Force Update Prompt:", true, titleFont);
        chkForceAppUpdate.addActionListener(e -> propertyChanged(PiPProperty.APP_UPDATE_FORCE, Boolean.toString(((BetterCheckbox) e.getSource()).isSelected())));
        chkForceAppUpdate.setToolTipText("Force the application to ask if you'd like to update, even if the version is the same. Only with manual updates via the button.");
        chkForceAppUpdate.setHorizontalTextPosition(SwingConstants.LEADING);
        chkForceAppUpdate.setIconTextGap(107);
        final BetterButton btnUpdateApp = new BetterButton("Check for Application Update", titleFont, (e) -> {
            // Do not stack update requests.
            if (PiPUpdater.APP_UPDATING) return;
            PiPUpdater.APP_UPDATING = true;
            
            // Run off of the EDT.
            CompletableFuture.runAsync(() -> {
                final boolean force = propertyState(PiPProperty.APP_UPDATE_FORCE, Boolean.class);
                final PiPUpdateResult result = PiPUpdater.updateApp(PropDefault.TYPE.matchAny(propertyState(PiPProperty.APP_UPDATE_TYPE, String.class)), force);
                if (result.checked()) {
                    propertyChanged(PiPProperty.APP_LAST_UPDATE_CHECK, LocalDateTime.now().toString());
                    if (!result.userPrompted()) EasyTopDialog.showMsg(this, "No update available.", PropDefault.THEME.matchAny(propertyState(PiPProperty.THEME, String.class)), false);
                }
                if (result.updated()) {
                    propertyChanged(PiPProperty.APP_UPDATING_FROM, (force ? "FORCED-" : "") + Initializer.APP_BUILD.toString());
                    System.exit(0);
                }
                if (result.hasException()) System.err.println("Warning, app update process failed: " + result.exception().getTotalMessage());
                
                PiPUpdater.APP_UPDATING = false;
            });
        });
        final BetterLabel lblBinUpdateTitle = new BetterLabel("Updates for Binaries", headerFont);
        final BetterLabel lblBinUpdateDesc  = new BetterLabel(PiPPropertyDesc.BIN_UPDATES, textFont);
        final BetterLabel lblBinUpdateFreqTitle = new BetterLabel("Automatically Check for Updates:", titleFont);
        comboBinUpdateFreq = new BetterComboBox(new String[] {"✔️ Always", "☀️ Daily", "📏 Weekly", "📅 Monthly", "❌ Never"}, titleFont, (e) -> {
            final int selection = (int) ((BetterComboBox) e.getSource()).getSelectedIndex();
            final FREQUENCY_OPTION frequency = switch (selection) {
            case 0  -> FREQUENCY_OPTION.ALWAYS;
            case 1  -> FREQUENCY_OPTION.DAILY;
            case 2  -> FREQUENCY_OPTION.WEEKLY;
            case 3  -> FREQUENCY_OPTION.MONTHLY;
            case 4  -> FREQUENCY_OPTION.NEVER;
            default -> throw new IllegalArgumentException("Unexpected value: " + selection);
            };
            propertyChanged(PiPProperty.BIN_UPDATE_FREQUENCY, frequency.toString());
            ((BetterComboBox) e.getSource()).setToolTipText(frequency.description());
        });
        final BetterButton btnUpdateBin = new BetterButton("Update Downloader Binaries", titleFont, (e) -> {
            // Do not stack update requests.
            if (PiPUpdater.BIN_UPDATING) return;
            PiPUpdater.BIN_UPDATING = true;
            
            // Run off of the EDT.
            CompletableFuture.runAsync(() -> {
                final String res = Binaries.updateAll();
                
                // Update last update check time.
                propertyChanged(PiPProperty.BIN_LAST_UPDATE_CHECK, LocalDateTime.now().toString());
                
                // Show Result
                TopDialog.showMsg(res, "Update Result", JOptionPane.INFORMATION_MESSAGE);
                
                PiPUpdater.BIN_UPDATING = false;
            });
        });
        
        // -------------------- Advanced Panel --------------------
        chkSystemBin = new BetterCheckbox("⚙️ Use System Binaries", false, titleFont);
        chkSystemBin.addActionListener((e) -> propertyChanged(PiPProperty.USE_SYS_BINARIES, Boolean.toString(((BetterCheckbox) e.getSource()).isSelected())));
        final BetterLabel lblSystemBin  = new BetterLabel(PiPPropertyDesc.USE_SYS_BINARIES, textFont);
        
        // Load Current Configuration
        refreshProperties();
        
        // Add Components to Content Panes
        contentPane.add(header, "dock north, h 30:50, gapleft push, gapright push, wrap 5px");
        contentPane.add(tabbedPane, "growy");
        // General Pane
        paneGeneral.add(comboTheme, "gaptop 5px, split 2, w 80%");
        paneGeneral.add(lblThemeTitle, "span, wrap 0px");
        paneGeneral.add(chkPreferLinkDND, "gaptop 5px, wrap 0px");
        paneGeneral.add(lblPreferLinkDND, "wrap 5px");
        paneGeneral.add(comboDLWebMedia, "gaptop 5px, split 2, w 55%");
        paneGeneral.add(lblDLWebMediaTitle, "span, wrap 4px");
        paneGeneral.add(lblDLWebMedia, "wrap");
        paneGeneral.add(chkConvertIndWeb, "wrap 0px");
        paneGeneral.add(lblConvIndWeb, "wrap 5px");
        paneGeneral.add(chkTrimTransparency, "split 2, w 70%");
        paneGeneral.add(comboTrim, "w 30%, wrap 0px");
        paneGeneral.add(lblTrimTransparency, "wrap 5px");
        paneGeneral.add(btnOpenFolder, "gaptop 15px, span, w 100%, h pref!, wrap");
        paneGeneral.add(btnShowShortcuts, "span, w 100%, h pref!, wrap");
        // Playback Pane
        panePlayback.add(comboGIFPlayback, "gaptop 5px, split 2, w 50%");
        panePlayback.add(lblGIFPlaybackTitle, "span, wrap 4px");
        panePlayback.add(lblGIFPlayback, "wrap");
        panePlayback.add(chkGlobMute, "wrap 0px");
        panePlayback.add(lblGlobMute, "wrap");
        panePlayback.add(lblDefVolTitle, "aligny top, split 2");
        panePlayback.add(sliderDefVol, "gapleft push, w 100:150:150, wrap 0px");
        panePlayback.add(lblDefVol, "wrap");
        panePlayback.add(lblDefRateTitle, "aligny top, split 2");
        panePlayback.add(sliderDefRate, "gapleft push, w 100:150:150, wrap 0px");
        panePlayback.add(lblDefRate, "wrap 15px");
        // Cache Pane
        paneCache.add(chkDisCache, "wrap 0px");
        paneCache.add(lblDisCache, "wrap");
        paneCache.add(comboOverwriteCache, "gaptop 5px, split 2, w 40%");
        paneCache.add(lblOverwriteCacheTitle, "span, wrap 4px");
        paneCache.add(lblAskCache, "wrap");
        paneCache.add(lblMediaCacheTitle, "gaptop 5px, wrap 0px");
        paneCache.add(lblMediaCache, "wrap");
        paneCache.add(btnOpenCache, "gaptop 0px, split 3, w 100%, h pref!");
        paneCache.add(btnPruneCache, "w 100%, h pref!");
        paneCache.add(btnClearCache, "w 100%, h pref!, wrap");
        paneCache.add(lblVLCCacheTitle, "gaptop 5px, wrap 0px");
        paneCache.add(lblVLCCache, "wrap");
        paneCache.add(btnOpenVLCCache, "gaptop 0px, split 2, w 100%, h pref!");
        paneCache.add(btnClearVLCCache, "w 100%, h pref!, wrap");
        // Updates Pane
        paneUpdates.add(lblAppUpdateTitle, "alignx center, wrap 5px");
        paneUpdates.add(lblAppUpdateDesc, "wrap");
        paneUpdates.add(lblAppUpdateTypeTitle, "split 2, w 60%");
        paneUpdates.add(comboAppUpdateType, "gaptop 5px, w 40%, wrap 4px");
        paneUpdates.add(lblAppUpdateFreqTitle, "split 2, w 60%");
        paneUpdates.add(comboAppUpdateFreq, "gaptop 5px, w 40%, wrap 4px");
        paneUpdates.add(chkForceAppUpdate, "x 3px, span, wrap 4px");
        paneUpdates.add(btnUpdateApp, "gaptop 5px, span, w 100%, h pref!, wrap 30px");
        paneUpdates.add(lblBinUpdateTitle, "alignx center, wrap");
        paneUpdates.add(lblBinUpdateDesc, "wrap");
        paneUpdates.add(lblBinUpdateFreqTitle, "split 2, w 60%");
        paneUpdates.add(comboBinUpdateFreq, "gaptop 5px, w 40%, wrap 4px");
        paneUpdates.add(btnUpdateBin, "gaptop 5px, span, w 100%, h pref!, wrap");
        // Advanced Pane
        paneAdvanced.add(chkSystemBin, "gaptop 5px, wrap 0px");
        paneAdvanced.add(lblSystemBin, "wrap");
    }
    
    /**
     * Generates and returns an updated text representation for the default volume
     * title. This method will get the current value of the default volume slider
     * and properly format it within the title.
     * 
     * @return a String with the up-to-date text.
     */
    private String volTitleTxt() {
        final int volume = sliderDefVol.getValue();
        String emoji = " 🔈 ";
        if (volume >= 75) {
            emoji = "🔊";
        } else if (volume > 25) {
            emoji = "🔉";
        }
        
        return emoji + " Default Volume: " + volume + "%";
    }
    
    /**
     * Generates and returns an updated text representation for the default playback
     * rate title. This method will get the current value of the default playback
     * rate slider and properly format it within the title.
     * 
     * @return a String with the up-to-date text.
     */
    private String rateTitleTxt() {
        final int rate = sliderDefRate.getValue();
        String emoji = "🐢";
        if (rate >= 200) {
            emoji = "🐇";
        } else if (rate > 50) {
            emoji = "⏱️";
        }
        
        return emoji + " Default Speed: " + (rate / 100.0f) + "x";
    }
    
    /**
     * Themes the components contained within the configuration window.
     * This method sets the proper colors for the components based on the theme.
     * It does not run on the EDT automatically, but it should be called on the EDT.
     * 
     * @param darkMode - a boolean for whether or not dark mode is enabled.
     * @param jc - the parent component to theme, along with all of its children.
     */
    private void themeComponents(THEME_OPTION theme, JComponent jc) {
        Color colorBG, colorFG, colorBord, colorBtnFG, colorBtn1, colorBtn2, colorBtn3, colorSli, colorSliE;
        colorBG    = theme.color(COLOR.BG);
        colorBord  = theme.color(COLOR.BG_ACCENT);
        colorFG    = theme.color(COLOR.TXT);
        colorBtnFG = Color.WHITE;
        colorBtn1  = theme.color(COLOR.BTN);
        colorBtn2  = theme.color(COLOR.BTN_PRESSED);
        colorBtn3  = theme.color(COLOR.BTN_BORDER);
        colorSli   = theme.color(COLOR.SLIDER);
        colorSliE  = theme.color(COLOR.SLIDER_EMPTY);
        
        if (jc instanceof BetterPanel panel) {
            panel.setBackground(colorBG);
            panel.setBorderColor(colorBord);
        }
        
        for (final Component c : jc.getComponents()) {
            if (c instanceof BetterButton button) {
                button.setColors(colorBtn1, colorBtn2, colorBtn3);
                button.setForeground(colorBtnFG);
                continue;
            }
            else if (c instanceof BetterComboBox combo) {
                combo.setForeground(colorFG);
                combo.setSelectionForeground(colorBtn1);
                combo.setBoxColor(colorBG);
                combo.setBoxBorderColor(colorBord);
                continue;
            }
            else if (c instanceof BetterSlider slider) {
                slider.setSliderColor(colorSli);
                slider.setSliderEmptyColor(colorSliE);
                slider.setSliderThumbColor(colorBtn1);
                continue;
            }
            c.setBackground(colorBG);
            c.setForeground(colorFG);
        }
    }
    
    @Override
    public void pickTheme(THEME_OPTION theme) {
        themeComponents(theme, contentPane);
        themeComponents(theme, paneGeneral);
        themeComponents(theme, panePlayback);
        themeComponents(theme, paneCache);
        themeComponents(theme, paneUpdates);
        themeComponents(theme, paneAdvanced);
        this.repaint();
        this.revalidate();
    }
    
    /**
     * Handles the change of the passed property and reflects the updated value in
     * the ConfigWindow. This method will use the ConfigWindow's PropertyListener to
     * pull the current property state. Therefore, one must update that property's
     * state <i>prior</i> to calling this method in order for the change to be
     * reflected in the ConfigWindow.
     * <p>
     * This method <b>does not</b> automatically run on the EDT. You should enclose
     * this method call within a <code>SwingUtilities.invokeLater(Runnable)</code>
     * block if necessary.
     * 
     * @param prop - the PiPProperty to handle a change for.
     */
    public void handlePropertyChange(PiPProperty prop) {
        switch(prop) {
        case THEME: {
            final THEME_OPTION theme = PropDefault.THEME.matchAny(propertyState(prop, String.class));
            comboTheme.setSelectedIndex(switch (theme) {
            case LIGHT      -> 0;
            case DARK       -> 1;
            case PINK       -> 2;
            case SUBNAUTICA -> 3;
            });
            pickTheme(theme);
            break;
        }
        case GIF_PLAYBACK_MODE:
            final PLAYBACK_OPTION playback = PropDefault.PLAYBACK.matchAny(propertyState(prop, String.class));
            comboGIFPlayback.setSelectedIndex(switch (playback) {
            case BASIC    -> 0;
            case ADVANCED -> 1;
            });
            comboGIFPlayback.setToolTipText(playback.description());
            break;
        case DND_PREFER_LINK:
            chkPreferLinkDND.setSelected(propertyState(prop, Boolean.class));
            break;
        case DOWNLOAD_WEB_MEDIA:
            final DOWNLOAD_OPTION download = PropDefault.DOWNLOAD.matchAny(propertyState(prop, String.class));
            comboDLWebMedia.setSelectedIndex(switch (download) {
            case NEVER  -> 0;
            case NORMAL -> 1;
            case ALWAYS -> 2;
            });
            comboDLWebMedia.setToolTipText(download.description());
            break;
        case CONVERT_WEB_INDIRECT:
            chkConvertIndWeb.setSelected(propertyState(prop, Boolean.class));
            break;
        case TRIM_TRANSPARENCY:
            chkTrimTransparency.setSelected(propertyState(prop, Boolean.class));
            break;
        case TRIM_TRANSPARENCY_OPTION:
            final TRIM_OPTION option = PropDefault.TRIM.matchAny(propertyState(prop, String.class));
            comboTrim.setSelectedIndex(switch (option) {
            case NORMAL -> 0;
            case STRICT -> 1;
            case FORCE  -> 2;
            });
            comboTrim.setToolTipText(option.description());
            break;
        case GLOBAL_MUTED:
            chkGlobMute.setSelected(propertyState(prop, Boolean.class));
            break;
        case DEFAULT_VOLUME:
            sliderDefVol.setValue(propertyState(prop, Integer.class));
            lblDefVolTitle.setText(volTitleTxt());
            break;
        case DEFAULT_PLAYBACK_RATE:
            sliderDefRate.setValue((int) (propertyState(PiPProperty.DEFAULT_PLAYBACK_RATE, Float.class) * 100));
            lblDefRateTitle.setText(rateTitleTxt());
            break;
        case DISABLE_CACHE:
            chkDisCache.setSelected(propertyState(prop, Boolean.class));
            break;
        case OVERWRITE_CACHE:
            final OVERWRITE_OPTION overwrite = PropDefault.OVERWRITE.matchAny(propertyState(prop, String.class));
            comboOverwriteCache.setSelectedIndex(switch (overwrite) {
            case ASK -> 0;
            case YES -> 1;
            case NO  -> 2;
            });
            break;
        case APP_UPDATE_FREQUENCY:
            final FREQUENCY_OPTION appFrequency = PropDefault.FREQUENCY_APP.matchAny(propertyState(prop, String.class));
            comboAppUpdateFreq.setSelectedIndex(switch (appFrequency) {
            case ALWAYS  -> 0;
            case DAILY   -> 1;
            case WEEKLY  -> 2;
            case MONTHLY -> 3;
            case NEVER   -> 4;
            });
            comboAppUpdateFreq.setToolTipText(appFrequency.description());
            break;
        case APP_UPDATE_TYPE:
            final TYPE_OPTION type = PropDefault.TYPE.matchAny(propertyState(prop, String.class));
            comboAppUpdateType.setSelectedIndex(switch (type) {
            case RELEASE  -> 0;
            case BETA     -> 1;
            case SNAPSHOT -> 2;
            });
            comboAppUpdateType.setToolTipText(type.description());
            break;
        case APP_UPDATE_FORCE:
            chkForceAppUpdate.setSelected(propertyState(prop, Boolean.class));
            break;
        case BIN_UPDATE_FREQUENCY:
            final FREQUENCY_OPTION binFrequency = PropDefault.FREQUENCY_BIN.matchAny(propertyState(prop, String.class));
            comboBinUpdateFreq.setSelectedIndex(switch (binFrequency) {
            case ALWAYS  -> 0;
            case DAILY   -> 1;
            case WEEKLY  -> 2;
            case MONTHLY -> 3;
            case NEVER   -> 4;
            });
            comboBinUpdateFreq.setToolTipText(binFrequency.description());
            break;
        case USE_SYS_BINARIES:
            chkSystemBin.setSelected(propertyState(prop, Boolean.class));
            break;
        // Do Nothing
        case APP_LAST_UPDATE_CHECK:
        case APP_UPDATING_FROM:
        case BIN_LAST_UPDATE_CHECK:
        case SET_ALL_MUTED:
        case SET_ALL_PAUSED:
        case SET_ALL_PLAYBACK_RATE:
        case SET_ALL_VOLUME:
            break;
        }
    }
    
    /**
     * Refreshes the display of the properties within the ConfigWindow. Gets all of
     * the current property values and ensures that the window reflects their state.
     * <p>
     * This method automatically runs the refresh code on the EDT, so <b>there is no
     * need</b> to call this method within a
     * <code>SwingUtilities.invokeLater(Runnable)</code> block.
     */
    public void refreshProperties() {
        SwingUtilities.invokeLater(() -> {
            // Iterate over each property and refresh it.
            for (final PiPProperty prop : PiPProperty.values()) handlePropertyChange(prop);
        });
    }

    // To Be Overriden
    @Override
    public void propertyChanged(PiPProperty prop, String value) {}
    @Override
    public <T> T propertyState(PiPProperty prop, Class<T> rtnType) { return null; }
}
