package dev.mwhitney.main;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.MenuItemUI;
import javax.swing.plaf.PopupMenuUI;
import javax.swing.plaf.SeparatorUI;

import dev.mwhitney.gui.ConfigWindow;
import dev.mwhitney.gui.PiPWindowManager;
import dev.mwhitney.gui.decor.ColoredArrowIcon;
import dev.mwhitney.gui.decor.InvertibleImage;
import dev.mwhitney.gui.popup.TopDialog;
import dev.mwhitney.listeners.PiPTrayAdapter;
import dev.mwhitney.listeners.PropertyListener;
import dev.mwhitney.main.Binaries.Bin;
import dev.mwhitney.main.PiPProperty.PropDefault;
import dev.mwhitney.main.PiPProperty.THEME_OPTION;
import dev.mwhitney.main.PiPProperty.THEME_OPTION.COLOR;
import dev.mwhitney.resources.AppRes;
import dorkbox.systemTray.Checkbox;
import dorkbox.systemTray.Entry;
import dorkbox.systemTray.Menu;
import dorkbox.systemTray.MenuItem;
import dorkbox.systemTray.Separator;
import dorkbox.systemTray.SystemTray;
import dorkbox.systemTray.ui.swing.SwingUIFactory;
import dorkbox.systemTray.util.HeavyCheckMark;
import dorkbox.systemTray.util.SizeAndScalingWindows;
import dorkbox.systemTray.util.swing.DefaultMenuItemUI;
import dorkbox.systemTray.util.swing.DefaultPopupMenuUI;
import dorkbox.systemTray.util.swing.DefaultSeparatorUI;

/**
 * Controls the tray icon, context menu, and other related operations.
 * 
 * @author mwhitney57
 */
public class Tray implements PropertyListener {
    
    @SuppressWarnings("unused")
    /** The default background color for tray elements. */
    private static final Color DEFAULT_BG_COLOR   = AppRes.COLOR_OFF_WHITE;
    /** The default shadow color for tray elements. */
    private static final Color DEFAULT_SHDW_COLOR = new Color(160, 160, 160);
    /** The default check mark color. */
    private static final Color DEFAULT_CHCK_COLOR = new Color(8, 144, 0);
    
    /** The tray {@link ImageIcon}, which uses a higher, 32x32 resolution. */
    private static final ImageIcon TRAY_IMAGEICON_32 = AppRes.IMGICON_APP_32;
    /** The tray image in normal, 16x16 resolution. */
    private static final Image     TRAY_IMAGE        = AppRes.IMG_APP_16;

    /** The configuration window that can be opened from the tray. */
    private ConfigWindow configWin;
    
    /** The tray object for configuring and displaying the tray icon and its menus. */
    private SystemTray tray;
    /** The tray's context menu that is opened upon receiving click input(s). */
    private Menu menu;
    /** A nested HashMap containing themes with their respective property keys and values. */
    private HashMap<String, HashMap<String, Object>> uiPropsMap;
    /** A map between icon-containing Menu or MenuItem entries and their respective InvertableImages. */
    private HashMap<Entry, InvertibleImage> imgMap;
    /** A list of Swing components linked to entries within the tray's context menu. */
    private List<JComponent> itemComps;
    /** The menu checkbox for enabling/disabling global mute. */
    private Checkbox entryGlobalMute;

    /** The listener for the tray which communicates back up and to other objects, primarily a {@link PiPWindowManager}. */
    private PiPTrayAdapter listener;

    public Tray() {
        // UI Theme Properties Setup and Refresh
        setupUIThemeProperties();
        SwingUtilities.invokeLater(() -> refreshUIThemeProperties());
        
        // Setup Swing Component Peers (?) of Tray Components
        itemComps = new ArrayList<JComponent>();
        
        // Dorkbox SystemTray Debug Statement
        SystemTray.DEBUG = true;
        // Dorkbox SystemTray Menu and Icon Scaling
        SizeAndScalingWindows.OVERRIDE_MENU_SIZE = 20;
        SizeAndScalingWindows.OVERRIDE_TRAY_SIZE = 32;
        // Dorkbox SystemTray Swing - Save Menus, Items, and Separators for Later (For Theme Changes)
        SystemTray.SWING_UI = new SwingUIFactory() {
            @Override
            public PopupMenuUI getMenuUI(JPopupMenu jPopupMenu, Menu entry) {
                itemComps.add(jPopupMenu);
                return new DefaultPopupMenuUI(jPopupMenu);
            }

            @Override
            public MenuItemUI getItemUI(JMenuItem jMenuItem, Entry entry) {
                itemComps.add(jMenuItem);
                return new DefaultMenuItemUI(jMenuItem);
            }

            @Override
            public SeparatorUI getSeparatorUI(JSeparator jSeparator) {
                itemComps.add(jSeparator);
                return new DefaultSeparatorUI(jSeparator);
            }

            @Override
            public String getCheckMarkIcon(Color color, int checkMarkSize, int targetImageSize) {
                color = DEFAULT_CHCK_COLOR;
                checkMarkSize = 16;
                return HeavyCheckMark.get(color, checkMarkSize, targetImageSize);
            }
        };

        // Setup Configuration Window
        SwingUtilities.invokeLater(() -> {
            configWin = new ConfigWindow("PiPAA Configuration") {
                /** The randomly-generated, unique serial ID for ConfigWindows. */
                private static final long serialVersionUID = -1347533618715414116L;
                
                @Override
                public void propertyChanged(PiPProperty prop, String value) {
                    // Forward property change update all the way up before pulling the new value cleanly as the right type.
                    Tray.this.propertyChanged(prop, value);
                    
                    // Update relevant tray buttons/states to reflect property change.
                    switch(prop) {
                    case THEME        -> CompletableFuture.runAsync(() -> refreshUITheme());
                    case GLOBAL_MUTED -> SwingUtilities.invokeLater(() -> {
                            entryGlobalMute.setChecked(propertyState(prop, Boolean.class));
                            entryGlobalMute.setText("Global Mute " + (propertyState(prop, Boolean.class) ? "Enabled" : "Disabled"));
                        });
                    default           -> {}
                    }
                }
                @Override
                public <T> T propertyState(PiPProperty prop, Class<T> rtnType) { return Tray.this.propertyState(prop, rtnType); }
            };
        });
        
        // Get System Tray
        tray = SystemTray.get();
        if (tray == null) throw new RuntimeException("Unable to get the system tray!");
        
        // Create Context Menu
        menu = tray.getMenu();
        
        // Setup Tray Menu and its Items (Adds them to Menu)
        setupTrayMenuItems();
        
        // Load Current Configuration
        entryGlobalMute.setChecked(Boolean.valueOf(this.propertyState(PiPProperty.GLOBAL_MUTED, String.class)));
        entryGlobalMute.setText("Global Mute " + (entryGlobalMute.getChecked() ? "Enabled" : "Disabled"));

        // Add Tray Icon to System Tray
        tray.setImage(TRAY_IMAGE);
        tray.setTooltip(AppRes.APP_NAME);
        tray.setStatus("Running Windows: 1");
    }
    
    /**
     * Performs setup for all entries within the Tray Menu.
     * <b>This setup is only intended to be performed once.</b>
     * After performing setup, this method will add each item to the <code>menu</code>.
     */
    private void setupTrayMenuItems() {
        final MenuItem aboutItem = new MenuItem("About", ((evt) -> {
            System.out.println("EDT? " + SwingUtilities.isEventDispatchThread());
            TopDialog.showMsg(AppRes.APP_NAME + "\nVersion: " + AppRes.APP_BUILD + "\n\n" + String.format("""
                    Packaged with:
                    - LibVlc      [%s] (video/advanced GIF playback)
                    - yt-dlp      [%s] (media downloads)
                    - gallery-dl  [%s] (IMG/GIF media downloads)
                    - ffmpeg      [%s] (GIF conversions)
                    - ImageMagick [%s] (IMG/GIF conversions)
                    
                    Tray Menu Icons by:
                    Icons8 @ icons8.com
                    """, AppRes.VERS_VLC, Bin.YT_DLP.version(), Bin.GALLERY_DL.version(), Bin.FFMPEG.version(), Bin.IMGMAGICK.version()),
                    "PiPAA Info", JOptionPane.INFORMATION_MESSAGE, TRAY_IMAGEICON_32);
        }));
        final MenuItem configItem = new MenuItem("Config...",   ((evt) -> SwingUtilities.invokeLater(() -> configWin.setVisible(true))));
        final Menu globalItem     = new Menu("Global");
        entryGlobalMute           = new Checkbox("Global Mute", ((evt) -> {
            final Checkbox srcItem = ((Checkbox) evt.getSource());
            srcItem.setText("Global Mute " + (srcItem.getChecked() ? "Enabled" : "Disabled"));
            this.propertyChanged(PiPProperty.GLOBAL_MUTED, Boolean.toString(srcItem.getChecked()));
            SwingUtilities.invokeLater(() -> configWin.handlePropertyChange(PiPProperty.GLOBAL_MUTED));
        }));
        final MenuItem setAllPauseItem    = new MenuItem("Pause All Windows",  ((evt) -> this.propertyChanged(PiPProperty.SET_ALL_PAUSED, "true")));
        final MenuItem setAllPlayItem     = new MenuItem("Play All Windows",   ((evt) -> {
            // Only play all windows if Single Play Mode is disabled.
            if (!this.propertyState(PiPProperty.SINGLE_PLAY_MODE, Boolean.class))
                this.propertyChanged(PiPProperty.SET_ALL_PAUSED, "false");
        }));
        final MenuItem setAllMuteItem     = new MenuItem("Mute All Windows",   ((evt) -> this.propertyChanged(PiPProperty.SET_ALL_MUTED,  "true")));
        final MenuItem setAllUnmuteItem   = new MenuItem("Unmute All Windows", ((evt) -> this.propertyChanged(PiPProperty.SET_ALL_MUTED, "false")));
        final MenuItem setAllVolumeItem   = new MenuItem("Volume for All Windows...", ((evt) -> {
            final String userInput = TopDialog.showInput("Set volume for all windows to...\nEx: 75");
            this.propertyChanged(PiPProperty.SET_ALL_VOLUME, userInput);
        }));
        final MenuItem setAllPlaybackItem = new MenuItem("Playback Rate for All Windows...", ((evt) -> {
            final String userInput = TopDialog.showInput("Set playback rate for all windows to...\nEx: 1.50");
            this.propertyChanged(PiPProperty.SET_ALL_PLAYBACK_RATE, userInput);
        }));
        final MenuItem minAllWindowsItem  = new MenuItem("Minimize All Windows", ((evt) -> listener.minimizeWindows()));
        final MenuItem restAllWindowsItem = new MenuItem("Restore All Windows",  ((evt) -> listener.restoreWindows()));
        final MenuItem hideAllWindowsItem = new MenuItem("Hide All Windows",     ((evt) -> listener.hideWindows()));
        final MenuItem showAllWindowsItem = new MenuItem("Show All Windows",     ((evt) -> listener.showWindows()));
        final MenuItem addItem            = new MenuItem("Add Window",    ((evt) -> listener.addWindow()));
        final MenuItem removeItem         = new MenuItem("Remove Window", ((evt) -> listener.removeWindow()));
        final MenuItem clearItem          = new MenuItem("Clear Windows", ((evt) -> listener.clearWindows()));
        final MenuItem exitItem           = new MenuItem("Exit",          ((evt) -> {
            // Exit/Close Application
            listener.applicationClosing();
            tray.shutdown(() -> System.exit(0));
            /* 
             * TODO Rework threading throughout app, implement exit call in a listener that certain classes implement.
             * Bad practice to call System.exit(...) on normal shutdowns. This is why tray icon lingers until hovered over afterwards.
             * Commenting it out works, UNLESS an async thread is executing (non-daemon), such as downloading media. In that case the app
             * stays running completely in the background, with no tray icon or windows, which is obviously terrible. All async thread
             * calls must be interruptible at user-called application exit.
             * Update (Nov. 25, 2024): Moved System.exit(0) call within the SystemTray shutdown method call. It executes after the tray has shut down.
             * Therefore, the tray icon properly goes away on its own now without having to hover over it after exit.
             * However, this is just a partial, temporary improvement. The other issues described above remain, which must be fixed.
             */
        }));
        
        // Context Menu Item Configuration
        // Context Menu Item Icon Mappings
        imgMap = new HashMap<Entry, InvertibleImage>();
        imgMap.put(aboutItem,          new InvertibleImage(Toolkit.getDefaultToolkit().getImage(Tray.class.getResource(AppRes.ICON_TRAY_INFO))));
        imgMap.put(configItem,         new InvertibleImage(Toolkit.getDefaultToolkit().getImage(Tray.class.getResource(AppRes.ICON_TRAY_CONFIG))));
        imgMap.put(globalItem,         new InvertibleImage(Toolkit.getDefaultToolkit().getImage(Tray.class.getResource(AppRes.ICON_TRAY_GLOBE))));
        imgMap.put(setAllPauseItem,    new InvertibleImage(Toolkit.getDefaultToolkit().getImage(Tray.class.getResource(AppRes.ICON_TRAY_PAUSE))));
        imgMap.put(setAllPlayItem,     new InvertibleImage(Toolkit.getDefaultToolkit().getImage(Tray.class.getResource(AppRes.ICON_TRAY_PLAY))));
        imgMap.put(setAllMuteItem,     new InvertibleImage(Toolkit.getDefaultToolkit().getImage(Tray.class.getResource(AppRes.ICON_TRAY_MUTE))));
        imgMap.put(setAllUnmuteItem,   new InvertibleImage(Toolkit.getDefaultToolkit().getImage(Tray.class.getResource(AppRes.ICON_TRAY_UNMUTE))));
        imgMap.put(setAllVolumeItem,   new InvertibleImage(Toolkit.getDefaultToolkit().getImage(Tray.class.getResource(AppRes.ICON_TRAY_AUDIO))));
        imgMap.put(setAllPlaybackItem, new InvertibleImage(Toolkit.getDefaultToolkit().getImage(Tray.class.getResource(AppRes.ICON_TRAY_PLAYBACK))));
        imgMap.put(minAllWindowsItem,  new InvertibleImage(Toolkit.getDefaultToolkit().getImage(Tray.class.getResource(AppRes.ICON_TRAY_MINIMIZE))));
        imgMap.put(restAllWindowsItem, new InvertibleImage(Toolkit.getDefaultToolkit().getImage(Tray.class.getResource(AppRes.ICON_TRAY_RESTORE))));
        imgMap.put(hideAllWindowsItem, new InvertibleImage(Toolkit.getDefaultToolkit().getImage(Tray.class.getResource(AppRes.ICON_TRAY_HIDE))));
        imgMap.put(showAllWindowsItem, new InvertibleImage(Toolkit.getDefaultToolkit().getImage(Tray.class.getResource(AppRes.ICON_TRAY_SHOW))));
        imgMap.put(addItem,            new InvertibleImage(Toolkit.getDefaultToolkit().getImage(Tray.class.getResource(AppRes.ICON_TRAY_ADD))));
        imgMap.put(removeItem,         new InvertibleImage(Toolkit.getDefaultToolkit().getImage(Tray.class.getResource(AppRes.ICON_TRAY_REMOVE))));
        imgMap.put(clearItem,          new InvertibleImage(Toolkit.getDefaultToolkit().getImage(Tray.class.getResource(AppRes.ICON_TRAY_CLEAR))));
        imgMap.put(exitItem,           new InvertibleImage(Toolkit.getDefaultToolkit().getImage(Tray.class.getResource(AppRes.ICON_TRAY_EXIT))));
        refreshUIThemeIcons();
        
        // Add Menu Items to Global Menu within the Context Menu
        globalItem.add(entryGlobalMute);
        globalItem.add(new Separator());
        globalItem.add(setAllPauseItem);
        globalItem.add(setAllPlayItem);
        globalItem.add(setAllMuteItem);
        globalItem.add(setAllUnmuteItem);
        globalItem.add(setAllVolumeItem);
        globalItem.add(setAllPlaybackItem);
        globalItem.add(new Separator());
        globalItem.add(minAllWindowsItem);
        globalItem.add(restAllWindowsItem);
        globalItem.add(hideAllWindowsItem);
        globalItem.add(showAllWindowsItem);
        
        // Add Menu Items to Context Menu
        menu.add(aboutItem).setShortcut('i');
        menu.add(configItem).setShortcut('c');
        menu.add(globalItem).setShortcut('g');
        menu.add(new Separator());
        menu.add(addItem).setShortcut('a');
        menu.add(removeItem).setShortcut('r');
        menu.add(clearItem).setShortcut('c');
        menu.add(new Separator());
        menu.add(exitItem).setShortcut('e');
    }
    
    /**
     * Performs setup for the configuration window.
     * This method is only intended to be executed once.
     * However, running it again should not produce any errors.
     */
//    private void setupConfigWindow() {
//    }
    
    /**
     * Performs setup for UI themes and their properties. This method should be
     * called prior to any UI refreshes, as it may be necessary in order for those
     * calls to execute. This method <b>should not</b> be called on the EDT.
     */
    private void setupUIThemeProperties() {
        // Initialize Props Map
        uiPropsMap = new HashMap<>();
        // Translate each theme and its colors into a map, then add it to the props map.
        for(final THEME_OPTION theme : THEME_OPTION.values()) {
            final HashMap<String, Object> themeMap = new HashMap<>();
            themeMap.put("Menu.background", new ColorUIResource(theme.color(COLOR.BG)));
            themeMap.put("Menu.foreground", theme.color(COLOR.TXT));
            themeMap.put("Menu.arrowIcon", new ColoredArrowIcon(theme.color(COLOR.TXT)));
            themeMap.put("PopupMenu.border", new LineBorder(theme.color(COLOR.BG_ACCENT), 2));
            themeMap.put("MenuItem.background", new ColorUIResource(theme.color(COLOR.BG)));
            themeMap.put("MenuItem.foreground", theme.color(COLOR.TXT));
            themeMap.put("Separator.background", theme.color(COLOR.BG));
            themeMap.put("Separator.shadow", new ColorUIResource(DEFAULT_SHDW_COLOR));
            themeMap.put("TabbedPane.tabAreaBackground", new ColorUIResource(theme.color(COLOR.BG)));
            uiPropsMap.put(theme.toString(), themeMap);
        }
    }
    
    /**
     * Refreshes the Tray UI to match the current theme. This method will first
     * ensure the Look and Feel properties match, then it will update the user
     * interface with those confirmed properties. It also specifically sets
     * properties of UI components that don't tend to respect the Look and Feel.
     */
    private void refreshUITheme() {
        // First, update the properties based on the current mode.
        refreshUIThemeProperties();
        // Then, refresh the Tray Menu images/icons.
        refreshUIThemeIcons();
        
        // Finally, Update UI and Apply Refreshed Properties
        SwingUtilities.invokeLater(() -> {
            final Border border   = (Border) UIManager.getLookAndFeelDefaults().get("PopupMenu.border");
            final Color text      = (Color)  UIManager.getLookAndFeelDefaults().get("MenuItem.foreground");
            final Color separator = (Color)  UIManager.getLookAndFeelDefaults().get("Separator.background");
            
            itemComps.forEach((e) -> {
                // Component-Specific Properties that Do Not Properly Set/Update While App is Running
                if (e instanceof JPopupMenu) {
                    e.setBorder(border);
                    
                    // Updates Component TREE, so only necessary to call on Menus.
                    SwingUtilities.updateComponentTreeUI(e);
                }
                else if (e instanceof JMenuItem)
                    e.setForeground(text);
                else if (e instanceof JSeparator)
                    e.setBackground(separator);
                else
                    // Backup UpdateUI Call.
                    SwingUtilities.updateComponentTreeUI(e);
            });
        });
    }
    
    /**
     * Refreshes the Tray UI's properties to match the current theme. This method
     * simply updates the Look and Feel defaults to match the current application
     * theme property.
     */
    private void refreshUIThemeProperties() {
        // Look and Feel Properties
        final THEME_OPTION theme = PropDefault.THEME.matchAny(propertyState(PiPProperty.THEME, String.class));
        
        // Get Theme's Map and Apply its Key/Values
        final HashMap<String, Object> themeMap = uiPropsMap.get(theme.toString());
        themeMap.forEach((k, v) -> UIManager.getLookAndFeelDefaults().put(k, v));
    }
    
    /**
     * Refreshes the Tray UI's icons to match the current theme. This method sets
     * the image of each applicable Menu or MenuItem to match the theme. It will
     * either set the regular version of the Image, or an inverse of it.
     */
    private void refreshUIThemeIcons() {
        // Grab image inverse for some themes. Regular image otherwise.
        final THEME_OPTION theme = PropDefault.THEME.matchAny(propertyState(PiPProperty.THEME, String.class));
        
        // Determine whether the image inverse should be used -- theme dependent.
        final boolean USE_INVERSE = theme.usesInvertedIcons();
        
        // Iterate over each image in the map and refresh its icon.
        imgMap.forEach((ent, img) -> {
            if (ent instanceof Menu menu)
                menu.setImage(USE_INVERSE ? img.inverse() : img.image());
            else if (ent instanceof MenuItem item)
                item.setImage(USE_INVERSE ? img.inverse() : img.image());
        });
    }
    
    /**
     * Updates this Tray's status within the context menu.
     * 
     * @param status - a String with the new status.
     */
    public void updateStatus(String status) {
        tray.setStatus(status);
    }

    /**
     * Sets the TrayListener used to send commands from.
     * 
     * @param listener - the TrayListener to use.
     */
    public void setTrayListener(PiPTrayAdapter listener) {
        this.listener = listener;
    }
    
    /**
     * Forwards the passed property change to the Tray and its ConfigWindow so that
     * they may update their visuals to respect the current property value. This
     * method is <b>not</b> intended to be called on the EDT. It is best to call it
     * off of the EDT for best performance.
     * 
     * @param prop  - the PiPProperty to handle a change for.
     * @param value - the new property value.
     */
    public void forwardPropertyChange(PiPProperty prop, String value) {
        configWin.propertyChanged(prop, value);
        SwingUtilities.invokeLater(() -> configWin.handlePropertyChange(prop));
    }
    
    // Must be Overridden.
    @Override
    public void propertyChanged(PiPProperty prop, String value) {}
    @Override
    public <T> T propertyState(PiPProperty prop, Class<T> rtnType) { return null; }
}
