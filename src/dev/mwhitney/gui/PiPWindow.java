package dev.mwhitney.gui;

import static dev.mwhitney.gui.PiPWindowState.StateProp.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.apache.commons.lang3.StringUtils;

import darrylbu.icon.StretchIcon;
import dev.mwhitney.gui.PiPWindowSnapshot.SnapshotData;
import dev.mwhitney.gui.binds.BindDetails;
import dev.mwhitney.gui.binds.BindHandler;
import dev.mwhitney.gui.binds.MouseInput;
import dev.mwhitney.gui.binds.Shortcut;
import dev.mwhitney.gui.components.better.BetterTextArea;
import dev.mwhitney.gui.decor.FadingLineBorder;
import dev.mwhitney.gui.decor.OffsetRoundedLineBorder;
import dev.mwhitney.gui.interfaces.Themed;
import dev.mwhitney.gui.popup.EasyTopDialog;
import dev.mwhitney.gui.popup.NumericalInputPopup;
import dev.mwhitney.gui.popup.OptionPopup;
import dev.mwhitney.gui.popup.SelectionPopup;
import dev.mwhitney.gui.popup.TopDialog;
import dev.mwhitney.gui.viewer.ZoomPanSnapshot;
import dev.mwhitney.listeners.ManagerFetcher;
import dev.mwhitney.listeners.PiPWindowManagerAdapter;
import dev.mwhitney.listeners.StartEndListener;
import dev.mwhitney.listeners.WindowClosingListener;
import dev.mwhitney.listeners.simplified.WindowFocusLostListener;
import dev.mwhitney.main.Binaries;
import dev.mwhitney.main.Binaries.Bin;
import dev.mwhitney.media.MediaExt;
import dev.mwhitney.media.PiPMedia;
import dev.mwhitney.media.PiPMediaAttributes;
import dev.mwhitney.media.PiPMediaAttributes.SRC_PLATFORM;
import dev.mwhitney.media.PiPMediaAttributor.Flag;
import dev.mwhitney.media.PiPMediaCMD;
import dev.mwhitney.media.PiPMediaCMDArgs;
import dev.mwhitney.media.WebMediaFormat;
import dev.mwhitney.media.WebMediaFormat.FORMAT;
import dev.mwhitney.media.exceptions.InvalidMediaException;
import dev.mwhitney.media.exceptions.MediaModificationException;
import dev.mwhitney.properties.PiPProperty;
import dev.mwhitney.properties.PropertyListener;
import dev.mwhitney.properties.PiPProperty.DOWNLOAD_OPTION;
import dev.mwhitney.properties.PiPProperty.OVERWRITE_OPTION;
import dev.mwhitney.properties.PiPProperty.PLAYBACK_OPTION;
import dev.mwhitney.properties.PiPProperty.PropDefault;
import dev.mwhitney.properties.PiPProperty.THEME_OPTION;
import dev.mwhitney.properties.PiPProperty.TRIM_OPTION;
import dev.mwhitney.properties.PiPProperty.THEME_OPTION.COLOR;
import dev.mwhitney.resources.AppRes;
import dev.mwhitney.util.Loop;
import dev.mwhitney.util.PiPAAUtils;
import dev.mwhitney.util.ScalingDimension;
import dev.mwhitney.util.UnsetBool;
import dev.mwhitney.util.interfaces.PermanentRunnable;
import dev.mwhitney.util.selection.ReloadSelection;
import dev.mwhitney.util.selection.ReloadSelection.ReloadSelections;
import dev.mwhitney.util.selection.Selector;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.media.AudioTrackInfo;
import uk.co.caprica.vlcj.media.Meta;
import uk.co.caprica.vlcj.media.MetaApi;
import uk.co.caprica.vlcj.media.VideoTrackInfo;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.base.State;
import uk.co.caprica.vlcj.player.base.TrackDescription;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.fullscreen.windows.Win32FullScreenStrategy;

/**
 * A picture-in-picture window capable of displaying media, in an always-on-top
 * state.
 * 
 * @author mwhitney57
 */
public class PiPWindow extends JFrame implements PropertyListener, Themed, ManagerFetcher<PiPWindowManager>, BindHandler {
    /** The randomly-generated serial UID for the PiPWindow class. */
    private static final long serialVersionUID = 6508277241367437180L;
    
    /** The default size (x and/or y) of media (at maximum) when first set. */
    public  static final int DEFAULT_MEDIA_SIZE = 480;
    /** The width (in px) of each side of the window's border. */
    public  static final int BORDER_SIZE = 20;
    /** The window insets where the user can drag and resize the window. */
    private static final Insets BORDER_RESIZE_INSETS  = new Insets(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE);
    /** The <b>absolute</b> minimum width and height of window content. The minimum size cannot adapt to display media at a lower size. */
    private static final int ABSOLUTE_MINIMUM_SIZE    = 4;
    /** The minimum width and height of window content. */
    public  static final int MINIMUM_SIZE_VALUE       = 64;
    /** The minimum size of PiPWindows. Includes window borders. */
    private static final Dimension MINIMUM_SIZE       = new Dimension(MINIMUM_SIZE_VALUE + (BORDER_SIZE*2), MINIMUM_SIZE_VALUE + (BORDER_SIZE*2));
    /** The default size of PiPWindows. */
    public  static final Dimension DEFAULT_SIZE       = new Dimension(320, 200);
    /** The maximum size of audio-only PiPWindows. */
    private static final Dimension MAXIMUM_AUDIO_SIZE = new Dimension(128 + (BORDER_SIZE*2), 128 + (BORDER_SIZE*2));
    
    /* Colors */
    public static final Color TRANSPARENT_BG  = new Color(0,   0,   0,   0.002f);
    public static final Color BORDER_NORMAL   = new Color(0,   69,  107, 200);
    public static final Color BORDER_OK       = new Color(152, 236, 133, 200);
    public static final Color BORDER_PROGRESS = new Color(249, 248, 113, 200);
    public static final Color BORDER_WARNING  = new Color(247, 154, 64,  200);
    public static final Color BORDER_ERROR    = new Color(247, 64,  66,  200);
    
    /** The normal icon for each PiPWindow. */
    private static final Image ICON_NORMAL   = AppRes.IMG_APP_32;
    /** The work icon for PiPWindows that are doing a background task. */
    private static final Image ICON_WORK     = AppRes.IMG_APP_32_WORK;
    /** The download icon for PiPWindows that are downloading something, typically media. */
    private static final Image ICON_DOWNLOAD = AppRes.IMG_APP_32_DOWNLOAD;
    /** The trim icon for PiPWindows that are trimming media. */
    private static final Image ICON_TRIM     = AppRes.IMG_APP_32_TRIM;

    /** The default minimum media size for all windows, with width and height values equivalent to {@link #MINIMUM_SIZE_VALUE}. */
    private static final Dimension DEFAULT_MIN_MEDIA_SIZE = new Dimension(MINIMUM_SIZE_VALUE, MINIMUM_SIZE_VALUE);
    /** This window's minimum media size. This differs from the usual minimum size, as it adapts to lower values if necessary to accommodate smaller media. */
    private Dimension minMediaSize = new Dimension(DEFAULT_MIN_MEDIA_SIZE);
    /** Manages user resizing of this window, despite its undecorated state. */
    private final ComponentResizer cr;

    /** The vlcj Media Player Component object. */
    private EmbeddedMediaPlayerComponent mediaPlayer;
    /**
     * The media currently being displayed in this window. Can be <code>null</code>,
     * in which case no media is currently displayed.
     */
    private volatile PiPMedia media;
    /** A boolean which is true when the current media is being saved. */
    private final PiPWindowState state = new PiPWindowState();
    
    /** This window's content pane. */
    private JPanel contentPane;
    /** This window's JLabel for displaying images. */
    private JLabel imgLabel;
    /** This window's JLabel's StretchIcon for displaying an image. */
    private StretchIcon imgLabelIcon;
    /** The last zoom and pan snapshot taken of the image icon, shown in the Swing image viewer, while in Normal (non-fullscreen) mode. */
    private ZoomPanSnapshot imgSnapshotNorm = ZoomPanSnapshot.DEFAULT;
    /** The last zoom and pan snapshot taken of the image icon, shown in the Swing image viewer, while in Fullscreen mode. */
    private ZoomPanSnapshot imgSnapshotFull = ZoomPanSnapshot.DEFAULT;
    /** The text field shown when no media is loaded to communicate with the user. */
    private JTextField textField;
    /** The default text to reset the text field to. */
    private static final String DEFAULT_FIELD_TXT    = "Drop media here...";
    /** The text font used in the text field. */
    private static final Font DEFAULT_FIELD_TXT_FONT = new Font(Font.DIALOG, Font.ITALIC, 20);
    /** The Timer responsible for resetting the text field's text after a set interval. */
    private final Timer textResetTimer;
    /** The FadingLineBorder bordering the content pane which can show itself and fade back to transparency. */
    private final FadingLineBorder fadingBorder = new FadingLineBorder(BORDER_NORMAL, BORDER_SIZE) {
        /** The randomly-generated serial UID for the FadingLineBorder class. */
        private static final long serialVersionUID = 616805148296397651L;
        @Override
        public void requestPaint() { contentPane.repaint(); }
    };
    /** An object for retrieving presets of various listeners used by PiPWindows. */
    private final PiPWindowListeners listeners = new PiPWindowListeners() {
        @Override
        public PiPWindow get() { return PiPWindow.this; }
        @Override
        public <C> void sendMediaCMD(PiPMediaCMD cmd, @SuppressWarnings("unchecked") C... args) {
            if (cmd != null) mediaCommand(cmd, args);
        }
    };
    /**
     * A {@link PiPWindowManagerAdapter}, set by the manager of this PiPWindow for
     * receiving communication from this instance.
     */
    private PiPWindowManagerAdapter managerListener;

    /**
     * Constructs a new PiPWindow. This constructor creates the window and simply
     * sits until it receives media.
     */
    public PiPWindow() {
        super();

        // Warn if manager is not valid for some reason.
        if (!hasManager()) System.err.println("<!> Critical error: Window doesn't have accessible manager during construction.");

        // Media Player -- Start with MediaPlayerFactory to provide audio separation fix option.
        setupMediaPlayer();
        
        // Establish Permanent Locked Hooks
        state.hook(LOCKED_SIZE, true,  (PermanentRunnable) () ->
            SwingUtilities.invokeLater(() -> PiPWindow.this.setResizable(false))
        );
        state.hook(LOCKED_SIZE, false, (PermanentRunnable) () ->
            SwingUtilities.invokeLater(() -> PiPWindow.this.setResizable(true))
        );
        // Establish Permanent Full Screen Border Hooks
        state.hook(FULLSCREEN, true,  (PermanentRunnable) () -> {
            // Only use media player to set fullscreen if VLC video.
            if (state.is(PLAYER_VLC) && state.not(CRASHED))
                mediaPlayer.mediaPlayer().fullScreen().set(true);
            // Use standard fullscreen approach for other players, even combo player.
            else SwingUtilities.invokeLater(() -> setExtendedState(JFrame.MAXIMIZED_BOTH));
            SwingUtilities.invokeLater(() -> contentPane.setBorder(null));  // Must be placed after mediaPlayer call to prevent occasional failure.
        });
        state.hook(FULLSCREEN, false, (PermanentRunnable) () -> {
            // Only use media player to set fullscreen if VLC video.
            if (state.is(PLAYER_VLC) && state.not(CRASHED))
                mediaPlayer.mediaPlayer().fullScreen().set(false);
            // Use standard fullscreen approach for other players, even combo player.
            else SwingUtilities.invokeLater(() -> setExtendedState(JFrame.NORMAL));
            SwingUtilities.invokeLater(() -> contentPane.setBorder(fadingBorder));
        });

        // Content Pane
        contentPane = new JPanel(new BorderLayout());
        contentPane.setFocusable(false);
        contentPane.setOpaque(false);
        contentPane.setBackground(Color.BLACK);
        contentPane.setBorder(fadingBorder);
        contentPane.setDropTarget(listeners.dndTargetSecondary());
        
        // JLabel for Displaying Images
        imgLabel = new JLabel();
        imgLabel.setBackground(Color.BLACK);
        imgLabel.setHorizontalAlignment(JLabel.CENTER);
        imgLabel.setBorder(null);
        imgLabel.setDropTarget(listeners.dndTarget());
        imgLabel.addMouseMotionListener(listeners.kbmHook());
        imgLabel.addMouseListener(listeners.kbmHook());
        imgLabel.addMouseWheelListener(listeners.kbmHook());
        imgLabel.addKeyListener(listeners.kbmHook());

        // Text Field (with Drag and Drop)
        textField = new JTextField(DEFAULT_FIELD_TXT);
        textField.setFont(DEFAULT_FIELD_TXT_FONT);
        textField.setEditable(false);
        textField.setHighlighter(null);
        textField.setFocusable(false);
        textField.setHorizontalAlignment(JTextField.CENTER);
        textField.setBorder(null);
        textField.setDropTarget(listeners.dndTarget());
        textField.addMouseMotionListener(listeners.kbmHook());
        textField.addMouseListener(listeners.kbmHook());
        // Text Field-Related Objects
        textResetTimer = new Timer(3000, (e) -> PiPWindow.this.textField.setText(DEFAULT_FIELD_TXT));
        textResetTimer.setRepeats(false);

        // Window-wide Key Listener
        this.addKeyListener(listeners.kbmHook());

        // ComponentResizer (With Modifications)
        cr = new ComponentResizer();
        cr.setMinimumSize(MINIMUM_SIZE);
        cr.setDragInsets(BORDER_RESIZE_INSETS);
        cr.setLockChecker(() -> state.is(LOCKED_SIZE));
        cr.addResizeListener(new StartEndListener() {
            @Override
            public void started() {
                if (imgLabelIcon == null || state.not(PLAYER_COMBO, PLAYER_SWING)) return;  // Return if null or not applicable player.
                imgLabelIcon.setParentResizing(true);
            }
            @Override
            public void ended() {
                if (imgLabelIcon == null || state.not(PLAYER_COMBO, PLAYER_SWING)) return;  // Return if null or not applicable player.
                imgLabelIcon.setParentResizing(false);
                imgLabel.repaint(); // Paint again properly now that resizing has completed.
            }
        });
        cr.registerComponent(this);

        // Pick Theme Based on Property
        pickTheme(PropDefault.THEME.matchAny(propertyState(PiPProperty.THEME, String.class)));
        
        // Frame
        this.setAlwaysOnTop(true);
        this.setUndecorated(true);
        this.setBackground(TRANSPARENT_BG);
        this.setIconImage(ICON_NORMAL);
        this.addWindowListener((WindowClosingListener) e -> {
            // Ensure PiPWindow is closed properly even if initiated unconventionally.
            if (state.not(CLOSING)) requestClose();
        });
        
        /* 
         * Clear Key/Mouse Inputs and Tracker Data When Focus is Lost -- Prevents Lingering Inputs from Lack of "Released" Calls.
         * Also prevents two windows from having their received inputs be considered together as potentially consecutive.
         */
        this.addWindowFocusListener((WindowFocusLostListener) e ->
            CompletableFuture.runAsync(PiPWindow.this.managerListener.get().getController().clearAllInputs()::clearTrackers)
        );

        // Add Text Field (not Media Player yet), then Set Content Pane and Show
        contentPane.add(textField, BorderLayout.CENTER);
        this.setContentPane(contentPane);
        this.setVisible(true);
    }

    /**
     * Constructs a new PiPWindow, immediately setting and using the passed media.
     * 
     * @param media - a PiPMedia object with the media to display.
     */
    public PiPWindow(PiPMedia media) {
        this();

        // Set Passed Media if Object is Not Null
        if (media != null) setMedia(media);
    }
    
    /**
     * Gets the inner width of the window.
     * The inner width excludes the width of the invisible border entirely.
     * 
     * @return an int with the inner width of the window.
     */
    public int getInnerWidth() {
        return getWidth() - (BORDER_SIZE * 2);
    }
    
    /**
     * Gets the inner height of the window.
     * The inner height excludes the height of the invisible border entirely.
     * 
     * @return an int with the inner height of the window.
     */
    public int getInnerHeight() {
        return getHeight() - (BORDER_SIZE * 2);
    }
    
    /**
     * Adapts the minimum size of the window depending on the current media.
     * <p>
     * If media is set, its width and height values will be compared to the set
     * minimum media size. The smaller of the two is picked for both the width and
     * height. The final values are kept at or above the
     * {@link #ABSOLUTE_MINIMUM_SIZE} regardless, to ensure proper display of the
     * window.
     * <p>
     * This method should be called after guaranteeing that the window's
     * {@link PiPMedia} has a set size within its {@link PiPMediaAttributes}.
     * Otherwise, the {@link #DEFAULT_MIN_MEDIA_SIZE} will be used.
     * 
     * @since 0.9.5
     */
    private void adaptMinimumSize() {
        // Having attributed media does not guarantee a size is set. Assign variable and check for null instead of a plain attributed media check.
        final Dimension mediaSize = hasAttributedMedia() ? this.getMedia().getAttributes().getSize() : null;
        if (mediaSize != null)
            // Prefer the smaller width/height between the default minimum and the media's size. Keep above the absolute minimum.
            this.minMediaSize.setSize(
                Math.max(ABSOLUTE_MINIMUM_SIZE, Math.min(DEFAULT_MIN_MEDIA_SIZE.width,  mediaSize.width)),
                Math.max(ABSOLUTE_MINIMUM_SIZE, Math.min(DEFAULT_MIN_MEDIA_SIZE.height, mediaSize.height))
            );
        // Media unset, or this was called before size was set in media's attributes. Use default minimum.
        else this.minMediaSize.setSize(DEFAULT_MIN_MEDIA_SIZE);
        
        // Adjust component resizer's minimum.
        this.cr.setMinimumContentSize(this.minMediaSize);
    }
    
    /**
     * Gets the {@link MediaPlayer} from within this window's media player
     * component.
     * <p>
     * This method just retrieves the VLC media player from within its embedded
     * component. Its purpose should not be confused with retrieving the type of
     * media player currently in use. That can be achieved by using
     * {@link PiPWindowState} via {@link #state()}.
     * 
     * @return the {@link MediaPlayer}, A.K.A. the VLC media player.
     */
    public MediaPlayer getMediaPlayer() {
        return (mediaPlayerValid() ? this.mediaPlayer.mediaPlayer() : null);
    }
    
    /**
     * Performs setup for the media player.
     */
    private void setupMediaPlayer() {
        if (mediaPlayerValid())
            contentPane.remove(mediaPlayer);
        
        // Determine player arguments.
        final ArrayList<String> playerArgs = new ArrayList<>();
        playerArgs.add("--aout=directsound");
        playerArgs.add("--rate=" + propertyState(PiPProperty.DEFAULT_PLAYBACK_RATE, Float.class));
        // Hardware-Accelerated Decoding (Acceleration)
        if (propertyState(PiPProperty.USE_HW_DECODING, Boolean.class)) {
            state.on(HW_ACCELERATION);
            playerArgs.add("--avcodec-hw=d3d11va");
            playerArgs.add("--vout=direct3d11");
            
            // NVIDIA RTX Video Super Resolution Configuration -- Uses Hardware Decoding/Direct3D11
            if (propertyState(PiPProperty.USE_SUPER_RES, Boolean.class)) {
                state.on(RTX_SUPER_RES);
                playerArgs.add("--d3d11-upscale-mode=super");   // Even if using old VLC version, this argument will not break the player.
            }
        }
        else {
            // Hardware-Accelerated Decoding (Acceleration) OFF
            state.off(HW_ACCELERATION);
            // This alone does not guarantee use of software decoding; ":avcodec-hw=none" as a playback argument helps. See PLAY logic.
            playerArgs.add("--avcodec-hw=none");
        }
        
        // Create player with arguments.
        final MediaPlayerFactory fac = (AppRes.USING_BACKUP_LIBVLC
                ? new MediaPlayerFactory((NativeDiscovery) null, playerArgs.toArray(new String[0]))
                : new MediaPlayerFactory(playerArgs.toArray(new String[0])));
        this.mediaPlayer = new EmbeddedMediaPlayerComponent(fac, null, new Win32FullScreenStrategy(this), null, null) {
            /** The randomly-generated serial UID for this component. */
            private static final long serialVersionUID = -392052189550107898L;
            
            /**
             * Applies the current audio configuration when called.
             */
            private void applyAudio() {
                // Set playback options based on defaults and current config.
                if (propertyState(PiPProperty.GLOBAL_MUTED, Boolean.class))
                    mediaCommand(PiPMediaCMD.MUTE, "false");
                mediaCommand(PiPMediaCMD.VOLUME_ADJUST, "SET", propertyState(PiPProperty.DEFAULT_VOLUME, String.class));
                mediaCommand(PiPMediaCMD.SPEED_ADJUST,  "SET", propertyState(PiPProperty.DEFAULT_PLAYBACK_RATE, String.class));
            }
            
            /**
             * Applies the current video configuration when called.
             * This primarily includes the sizing of the window and the determining of its aspect ratio.
             * 
             * @param mediaPlayer - the MediaPlayer to apply the configuration to.
             */
            private void applyVideo(MediaPlayer mediaPlayer) {
                state.on(RESIZING);
                CompletableFuture.runAsync(() -> {
                    System.err.println("> Started applying video.");
                    // Get Media Size and Update Attributes
                    final List<VideoTrackInfo> tracks = mediaPlayer.media().info().videoTracks();
                    if (tracks.isEmpty()) {
                        System.err.println("Warning: Couldn't apply video as there are no video tracks.");
                        return;
                    }
                    final VideoTrackInfo trackInfo = tracks.get(0);
                    final Dimension mediaSize = new Dimension(trackInfo.width(), trackInfo.height());
                    if (mediaSize.width == 0 && mediaSize.height == 0)
                        return; // If the video track does not have a size yet, don't bother setting it.
                    media.getAttributes().setSize(mediaSize.width, mediaSize.height);
                    // Set window size based on media's size on EDT.
                    SwingUtilities.invokeLater(() -> {
                        // Scale incoming media to be a maximum size while respecting its aspect ratio.
                        PiPWindow.this.changeSize(media.getAttributes().getScaledSize(DEFAULT_MEDIA_SIZE));
                        PiPWindow.this.cr.setAspectRatio(media.getAttributes().getSize());
                        adaptMinimumSize();
                        
                        // Ensure the window is on-screen after loading content.
                        ensureOnScreen();
                        state.on(READY);
                    });
                    
                    System.err.println("---> FINISHED applying video.");
                });
            }

            @Override
            public void positionChanged(MediaPlayer mediaPlayer, float newPosition) {
//                System.out.println("POSITION CHANGED: [" + newPosition + "]" + (System.nanoTime() / 1000000));
                // Sets the media to be done loading if it was still loading and the position changed.
                if (media != null && media.isLoading() && newPosition > 0.0f) {
                    media.setLoading(false);
                    state.off(LOADING);

                    applyVideo(mediaPlayer);
                    
                    // Fire redundant play command to ensure older windows respect the mode.
                    if (propertyState(PiPProperty.SINGLE_PLAY_MODE, Boolean.class))
                        mediaCommand(PiPMediaCMD.PLAY);
                }
            }
            
            @Override
            public void videoOutput(MediaPlayer mediaPlayer, int newCount) {
//                System.out.println("VIDEOOUTPUT EVENT " + (System.nanoTime() / 1000000));
//                System.err.println("NEW VIDEO OUTPUT DETECTED.");
                
                // Only fire when the media is loading. Otherwise, ignore.
                if (media != null && media.isLoading()) {
                    applyAudio();
                }
            }
            @Override
            public void playing(MediaPlayer mediaPlayer) {
//                System.err.println("PLAYING EVENT");
                // AUDIO Media.
                if (media != null && media.isLoading() && media.hasAttributes() && media.getAttributes().isAudio()) {
                    // Set that the media loading is being handled (false).
                    media.setLoading(false);
                    state.off(LOADING);
                    
                    // Run further operations asynchronously.
                    CompletableFuture.runAsync(() -> {
                        // Load album artwork (if available)
                        File file = null;
                        try {
//                            System.out.println(mediaPlayer.media().meta().get(Meta.ARTWORK_URL));
                            // Only bother attempting to change the artwork if the type of audio file supports it.
                            if (MediaExt.supportsArtwork(media.getAttributes().getFileExtension()))
                                file = new File(URI.create(mediaPlayer.media().meta().get(Meta.ARTWORK_URL)));
                        } catch (Exception e) { System.err.println("Warning: Couldn't load media artwork, it may not exist. Using default..."); }
                        try {
                            setImgViewerSrc((file != null ? file.getPath() : null), PiPWindow.class.getResource(AppRes.ICON_AUDIO_128));
                            if (file == null) PiPWindow.this.cr.setMaximumSize(MAXIMUM_AUDIO_SIZE);
                        } catch (InvalidMediaException e) { e.printStackTrace(); }
                        
                        // Ensure that audio is able to be set. Sleep until the -1 volume value becomes greater.
                        float sleepMS = 1.00f;
                        while (mediaPlayer.audio().volume() == -1) {
//                            System.out.println("Volume still -1, sleeping " + sleepMS + "ms");
                            try {
                                Thread.sleep((int) sleepMS);
                            } catch (InterruptedException e) { e.printStackTrace(); }
//                            System.out.print("Before MS: " + sleepMS);
                            sleepMS += Math.log10(sleepMS + 1);
//                            System.out.println(" | After MS: " + sleepMS);
                        }
                        
                        // Audio is not settable, so apply audio.
                        applyAudio();
                    });
                }
            }
        };
        mediaPlayer.mediaPlayer().controls().setRepeat(false);
        mediaPlayer.videoSurfaceComponent().setDropTarget(listeners.dndTarget());
        mediaPlayer.videoSurfaceComponent().addMouseMotionListener(listeners.kbmHook());
        mediaPlayer.videoSurfaceComponent().addMouseListener(listeners.kbmHook());
        mediaPlayer.videoSurfaceComponent().addMouseWheelListener(listeners.kbmHook());
        mediaPlayer.videoSurfaceComponent().addKeyListener(listeners.kbmHook());
        mediaPlayer.mediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void paused(MediaPlayer mediaPlayer) {
//                System.out.println("PAUSED");     // Debug
                if(state.not(MANUALLY_PAUSED)) {
                    mediaCommand(PiPMediaCMD.SEEK, "SET", "0.00f");
                    mediaCommand(PiPMediaCMD.PLAY);
                }
            }
            @Override
            public void stopped(MediaPlayer mediaPlayer) {
//                System.out.println("STOPPED");    // Debug
                // Only attempt to play stopped media again if window is not closing/closed.
                if(state.not(CLOSING, MANUALLY_STOPPED))
                    mediaCommand(PiPMediaCMD.PLAY);
            }
        });
    }
    
    /**
     * Simply checks if the VLC media player component within the window is
     * non-<code>null</code> and therefore still valid to call upon.
     * 
     * @return <code>true</code> if valid; <code>false</code> otherwise.
     */
    private boolean mediaPlayerValid() {
        return this.mediaPlayer != null;
    }

    /**
     * Checks if the media player can be stopped. The player can be stopped if
     * {@link #mediaPlayerValid()} and it is either playing or paused, meaning it
     * has media and is not stopped.
     * <p>
     * Getting technical, while a media player <i>can</i> be ordered to stop even if
     * it doesn't have loaded media, it might cause problems. For example, during
     * media and window close, this check can be performed, potentially reducing an
     * unnecessary native call and improving performance.
     * 
     * @return <code>true</code> if it can be stopped; <code>false</code> otherwise.
     * @since 0.9.5
     */
    private boolean mediaPlayerCanBeStopped() {
        return mediaPlayerValid() && (mediaPlayer.mediaPlayer().status().state() == State.PLAYING
                || mediaPlayer.mediaPlayer().status().state() == State.PAUSED);
    }

    @Override
    public void handleShortcutBind(final BindDetails<?> bind) {
        // Null safety check.
        if (bind == null) {
            System.err.println("<!> Received null BindDetails when handling key control in window.");
            return;
        }
        
        // Ensure code runs asynchronously (OFF of EDT).
        PiPAAUtils.invokeNowOrAsync(() -> {
            // Debug
            // System.out.println("Handling Shortcut Bind in Window: " + bind);
            
            // Modifiers
            final boolean ctrlDown  = bind.maskDown(KeyEvent.CTRL_DOWN_MASK);

            // Grab Shortcut from details.
            final Shortcut shortcut = bind.shortcut();
            
            // Controls For Either Player
            switch (shortcut) {
            case FLASH_BORDERS:
                flashBorder(null);
                break;
            case FULLSCREEN:
                if (hasMedia()) mediaCommand(PiPMediaCMD.FULLSCREEN);
                break;
            case DEBUG_INFO:
                if (!hasAttributedMedia() && !ctrlDown) {
                    // TODO Consider adding quick dialog message here as well: "No media."
                    System.out.println("Cancelling request to print info: Window is missing attributed media.");
                    flashBorder(BORDER_WARNING);
                    break;
                }
                
                // Display Info in Top Dialog
                final BetterTextArea infoTxt = new BetterTextArea(this.toString(ctrlDown));
                TopDialog.showMsg(infoTxt, "Window Information", JOptionPane.PLAIN_MESSAGE);
                break;
            case PAUSE:
            case PLAY:
            case PLAY_PAUSE:
                final PiPMediaCMD playPause = switch(shortcut) {
                case PAUSE -> PiPMediaCMD.PAUSE;
                case PLAY  -> PiPMediaCMD.PLAY;
                default    -> PiPMediaCMD.PLAYPAUSE;
                };
                // Command, Flash Borders? (Only for Audio Media), Manual? (Yes)
                if (state.is(PLAYER_COMBO))
                    mediaCommand(playPause, "true", "true");
                else if (state.is(PLAYER_VLC))
                    mediaCommand(playPause, "false", "true");
                break;
            // SEEK SPECIFIC
            case SEEK:
                // TODO Work on extension of SelectionPopup which is similar to a JOptionPane text input dialog. Can be used here.
                break;
            // SEEK BY PERCENTAGE
            case SEEK_0:
                mediaCommand(PiPMediaCMD.SEEK, "SET", "0.0f");
                break;
            case SEEK_1:
                mediaCommand(PiPMediaCMD.SEEK, "SET", "0.1f");
                break;
            case SEEK_2:
                mediaCommand(PiPMediaCMD.SEEK, "SET", "0.2f");
                break;
            case SEEK_3:
                mediaCommand(PiPMediaCMD.SEEK, "SET", "0.3f");
                break;
            case SEEK_4:
                mediaCommand(PiPMediaCMD.SEEK, "SET", "0.4f");
                break;
            case SEEK_5:
                mediaCommand(PiPMediaCMD.SEEK, "SET", "0.5f");
                break;
            case SEEK_6:
                mediaCommand(PiPMediaCMD.SEEK, "SET", "0.6f");
                break;
            case SEEK_7:
                mediaCommand(PiPMediaCMD.SEEK, "SET", "0.7f");
                break;
            case SEEK_8:
                mediaCommand(PiPMediaCMD.SEEK, "SET", "0.8f");
                break;
            case SEEK_9:
                mediaCommand(PiPMediaCMD.SEEK, "SET", "0.9f");
                break;
            // SEEK FRAME BY FRAME
            case SEEK_FRAME:
                mediaCommand(PiPMediaCMD.SEEK_FRAME);
                break;
            // SEEK BACKWARD
            case SEEK_BACKWARD_LESS:
                mediaCommand(PiPMediaCMD.SEEK, "SKIP", "-2000");
                break;
            case SEEK_BACKWARD:
                mediaCommand(PiPMediaCMD.SEEK, "SKIP", "-5000");
                break;
            case SEEK_BACKWARD_MORE:
                mediaCommand(PiPMediaCMD.SEEK, "SKIP", "-10000");
                break;
            case SEEK_BACKWARD_MAX:
                mediaCommand(PiPMediaCMD.SEEK, "SET", "0.0f");
                break;
            // SEEK FORWARD
            case SEEK_FORWARD_LESS:
                mediaCommand(PiPMediaCMD.SEEK, "SKIP", "2000");
                break;
            case SEEK_FORWARD:
                mediaCommand(PiPMediaCMD.SEEK, "SKIP", "5000");
                break;
            case SEEK_FORWARD_MORE:
                mediaCommand(PiPMediaCMD.SEEK, "SKIP", "10000");
                break;
            case SEEK_FORWARD_MAX:
                mediaCommand(PiPMediaCMD.SEEK, "SET", "1.0f");
                break;
            // PLAYBACK RATE BY PERCENTAGE
            case PLAYBACK_RATE_0:
                mediaCommand(PiPMediaCMD.SPEED_ADJUST, "SET", "0.0f");
                break;
            case PLAYBACK_RATE_1:
                mediaCommand(PiPMediaCMD.SPEED_ADJUST, "SET", "1.0f");
                break;
            case PLAYBACK_RATE_2:
                mediaCommand(PiPMediaCMD.SPEED_ADJUST, "SET", "2.0f");
                break;
            case PLAYBACK_RATE_3:
                mediaCommand(PiPMediaCMD.SPEED_ADJUST, "SET", "3.0f");
                break;
            case PLAYBACK_RATE_4:
                mediaCommand(PiPMediaCMD.SPEED_ADJUST, "SET", "4.0f");
                break;
            case PLAYBACK_RATE_5:
                mediaCommand(PiPMediaCMD.SPEED_ADJUST, "SET", "5.0f");
                break;
            case PLAYBACK_RATE_6:
                mediaCommand(PiPMediaCMD.SPEED_ADJUST, "SET", "6.0f");
                break;
            case PLAYBACK_RATE_7:
                mediaCommand(PiPMediaCMD.SPEED_ADJUST, "SET", "7.0f");
                break;
            case PLAYBACK_RATE_8:
                mediaCommand(PiPMediaCMD.SPEED_ADJUST, "SET", "8.0f");
                break;
            case PLAYBACK_RATE_9:
                mediaCommand(PiPMediaCMD.SPEED_ADJUST, "SET", "9.0f");
                break;
            // PLAYBACK RATE DECREASE
            case PLAYBACK_RATE_DOWN_LESS:
                mediaCommand(PiPMediaCMD.SPEED_ADJUST, "SKIP", "-0.1f");
                break;
            case PLAYBACK_RATE_DOWN:
                mediaCommand(PiPMediaCMD.SPEED_ADJUST, "SKIP", "-0.2f");
                break;
            case PLAYBACK_RATE_DOWN_MORE:
                mediaCommand(PiPMediaCMD.SPEED_ADJUST, "SKIP", "-0.5f");
                break;
            case PLAYBACK_RATE_DOWN_MAX:
                mediaCommand(PiPMediaCMD.SPEED_ADJUST, "SET", "0.0f");
                break;
            // PLAYBACK RATE INCREASE
            case PLAYBACK_RATE_UP_LESS:
                mediaCommand(PiPMediaCMD.SPEED_ADJUST, "SKIP", "0.1f");
                break;
            case PLAYBACK_RATE_UP:
                mediaCommand(PiPMediaCMD.SPEED_ADJUST, "SKIP", "0.2f");
                break;
            case PLAYBACK_RATE_UP_MORE:
                mediaCommand(PiPMediaCMD.SPEED_ADJUST, "SKIP", "0.5f");
                break;
            case PLAYBACK_RATE_UP_MAX:
                mediaCommand(PiPMediaCMD.SPEED_ADJUST, "SET", "1.0f");
                break;
            // VOLUME BY PERCENTAGE
            case VOLUME_0:
                mediaCommand(PiPMediaCMD.VOLUME_ADJUST, "SET", "0");
                break;
            case VOLUME_1:
                mediaCommand(PiPMediaCMD.VOLUME_ADJUST, "SET", "10");
                break;
            case VOLUME_2:
                mediaCommand(PiPMediaCMD.VOLUME_ADJUST, "SET", "20");
                break;
            case VOLUME_3:
                mediaCommand(PiPMediaCMD.VOLUME_ADJUST, "SET", "30");
                break;
            case VOLUME_4:
                mediaCommand(PiPMediaCMD.VOLUME_ADJUST, "SET", "40");
                break;
            case VOLUME_5:
                mediaCommand(PiPMediaCMD.VOLUME_ADJUST, "SET", "50");
                break;
            case VOLUME_6:
                mediaCommand(PiPMediaCMD.VOLUME_ADJUST, "SET", "60");
                break;
            case VOLUME_7:
                mediaCommand(PiPMediaCMD.VOLUME_ADJUST, "SET", "70");
                break;
            case VOLUME_8:
                mediaCommand(PiPMediaCMD.VOLUME_ADJUST, "SET", "80");
                break;
            case VOLUME_9:
                mediaCommand(PiPMediaCMD.VOLUME_ADJUST, "SET", "90");
                break;
            // VOLUME DOWN
            case VOLUME_DOWN_LESS:
                mediaCommand(PiPMediaCMD.VOLUME_ADJUST, "SKIP", "-1");
                break;
            case VOLUME_DOWN:
                mediaCommand(PiPMediaCMD.VOLUME_ADJUST, "SKIP", "-3");
                break;
            case VOLUME_DOWN_MORE:
                mediaCommand(PiPMediaCMD.VOLUME_ADJUST, "SKIP", "-10");
                break;
            case VOLUME_DOWN_MAX:
                mediaCommand(PiPMediaCMD.VOLUME_ADJUST, "SKIP", "-100");
                break;
            // VOLUME UP
            case VOLUME_UP_LESS:
                mediaCommand(PiPMediaCMD.VOLUME_ADJUST, "SKIP", "1");
                break;
            case VOLUME_UP:
                mediaCommand(PiPMediaCMD.VOLUME_ADJUST, "SKIP", "3");
                break;
            case VOLUME_UP_MORE:
                mediaCommand(PiPMediaCMD.VOLUME_ADJUST, "SKIP", "10");
                break;
            case VOLUME_UP_MAX:
                mediaCommand(PiPMediaCMD.VOLUME_ADJUST, "SKIP", "100");
                break;
            // VOLUME MUTE/UNMUTE
            case VOLUME_MUTE_UNMUTE:
                flashBorder(state.is(LOCALLY_MUTED) ? BORDER_OK : BORDER_ERROR);
                mediaCommand(PiPMediaCMD.MUTEUNMUTE);
                break;
            case CYCLE_AUDIO_TRACKS:
                // Only cycle tracks under applicable players.
                if (state.not(PLAYER_VLC, PLAYER_COMBO)) break;
                
                if (mediaPlayer.mediaPlayer().audio().trackCount() > 1) {
                    // Determine starting index for loop, then find next track in cycle.
                    final List<TrackDescription> tracks = mediaPlayer.mediaPlayer().audio().trackDescriptions();
                    final List<Integer> trackIDs = tracks.stream().map(t -> t.id()).toList();
                    final int index = trackIDs.indexOf(mediaPlayer.mediaPlayer().audio().track());
                    final Loop<TrackDescription> trackLoop = new Loop<>(tracks.toArray(TrackDescription[]::new), index);
                    
                    // Skip disabled audio track -- duplicate functionality to muting
                    if (trackLoop.next().id() == -1 && trackLoop.current().description().equalsIgnoreCase("Disable"))
                        trackLoop.next();
                    
                    // Set new audio track selection and notify user.
                    mediaPlayer.mediaPlayer().audio().setTrack(trackLoop.current().id());
                    EasyTopDialog.showMsg(this, "Audio Track: " + trackLoop.current().description(), PropDefault.THEME.matchAny(propertyState(PiPProperty.THEME, String.class)), 1000, true);
                } else {
                    EasyTopDialog.showMsg(this, "No audio tracks.", PropDefault.THEME.matchAny(propertyState(PiPProperty.THEME, String.class)), 1000, true);
                }
                break;
            case CYCLE_SUBTITLE_TRACKS:
                // Only cycle tracks under applicable players.
                if (state.not(PLAYER_VLC, PLAYER_COMBO)) break;
                
                if (mediaPlayer.mediaPlayer().subpictures().trackCount() > 1) {
                    // Determine starting index for loop, then find next track in cycle.
                    final List<TrackDescription> tracks = mediaPlayer.mediaPlayer().subpictures().trackDescriptions();
                    final List<Integer> trackIDs = tracks.stream().map(t -> t.id()).toList();
                    final int index = trackIDs.indexOf(mediaPlayer.mediaPlayer().subpictures().track());
                    final Loop<TrackDescription> trackLoop = new Loop<>(tracks.toArray(TrackDescription[]::new), index);
                    
                    // Set new subtitle track selection and notify user.
                    mediaPlayer.mediaPlayer().subpictures().setTrack(trackLoop.next().id());
                    EasyTopDialog.showMsg(this, "Subtitle Track: " + trackLoop.current().description(), PropDefault.THEME.matchAny(propertyState(PiPProperty.THEME, String.class)), 1000, true);
                } else {
                    EasyTopDialog.showMsg(this, "No subtitle tracks.", PropDefault.THEME.matchAny(propertyState(PiPProperty.THEME, String.class)), 1000, true);
                }
                break;
            case SAVE_MEDIA:
            case SAVE_MEDIA_ALT:
                // Save to Cache
                if (state.not(SAVING_MEDIA) && hasAttributedMedia() && !media.getAttributes().isLocal()) {
                    // Cancel if media is already cached.
                    if (media.isCached()) {
                        flashBorder(BORDER_WARNING);
                        EasyTopDialog.showMsg(this, "Media is already cached.", PropDefault.THEME.matchAny(propertyState(PiPProperty.THEME, String.class)));
                        break;
                    }
                    
                    iconUpdate(ICON_WORK);
                    state.on(SAVING_MEDIA).hook(SAVING_MEDIA, false, () -> iconUpdate(ICON_NORMAL));
                    flashBorder(BORDER_PROGRESS);
                    titleStatusUpdate("[Attempting to Cache...]");
                    
                    // Alternative Method -- Uses current media and attributes, sometimes resulting in a generic file name.
                    if (shortcut == Shortcut.SAVE_MEDIA_ALT) {
                        titleStatusUpdate("[Caching...]");
                        if (setRemoteMedia(media.getSrc(), media, true, false) != null) {
                            flashBorder(BORDER_OK);
                            EasyTopDialog.showMsg(this, "Saved to Cache!", PropDefault.THEME.matchAny(propertyState(PiPProperty.THEME, String.class)));
                        }
                    }
                    // Standard Method -- Takes slightly longer but results in a more accurate/readable file name.
                    else {
                        final PiPMedia mediaCopy = new PiPMedia(media.getSrc());
                        if (updateMediaAttributes(mediaCopy, Flag.RAW_ATTRIBUTION)) {
                            titleStatusUpdate("[Caching...]");
                            if (setRemoteMedia(mediaCopy.getSrc(), mediaCopy, true, true) != null) {
                                flashBorder(BORDER_OK);
                                media.setCacheSrc(mediaCopy.getCacheSrc());
                                mediaCommand(PiPMediaCMD.RELOAD, ReloadSelections.QUICK);
                            }
                        }
                    }
                    
                    if (!media.isCached()) EasyTopDialog.showMsg(this, "Could not save media to the cache.", PropDefault.THEME.matchAny(propertyState(PiPProperty.THEME, String.class)));
                    titleStatusUpdate(null);
                    state.off(SAVING_MEDIA);
                }
                break;
            case ADD_MEDIA_ARTWORK:
                if (state.not(PLAYER_COMBO)) break;
                
                final StringBuilder imgLoc = new StringBuilder();
                try {
                    SwingUtilities.invokeAndWait(() -> {
                        final FileDialog dialog = new FileDialog(this, "Add Artwork to Media File", FileDialog.LOAD);
                        dialog.setAlwaysOnTop(true);
                        dialog.setDirectory("C:\\");
                        dialog.setFile("*.jpg;*.jpeg;*.png");
//                        dialog.setFilenameFilter((dir, name) -> name.endsWith(".jpg") || name.endsWith(".png"));
                        dialog.setVisible(true);
                        final File[] files = dialog.getFiles();
                        if (files.length < 1 || files[0] == null || !files[0].exists() ||
                                (!files[0].getPath().endsWith("jpg") && !files[0].getPath().endsWith("jpeg") && !files[0].getPath().endsWith("png"))) {
                            flashBorder(BORDER_ERROR);
                        } else {
                            imgLoc.append("file:///").append(files[0].getPath());
                            flashBorder(BORDER_OK);
                        }
                        dialog.dispose();
                    });
                } catch (InvocationTargetException | InterruptedException ex) { ex.printStackTrace(); }
                if (!imgLoc.isEmpty()) replaceArtwork(imgLoc.toString());
                break;
            case RELOCATE_WINDOW:
                SwingUtilities.invokeLater(() -> ensureOnScreen());
                break;
            case RESIZE_WINDOW:
            case RESIZE_WINDOWS:
                // ♦ EDT Safe ♦ Verified: 2025-09-21
                // Determine if scaling Width or Height of window.
                final UnsetBool scaleWidth = new UnsetBool();
                SelectionPopup.showAndBlock(() -> new OptionPopup(
                        PropDefault.THEME.matchAny(propertyState(PiPProperty.THEME, String.class)),
                        "Resize Width or Height?", new String[] { "Width", "Height" })
                        .setReceiver(option -> scaleWidth.set(option == 0 ? true : false)).moveRelTo(this).display());
                
                // Neither Width or Height selected -- User exited pop-up without selecting.
                if (scaleWidth.isUnset()) break;
                
                // Get numerical value using pop-up.
                final AtomicInteger input = new AtomicInteger(-1);  // Start at -1 to detect lack of change.
                SelectionPopup.showAndBlock(() -> new NumericalInputPopup(
                        PropDefault.THEME.matchAny(propertyState(PiPProperty.THEME, String.class)),
                        "Resize: " + (scaleWidth.isTrue() ? "Width" : "Height"), 4)
                        .setValueReceiver(value -> input.set(value)).moveRelTo(this).display());
                
                // Size value not entered -- User exited pop-up without confirming.
                if (input.get() == -1) break;
                
                // Scale current dimensions to desired width/height while respecting window minimum.
                if (shortcut == Shortcut.RESIZE_WINDOW) scaleSize(input.get(), scaleWidth.isTrue());
                else getManager().callInLiveWindows(w -> w.scaleSize(input.get(), scaleWidth.isTrue()));
                break;
            case RESET_SIZE:
                SwingUtilities.invokeLater(() -> resetSize());
                break;
            case RESET_ZOOM:
                if (state.is(PLAYER_SWING)) mediaCommand(PiPMediaCMD.ZOOM, "SET", "1.00f", "0", "0");
                break;
            // ZOOM -- Combined logic simplifies casting to MouseInput and usage of coordinates.
            case ZOOM_IN_LESS:
            case ZOOM_IN:
            case ZOOM_IN_MORE:
            case ZOOM_OUT_LESS:
            case ZOOM_OUT:
            case ZOOM_OUT_MORE:
                // Player must be SWING.
                if (state.not(PLAYER_SWING)) break;
                
                // Determine Zoom Amount.
                final float zoomAmount = switch (shortcut) {
                case ZOOM_IN_LESS  ->  0.05f;
                case ZOOM_IN       ->  0.10f;
                case ZOOM_IN_MORE  ->  0.25f;
                case ZOOM_OUT_LESS -> -0.05f;
                case ZOOM_OUT      -> -0.10f;
                case ZOOM_OUT_MORE -> -0.25f;
                default -> 0.10f; // Should not occur.
                };
                
                // If the input came from the mouse, consider the coordinates.
                if (bind.input() instanceof MouseInput in)
                    mediaCommand(PiPMediaCMD.ZOOM, "SKIP", Float.toString(zoomAmount),
                            Integer.toString(in.getX() - (this.getInnerWidth()  / 2)),
                            Integer.toString(in.getY() - (this.getInnerHeight() / 2)));
                else
                    mediaCommand(PiPMediaCMD.ZOOM, "SKIP", Float.toString(zoomAmount));
                break;
            default:
                // Pass off to manager's implementation in case it handles this shortcut.
                getManager().handleShortcutBind(bind);
                break;
            }
        });
    }

    /**
     * Executes a media command based on the passed PiPMediaCMD. Shorthand for
     * wrapping the arguments in an {@link PiPMediaCMDArgs} instance and calling
     * {@link #mediaCommand(PiPMediaCMD, PiPMediaCMDArgs)}. Check out that method
     * for full documentation.
     * 
     * @param <C>  - the type of the argument(s).
     * @param cmd  - a {@link PiPMediaCMD} to execute.
     * @param args - one or more arguments to go with the command.
     * @return <code>true</code> if the command executed successfully;
     *         <code>false</code> otherwise.
     * @see {@link #mediaCommand(PiPMediaCMD, PiPMediaCMDArgs)} for full method
     *      documentation.
     */
    @SafeVarargs
    private <C> boolean mediaCommand(PiPMediaCMD cmd, C... args) {
        return mediaCommand(cmd, new PiPMediaCMDArgs<>(args));
    }
    
    /**
     * Executes a media command based on the passed PiPMediaCMD. This method will
     * return a boolean value which is dependent upon the success of the command
     * execution. If this method is called from the EDT, it will automatically
     * return <code>true</code>, since it would otherwise require halting the EDT
     * and awaiting the execution of the command in order to know if it succeeded.
     * Therefore, <b>to know if the command executed successfully, call this method
     * off of the EDT</b>.
     * 
     * @param cmd     - a {@link PiPMediaCMD} to execute.
     * @param cmdArgs - a {@link PiPMediaCMDArgs} object containing any arguments
     *                that go with the command.
     * @return <code>true</code> if the command executed successfully;
     *         <code>false</code> otherwise.
     * @see {@link #mediaCommand(PiPMediaCMD, Object...)} for an easy way to pass
     *      arguments without needing to explicitly call the {@link PiPMediaCMDArgs}
     *      constructor.
     */
    private boolean mediaCommand(PiPMediaCMD cmd, PiPMediaCMDArgs<?> cmdArgs) {
        final Supplier<Boolean> cmdCode = () -> {
//            System.out.println(Objects.toString(cmdArgs, "<null cmd args>"));   //Debug - Print arguments.
            // Determine if there are any arguments and if they are Strings.
            final boolean anyArgs = cmdArgs != null && !cmdArgs.isEmpty();
            final String[] args   = (anyArgs && cmdArgs.isOfType(String.class) ? cmdArgs.raw().toArray(new String[0]) : null);
            final boolean strArgs = args != null;
            
            switch (cmd) {
            case SET_SRC: {
                media.setLoading(true);
                
                // Declare Options
                String[] options = {
                    // Pause the media once the end is reached. PiPAA handles the restart/replay logic.
                    ":play-and-pause",
                    // Helps ensure software decoding is used in combination with previous argument.
                    state.not(HW_ACCELERATION) ? ":avcodec-hw=none" : "",
                    // Empty argument to be replaced if needed below.
                    ""
                };
                
                // Local Media
                if (media.getAttributes().isLocal()) {
                    // Check if Local Media is from the Clipboard and Has Duplicates.
                    final File currentFile = new File(args[0]);
                    final String cacheFileGuess = media.existsInClipboardCache(currentFile);
                    if (cacheFileGuess != null) {
                        currentFile.delete();
                        args[0] = cacheFileGuess;
                        media.setSrc(args[0]);
                        media.setCacheSrc(args[0]);
                        media.setAttributes(managerListener.requestAttributes(new PiPMedia(args[0])));
                    }
                    // Set as cache source if true -- allows for user deletion of media via shortcut.
                    if (media.isFromCache()) media.setCacheSrc(media.getSrc());
                }
                // Web Media
                else {
                    final String result = media.isCached() ? media.getCacheSrc() : setRemoteMedia(args[0], media, false, false);
                    if (result == null) {
                        // Unable to Set Remote Media -- Cancel and Close Current Media
                        mediaCommand(PiPMediaCMD.CLOSE);
                        flashBorder(BORDER_ERROR);
                        return false;
                    }
                    
                    args[0] = result;
                }
                
                // Media Modifications and Tuning
                args[0] = media.convertUnsupported(args[0]);
                // Since WEBP can change between IMAGE/GIF, ensure correct player is to be used.
                if (media.getAttributes().getFileExtension() == MediaExt.WEBP)
                    SwingUtilities.invokeLater(() -> pickPlayer());
                
                // Trim Transparent Edges if Set and Applicable
                if (propertyState(PiPProperty.TRIM_TRANSPARENCY, Boolean.class) && (media.getAttributes().isImage() || media.getAttributes().isGIF())) {
                    titleStatusUpdate("[Trimming...]");
                    iconUpdate(ICON_TRIM);
                    try {
                        final String croppedSrc = media.trimTransparency(args[0], TRIM_OPTION.NORMAL.matchAny(propertyState(PiPProperty.TRIM_TRANSPARENCY_OPTION, String.class)));
                        if (!croppedSrc.equals(args[0])) {
                            args[0] = croppedSrc;
                            media.setTrimSrc(args[0]);
                        }
                    } catch (MediaModificationException e) {
                        // Alert and default to regular non-cropped media
                        flashBorder(BORDER_ERROR);
                        TopDialog.showMsg("Transparency cropping failed! Defaulting to uncropped media.\n" + e.getMessage(), "Transparency Cropping Failed", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    }
                }
                
                // Advanced GIF Playback Conversion
                if (media.getAttributes().isGIF() && media.getAttributes().usesAdvancedGIFPlayback()) {
                    titleStatusUpdate("[Converting...]");
                    iconUpdate(ICON_WORK);
                    final String mediaNameID = media.getAttributes().getFileTitleID();
                    final String result = convertGIFToVideo(args[0], AppRes.APP_CONVERTED_FOLDER + "/" + mediaNameID + ".mp4");
                    if (result == null) {
                        // Unable to Convert Media -- Use Fallback Method
                        System.err.println("RESULT IS NULL !_-1--11- RESULT IS NULL using fallback and setting adv playback to false");
                        media.getAttributes().setUseAdvancedGIFPlayback(false);
                    } else {
                        args[0] = result;
                    }
                }
                
                // SWING and COMBO Players
                if(state.is(PLAYER_SWING)) {
                    // Image and Basic GIF Playback Media
                    try {
                        setImgViewerSrc(args[0], null);
                    } catch (InvalidMediaException ime) {
                        // Unable to Set Media -- Cancel and Close Current Media
                        ime.printStackTrace();
                        mediaCommand(PiPMediaCMD.CLOSE);
                        return false;
                    }
                    media.setLoading(false);
                    state.off(LOADING);
                }
                // VLC and COMBO Players
                if(state.not(PLAYER_SWING)) {
                    // Set repeat as false -- restart playback is handled manually for more control.
                    mediaPlayer.mediaPlayer().controls().setRepeat(false);
                    mediaPlayer.mediaPlayer().media().play(args[0], options);
                }
                titleStatusUpdate(null);
                break;
            }
            case PLAYPAUSE:
                // Option Arguments (T/F): [0]=Flash Borders (DEFAULT: false), [1]=Manual (DEFAULT: true)
                final boolean isPlaying = mediaPlayer.mediaPlayer().status().isPlaying();
                if (isPlaying) mediaCommand(PiPMediaCMD.PAUSE, args);
                else           mediaCommand(PiPMediaCMD.PLAY,  args);
                break;
            case PLAY:
                // Option Arguments (T/F): [0]=Flash Borders (DEFAULT: false)
                if (strArgs && args[0] != null && Boolean.valueOf(args[0]))
                    flashBorder(BORDER_OK);
                
                // If in Single Play Mode, pause all windows first, then play this one.
                if (propertyState(PiPProperty.SINGLE_PLAY_MODE, Boolean.class))
                    getManager().propertyChanged(PiPProperty.SET_ALL_PAUSED, "true");
                
                state.off(MANUALLY_STOPPED, MANUALLY_PAUSED);
                mediaPlayer.mediaPlayer().controls().play();
                break;
            case PAUSE:
                // Option Arguments (T/F): [0]=Flash Borders (DEFAULT: false), [1]=Manual (NO DEFAULT)
                if (strArgs && args[0] != null && Boolean.valueOf(args[0]))
                    flashBorder(BORDER_ERROR);
                if (strArgs && args.length > 1 && args[1] != null)
                    state.set(MANUALLY_PAUSED, Boolean.valueOf(args[1]));

                // Only allow pause once window is ready. Prevents invisible window effect and allows Single Playback Mode to function.
                if (state.is(READY))
                    mediaPlayer.mediaPlayer().controls().setPause(true);    // Use setPause(boolean), not pause() which inverts state.
                break;
            case SEEK_FRAME:
                /*
                 * Not directly possible to seek backwards. Costly compared to nextFrame, as it
                 * would involve seeking backwards to the most recent full frame, then going
                 * through each subsequent frame's changes until arriving at the desired frame.
                 */
                mediaPlayer.mediaPlayer().controls().nextFrame();
                break;
            case SEEK:
                // Expected Arguments: [0]=Type (Set or Skip), [1]=Amount
                if (args[0].equals("SET"))
                    mediaPlayer.mediaPlayer().controls().setPosition(Float.valueOf(args[1]));
                else
                    mediaPlayer.mediaPlayer().controls().skipTime(Integer.valueOf(args[1]));
                break;
            case MUTEUNMUTE:
                if (state.is(LOCALLY_MUTED))
                    mediaCommand(PiPMediaCMD.UNMUTE);
                else
                    mediaCommand(PiPMediaCMD.MUTE);
                break;
            case MUTE:
                // Optional Argument: [0]=Locally Called (DEFAULT: true)
                // No Argument Provided or Argument is true.
                if (!strArgs || Boolean.valueOf(args[0]))
                    state.on(LOCALLY_MUTED);
                
                mediaPlayer.mediaPlayer().audio().setMute(true);
                break;
            case UNMUTE:
                // Optional Argument: [0]=Locally Called (DEFAULT: true)
                // No Argument Provided or Argument is true.
                if (!strArgs || Boolean.valueOf(args[0]))
                    state.off(LOCALLY_MUTED);
                
                if (state.not(LOCALLY_MUTED) && !propertyState(PiPProperty.GLOBAL_MUTED, Boolean.class))
                    mediaPlayer.mediaPlayer().audio().setMute(false);
                break;
            case VOLUME_ADJUST:
//                System.out.println("VOL ADJUST DETECTED ON: " + media.getAttributes().getTitle());
                // Expected Arguments: [0]=Type (Set or Skip), [1]=Amount
                // TODO Catch/handle potential formatting exception on valueOf, or produce better logic here.
                int newVol = Integer.valueOf(args[1]);
                if (args[0].equals("SKIP")) {
                    newVol += mediaPlayer.mediaPlayer().audio().volume();
                }
                mediaPlayer.mediaPlayer().audio().setVolume(Math.max(0, Math.min(newVol, 100)));
                break;
            case SPEED_ADJUST:
//                System.out.println("SPEED ADJUST " + args[0] + args[1]);
                // Expected Arguments: [0]=Type (Set or Adjust), [1]=Amount
                // TODO Catch/handle potential formatting exception on valueOf, or produce better logic here.
                float newRate = Float.valueOf(args[1]);
                if (args[0].equals("SKIP")) {
                    newRate += mediaPlayer.mediaPlayer().status().rate();
                }
                mediaPlayer.mediaPlayer().controls().setRate(Math.max(0, Math.min(newRate, 5.0f)));
                break;
            case ZOOM:
                if (state.not(PLAYER_SWING))
                    return false;
                
                // Expected Arguments: [0]=Type (Set or Adjust), [1]=Amount, [2]=Mouse PositionX, [3]=Mouse PositionY
                float newZoom = Float.valueOf(args[1]);
                // If no point arguments were specified, use a default of x=0, y=0. That will zoom centrally.
                final boolean hasPointArgs = args.length == 4; 
                final Point point = hasPointArgs ? new Point(Integer.valueOf(args[2]), Integer.valueOf(args[3])) : new Point(0, 0);
                if (args[0].equals("SKIP")) ((StretchIcon) imgLabel.getIcon()).incZoom(newZoom, point);
                else                        ((StretchIcon) imgLabel.getIcon()).setZoom(newZoom, point);
                SwingUtilities.invokeLater(() -> imgLabel.repaint());
                break;
            case PAN:
                if (state.not(PLAYER_SWING))
                    return false;
                
                // Expected Arguments: [0]=Mouse PositionX, [1]=Mouse PositionY, [2]=Mouse PositionXRel, [3]=Mouse PositionYRel
                if (!strArgs) {
                    ((StretchIcon) imgLabel.getIcon()).stoppedPan();
                } else {
                    ((StretchIcon) imgLabel.getIcon()).pan(Integer.valueOf(args[0]), Integer.valueOf(args[1]));
                }
                SwingUtilities.invokeLater(() -> imgLabel.repaint());
                break;
            case FULLSCREEN:
                if (state.not(LOCKED_FULLSCREEN)) state.toggle(FULLSCREEN);
                break;
            case RELOAD:
                // Optional Argument: [0+]=ReloadSelections OR SnapshotData -- Check for valid argument types.
                final boolean selectionsArgs = anyArgs && cmdArgs.isOfType(ReloadSelections.class);
                final boolean dataArgs       = anyArgs && cmdArgs.isOfType(SnapshotData.class);
                // Create Selector -- Default to REGULAR, but change depending on argument type.
                final Selector<ReloadSelections> selector = Selector.from(ReloadSelections.REGULAR);
                if (selectionsArgs) selector.select(cmdArgs.arg(0, ReloadSelections.class));
                if (dataArgs) selector.select(ReloadSelections.CUSTOM);
                
                // Using VLC media player?
                final boolean vlc = (state.not(PLAYER_SWING) && mediaPlayerValid());
                
                // Determine final ReloadSelection using selector.
                final ReloadSelection selection = (selector.selected(ReloadSelections.CUSTOM)
                        ? new ReloadSelection(cmdArgs.argsAs(new SnapshotData[0]))
                        : new ReloadSelection(selector.selection()));
                // Capture snapshot -- Use Data Args if Present, Otherwise Default to Regular/String Arguments
                final PiPWindowSnapshot snapshot = new PiPWindowSnapshot(selection.filteredData(vlc)).capture(this);
                
                // Close media before setting it again.
                mediaCommand(PiPMediaCMD.CLOSE);
                
                // Create new media and potentially preserve previous attributes. Preserve regular media source at a minimum since it's a reload.
                final PiPMedia relMedia = new PiPMedia(snapshot.getMediaSrc());
                if (selection.shouldApplyMedia()) {
                    snapshot.apply(relMedia);
                    // Mark Media for Deletion Upon Close if Cache is Disabled
                    if (relMedia.isCached() && propertyState(PiPProperty.DISABLE_CACHE, Boolean.class))
                        relMedia.markForDeletion();
                }
                // Only set source again if entire window is NOT Closing/Crashed.
                if (state.not(CLOSING, CRASHED)) {
                    // Setup Hook Before Setting Media
                    // Preserve aspects of media player and window after ready.
                    state.hookIf(selection.shouldApplyHook(), READY, true, () -> {
                        System.out.println("Executing ready hook.");
                        // Ensure window is not closing media, though this shouldn't be an issue.
                        if (state.not(CLOSING_MEDIA)) {
                            // Run after window ready.
                            SwingUtilities.invokeLater(() -> snapshot.apply(this));
                            if (vlc) snapshot.apply(getMediaPlayer());  // Only apply if using VLC player.
                        }
                    });
                    
                    // Finally, set the media.
                    setMedia(relMedia);
                }
                break;
            case CLOSE:
                System.out.println("Got req to close media.");
                titleStatusUpdate("[Closing...]");
                final boolean replacing = (strArgs    ? Boolean.valueOf(args[0])    : false);
                final boolean marked    = (hasMedia() ? media.isMarkedForDeletion() : false);
                final PiPWindowSnapshot srcSnap = new PiPWindowSnapshot(SnapshotData.MEDIA_SOURCES).capture(this);
                final boolean hasCacheSrc = srcSnap.hasMediaCacheSrc(),
                              hasTrimSrc  = srcSnap.hasMediaTrimSrc(),
                              hasConvSrc  = srcSnap.hasMediaConvertSrc();
                
                // Stop the media player, but ensure that code does not freeze if the player is frozen and stop is called.
                state.off(LOADING);
                state.on(CLOSING_MEDIA, MANUALLY_STOPPED);
                if (state.is(PLAYER_SWING)) {
                    try {
                        PiPAAUtils.invokeNowAndWait(this::clearImgViewer);
                    } catch (InvocationTargetException | InterruptedException e) { e.printStackTrace(); }
                }
                else if (mediaPlayerCanBeStopped()) {
                    // Not using a virtual thread, as this operation may use native code.
                    try (final ExecutorService executor = Executors.newSingleThreadExecutor()) {
                        final CompletableFuture<Void> future = CompletableFuture.runAsync(() -> mediaPlayer.mediaPlayer().controls().stop(), executor);
                        try {
                            // Give the media player a max runtime to execute the command. If exceeded, it has likely crashed.
                            future.get(2000, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException | ExecutionException | TimeoutException e) {
                            future.cancel(true);
                            mediaPlayer = null;
                            state.on(CRASHED);
                            managerListener.windowMediaCrashed();
                        }
                    }   // Executor closed via try-with-resources.
                }
                state.off(FULLSCREEN);  // Fullscreen hooks have native call, but if it crashes, it will happen asynchronously.
                if (state.not(CRASHED) && !replacing) setMedia(null);
                System.out.println("Close req stopped media player.");
                
                // Determine if media source is cached and delete if marked for deletion.
                try {
                    if (marked) {
                        if (hasCacheSrc) new File(srcSnap.getMediaCacheSrc()).delete();
                        if (hasTrimSrc)  new File(srcSnap.getMediaTrimSrc()).delete();
                        if (hasConvSrc)  new File(srcSnap.getMediaConvertSrc()).delete();
                        if (hasCacheSrc || hasTrimSrc || hasConvSrc) statusUpdate("Deleted from cache.");
                    }
                } catch (NullPointerException | SecurityException e) {
                    System.err.println("Error occurred: Failed to dispose of cached media marked for deletion.");
                    e.printStackTrace();
                    statusUpdate("Failed to delete from cache!");
                }
                System.out.println("Done with req to close media.");
                state.off(CLOSING_MEDIA);
                
                // Return false if a crash occurred during closing.
                if (state.is(CRASHED)) return false;
                break;
            }
            return true;
        };

        // Run OFF of EDT.
        if (SwingUtilities.isEventDispatchThread()) {
            CompletableFuture.supplyAsync(cmdCode);
            return true;
        } else {
            return cmdCode.get();
        }
    }

    /**
     * Performs actions and adjusts the window based on the new media.
     * <p>
     * <b> Call on the event-dispatch thread (EDT).</b>
     */
    private void mediaChanged() {
        // Retrieve new/changed media.
        final PiPMedia mediaNew = this.getMedia();
        
        // Media is null when closed. Otherwise, treat as new media.
        if (mediaNew == null) {
            // Unhook READY hooks so that they do not carry over to new media.
            state.unhook(READY);
            state.off(READY);
            
            this.setTitle("");
            textField.setVisible(true);
            contentPane.remove(imgLabel);
            contentPane.remove(mediaPlayer);
            contentPane.add(textField, BorderLayout.CENTER);
            adaptMinimumSize();
            changeSize(PiPWindow.DEFAULT_SIZE, true);
            cr.setAspectRatio(null);
            ensureOnScreen();
            this.requestFocus();
            this.repaint();
            this.revalidate();
            return;
        } else {
            if (mediaNew.hasAttributes() && mediaNew.getAttributes().hasTitle())
                this.setTitle(mediaNew.getAttributes().getTitle());
            textField.setVisible(false);
            contentPane.remove(textField);
            clearImgViewer();
            cr.setMaximumSize(null);
            this.repaint();
            this.revalidate();
        }
        
        // Minimize during loading to prevent obstruction. Loading status ON and set hook.
        setExtendedState(JFrame.ICONIFIED);
        setIconImage(ICON_WORK);
        state.off(READY).on(LOADING).hook(LOADING, false, () -> SwingUtilities.invokeLater(() -> {
            setExtendedState(JFrame.NORMAL); setIconImage(ICON_NORMAL);
        }));

        // Asynchronously fire the command to set the new media source.
        CompletableFuture.runAsync(() -> {
            // Cancel and return if updating media attributes failed.
            if (!updateMediaAttributes(mediaNew))
                return;
            
            // Pick which player to use based on media.
            try {
                SwingUtilities.invokeAndWait(() -> pickPlayer());
            } catch (InvocationTargetException | InterruptedException e) {
                e.printStackTrace();
                setMedia(null);
                return;
            }
            
            mediaCommand(PiPMediaCMD.SET_SRC, mediaNew.getSrc());
        });
    }
    
    /**
     * Updates the passed media's attributes by attempting to attribute them. Raw
     * attribution can be requested via {@link Flag#RAW_ATTRIBUTION}, which ignores
     * user configuration and attempts to attribute the raw media as is (no
     * pre-conversions). This method will return a boolean value which is only
     * <code>false</code> if media attribution failed.
     * 
     * @param media - the PiPMedia to attribute.
     * @param flags - any number of attribution {@link Flag} values.
     * @return <code>true</code> if attribution succeeded; <code>false</code>
     *         otherwise.
     */
    private boolean updateMediaAttributes(PiPMedia media, Flag... flags) {
        if (!media.isAttributed()) {
            // Setup Attribute Listener then Request Attribution of Media.
            media.setAttributeUpdateListener(listeners.attributeListener());
            media.setAttributes(managerListener.requestAttributes(media, flags));
            if (!media.hasAttributes()) {
                // Failed getting attributes. Cancel setting and playing of PiPMedia.
                System.err.println("Cancelled attempt to set media: Attributor failed or returned invalid data.");
                statusUpdate("Invalid media content!");
                mediaCommand(PiPMediaCMD.CLOSE);
                return false;
            }
            media.setAttributed();
        }
        
        // Pick Player Based on Media and Config and Update Attributes
        media.getAttributes().setUseAdvancedGIFPlayback(PLAYBACK_OPTION.ADVANCED.is(propertyState(PiPProperty.GIF_PLAYBACK_MODE, String.class)));
        return true;
    }
    
    /**
     * Picks the media player/viewer based on the current media. If the passed
     * boolean is <code>true</code>, the window should use the standard media
     * player. Otherwise, it will use the image viewer.
     */
    private void pickPlayer() {
        final boolean useCombo = hasAttributedMedia() && (media.getAttributes().isAudio());
        final boolean useImgViewer = (!useCombo && (hasAttributedMedia() && (media.getAttributes().isImage()
                || (!media.getAttributes().usesAdvancedGIFPlayback() && media.getAttributes().isGIF()))));
        
        if (useImgViewer || useCombo) {
            contentPane.remove(mediaPlayer);
            contentPane.add(imgLabel, BorderLayout.CENTER);
            imgLabel.requestFocusInWindow();
            if (!useCombo) {
                System.out.println("Will use >> IMG VIEWER <<");
                state.on(PLAYER_SWING);
            } else {
                System.out.println("Will use >> COMBO VIEWER <<");
                state.on(PLAYER_COMBO);
                // Add media player to a non-center location, but make it invisible, allowing the image viewer to take up the full window.
                mediaPlayer.setVisible(false);
                contentPane.add(mediaPlayer, BorderLayout.SOUTH);
            }
        } else {
            System.out.println("Will use >> MEDIA PLAYER <<");
            contentPane.remove(imgLabel);
            mediaPlayer.setVisible(true);
            contentPane.add(mediaPlayer, BorderLayout.CENTER);
            state.on(PLAYER_VLC);
        }
        this.repaint();
        this.revalidate();
    }
    
    @Override
    public void pickTheme(THEME_OPTION theme) {
        contentPane.setBackground(theme.color(COLOR.BG));
        textField.setBackground(theme.color(COLOR.BG));
        textField.setForeground(theme.color(COLOR.TXT));
        textField.setBorder(new OffsetRoundedLineBorder(theme.color(COLOR.BG_ACCENT), 10));
        this.repaint();
        this.revalidate();
    }
    
    /**
     * Checks if this window has any valid media set. Since this method requires the
     * media to be valid (not <code>null</code>), this method will return
     * <code>false</code> if said media is <code>null</code>.
     * 
     * @return <code>true</code> if this window has valid media; <code>false</code>
     *         otherwise.
     */
    public boolean hasMedia() {
        return (this.media != null);
    }
    
    /**
     * Checks if this window has any valid, <b>attributed</b> media set. Since this
     * method requires the media to be valid (not <code>null</code>), this method
     * will return <code>false</code> if said media is <code>null</code>.
     * 
     * @return <code>true</code> if this window has valid, attributed media;
     *         <code>false</code> otherwise.
     */
    public boolean hasAttributedMedia() {
        return (hasMedia() && this.media.hasAttributes());
    }
    
    /**
     * Gets the current media of this window.
     * 
     * @return a PiPMedia object with the current media.
     */
    public PiPMedia getMedia() {
        return this.media;
    }

    /**
     * Sets the media of this window.
     * This method will update the window's title status to indicate that it is loading the media.
     * Then, it will update the window's GUI to properly play the media.
     * 
     * @param media - a PiPMedia object with the new media.
     */
    public void setMedia(PiPMedia media) {
        // Cancel setting media if media is locked.
        if (state.is(LOCKED_MEDIA) && state.not(CLOSING)) {
            System.err.println("Warning: Cancelled setting media: Window media is locked.");
            return;
        }
        // New media to replace current media -- Close current media first.
        else if(hasMedia() && !mediaCommand(PiPMediaCMD.CLOSE, "true")) {
            System.err.println("Warning: Cancelled setting media: Window's media player has crashed.");
            return;
        }
        
        this.media = media;
        
        // Update Window Loading Status
        titleStatusUpdate(media == null ? "[Closing...]" : "[Loading...]");
        
        // Fire Media Changed on EDT
        try {
            PiPAAUtils.invokeNowAndWait(this::mediaChanged);
        } catch (InvocationTargetException | InterruptedException e) {
            System.err.println("Unexpected error occurred during media change.");
            e.printStackTrace();
        }
    }
    
    /**
     * Gets the proper arguments for setting the remote media based on the passed
     * objects, including the {@link SRC_PLATFORM} of the PiPMedia.
     * <p>
     * A {@link Bin} override may also be specified for use instead of the
     * automatically selecting one. Passing a <code>null</code> value for this will
     * simply equate to no override.
     * 
     * @param src         - a String with the media source.
     * @param media       - the PiPMedida object to get remote arguments for.
     * @param binOverride - a {@link Bin} with an override to use a specific binary.
     *                    A <code>null</code> or invalid value will not override.
     * @return a List<String> of arguments for setting the remote media.
     */
    private List<String> getRemoteArgs(String src, String outDir, PiPMedia media, Bin binOverride) {
        // Cannot proceed without proper, non-null media.
        if (media == null || !media.hasAttributes())
            return null;
        
        final List<String> platformArgs = new ArrayList<String>();
        final String mediaFileNameID = media.getAttributes().getDownloadFileNameID();
        final WebMediaFormat wmf = media.getAttributes().getWMF();
        final FORMAT webFormat = media.getAttributes().getWebFormat();
        final boolean multiMedia = wmf.isItem();
        final boolean useCookies = media.getAttributes().getWMF().usedCookies();
        
        // Try downloading with the same binary that the attributor succeeded in using, but prefer override value if passed.
        //     Defaults to gallery-dl if format is null.
        boolean useYTDLP = (webFormat != null && FORMAT.GALLERY_DL.not(webFormat));
        useYTDLP = (binOverride == Bin.YT_DLP ? true : (binOverride == Bin.GALLERY_DL ? false : useYTDLP));
        
        // Change Commands Depending on Remote Source Platform (if any)
        SRC_PLATFORM platform = media.getAttributes().getSrcPlatform();
        if (platform == null) {
            System.out.println("Unexpected (Review): Remote Media didn't have SRC_PLATFORM, defaulting to GENERIC.");
            platform = SRC_PLATFORM.GENERIC;
        }
        switch (platform) {
        case YOUTUBE:
            useYTDLP = true;
        case X:
        case REDDIT:
        default:
            // Args = [0] Binary [1-2] Cookies
            platformArgs.add(useYTDLP ? Binaries.bin(Bin.YT_DLP) : Binaries.bin(Bin.GALLERY_DL));
            platformArgs.add(useYTDLP ? "--force-overwrites" : "--no-skip");
            if (useCookies) {
                platformArgs.add("--cookies");
                platformArgs.add(AppRes.COOKIES_PATH_ARG);
            }
            
            // Platform-Specific Intermediate Arguments
            switch(platform) {
            case YOUTUBE:
            case REDDIT:
                break;
            case X:
            default:
                if (!useYTDLP) {
                    platformArgs.add("--range");
                    platformArgs.add((multiMedia ? wmf.item() + "-" + wmf.item() : "1"));
                }
                break;
            }
            
            // Arguments for All Platforms, Including Generic
            if (!useYTDLP) {
                platformArgs.add("-f");
                platformArgs.add(mediaFileNameID);
                platformArgs.add("\"" + src + "\"");
                platformArgs.add("-D");
                platformArgs.add("\"" + outDir + "\"");
            } else {
                if (wmf.audioOnly() || MediaExt.supportsArtwork(media.getAttributes().getFileExtension())) {
                    System.err.println("During remote args get: Is remote audio should be downloading?");
                    platformArgs.add("--embed-thumbnail");
                    platformArgs.add("--convert-thumbnail");
                    platformArgs.add("jpg/png");
                    platformArgs.add("--embed-metadata");
                    platformArgs.add("-S");
                    platformArgs.add("aext");
                }
                platformArgs.add("--ffmpeg-location");
                platformArgs.add(AppRes.FFMPEG_LOC_ARG);
                platformArgs.add("--no-playlist");
                platformArgs.add("-I");
                platformArgs.add(multiMedia ? "" + wmf.item() : "1");
                platformArgs.add("\"" + src + "\"");
                platformArgs.add("-o");
                platformArgs.add("\"" + outDir + "/" + mediaFileNameID + "\"");
            }
            break;
        }
        
        return platformArgs;
    }
    
    /**
     * Takes a remote media source and downloads/handles it, returning the new
     * source location. This method will download the media if possible, pending the
     * current user configuration and the passed booleans.
     * 
     * @param src           - the String containing the remote source location.
     * @param media         - the PiPMedia to set.
     * @param dlOverride    - a boolean with a manual preference override on whether
     *                      to download and cache the media.
     * @param ogSrcOverride - a boolean with a manual preference override on whether
     *                      to use the original source passed in the method instead
     *                      of the web source found in the media's attributes (if
     *                      available).
     * @return a String with the new source location, which is likely to be local.
     */
    private String setRemoteMedia(String src, PiPMedia media, final boolean dlOverride, final boolean ogSrcOverride) {
        System.out.println("Setting Remote Media...");
        final PiPMediaAttributes attributes = media.getAttributes();
        final String adjustedWebSrc = attributes.getWebSrc();
        final DOWNLOAD_OPTION dlOption = PropDefault.DOWNLOAD.matchAny(propertyState(PiPProperty.DOWNLOAD_WEB_MEDIA, String.class));
        final boolean forceDownload = (dlOption.is(DOWNLOAD_OPTION.ALWAYS) || dlOverride);    // True if option or override set.
        final boolean noDownload    = (!forceDownload && dlOption.is(DOWNLOAD_OPTION.NEVER)); // True if not forced and option set.
        
        // Try to load direct remote/web media without caching.
        if(adjustedWebSrc != null && !ogSrcOverride && !forceDownload)
            src = adjustedWebSrc;
        // Return null if configuration disallows download when it's necessary.
        if (noDownload && attributes.needsDownload()) {
            System.err.println("Cannot set remote media: Media must be downloaded, but Download Media configured to NEVER.");
            statusUpdate("Media downloads disabled!");
            return null;
        }
        // Type is able to be played directly, so return current (direct) source early -- UNLESS we should force download.
        if (!attributes.needsDownload() && !forceDownload) {
            System.out.println("Media can be played directly without downloading.");
            return src;
        }
        
        // Prepare Media Information, Cache Folder, and Arguments for Commands
        final StringBuilder cacheFolder = new StringBuilder(AppRes.APP_CACHE_FOLDER + "/web");
        // Update the Specific Location Within the Cache Folder Based on Platform and Media Type
        if (!attributes.isGenericPlatform())
            cacheFolder.append("/").append(attributes.getSrcPlatform().toString().toLowerCase());
        else if (attributes.getWebSrcDomain() != null)
            cacheFolder.append("/").append(attributes.getWebSrcDomain());
        if (attributes.getType() != null)
            cacheFolder.append("/!").append(attributes.getType().toString().toLowerCase());
        
        List<String> platformArgs = getRemoteArgs(src, cacheFolder.toString(), media, null);
        final boolean triedDLPFirst = platformArgs.get(0).equals(Binaries.bin(Bin.YT_DLP));
        
        // Download the Remote Media and Update Source Information
        titleStatusUpdate("[Downloading...]");
        iconUpdate(ICON_DOWNLOAD);
        String dlResult  = downloadMedia(media, cacheFolder.toString(), platformArgs.toArray(new String[0]));
        if (dlResult == null) {
            platformArgs = getRemoteArgs(src,   cacheFolder.toString(), media, (triedDLPFirst ? Bin.GALLERY_DL : Bin.YT_DLP));
            dlResult     = downloadMedia(media, cacheFolder.toString(), platformArgs.toArray(new String[0]));
            if (dlResult == null) {
                System.err.println("Media download failed! Neither downloader succeeded.");
                statusUpdate("Could not play media.");
                return null;
            }
        }
        src = dlResult;
        media.setCacheSrc(src);
        
        // Mark Media for Deletion Upon Close if Cache is Disabled
        if (propertyState(PiPProperty.DISABLE_CACHE, Boolean.class))
            media.markForDeletion();
        
        // Return Modified Source Location String
        return src;
    }
    
    /**
     * Downloads remote media and outputs it to the passed <code>outDir</code>. The
     * download is controlled via the passed <code>String[] args</code>.
     * 
     * @param media  - the PiPMedia to download.
     * @param outDir - a String with the download output directory.
     * @param args   - a String[] with the download command arguments.
     * @return a String with the source location of the downloaded media.
     */
    private String downloadMedia(PiPMedia media, String outDir, String[] args) {
        // Setup Cache Folder and File Name
        PiPAAUtils.ensureExistence(outDir);
        final File fileOut = new File(outDir + "/" + media.getAttributes().getDownloadFileNameID());
        
        // Check if Media Exists Before Attempting Download
        final boolean fileExists = fileOut.exists();
        final String overwriteCache = propertyState(PiPProperty.OVERWRITE_CACHE, String.class);
        if (fileExists && (OVERWRITE_OPTION.NO.is(overwriteCache) || (OVERWRITE_OPTION.ASK.is(overwriteCache)
                && TopDialog.showConfirm("A media under the following name already exists in the cache:\n" + fileOut.getPath()
                    + "\n\nOverwrite it?", "Overwrite Cached Media?", JOptionPane.YES_NO_OPTION) > 0))) {
            System.out.println("Media already exists in cache and will be used instead. Download cancelled.");
            return fileOut.getPath();
        }
        
        // Download Media Using Passed Arguments.
        try {
            System.out.println("Media DL CMD Executing:\n---> " + String.join(" ", args) + "\n");
            Binaries.exec(args);
            System.out.println("Media DL CMD should be done.");
        } catch (IOException | InterruptedException e) { e.printStackTrace(); return null; }
        
        // If File Did Not Download, Return Null (Failed)
        if (!fileOut.exists())
            return null;
        
        System.out.println("Should be returning normal DL result.");
        // Return Downloaded File Path
        return fileOut.getPath();
    }
    
    /**
     * Converts the local GIF at the passed <code>in</code> location to a video and
     * outputs to <code>out</code>. Finally, the method returns the final location
     * of the converted media, or null if conversion failed.
     * 
     * @param in  - the String, local source location of the GIF media to convert.
     * @param out - the String, local source location to place the converted media at.
     * @return a String with the converted media's source location, or null if
     *         conversion failed.
     */
    private String convertGIFToVideo(String in, String out) {
        // Now convert to MP4 from GIF and return its file location.
        final File outFile = new File(out);
        if (!outFile.exists()) {
            outFile.getParentFile().mkdirs();
            System.out.println("MP4 DOESN'T EXIST");
            System.out.println("     Conv-IN: " + in);
            System.out.println("    Conv-OUT: " + out);
            System.out.println("ConvOUT-PATH: " + outFile.getPath());
            try {
                Binaries.exec(Binaries.bin(Bin.FFMPEG), "-y", "-i", "\"" + in + "\"", "-movflags", "faststart",
                        "-pix_fmt", "yuv420p", "-vf", "\"scale=trunc(iw/2)*2:trunc(ih/2)*2\"", outFile.getPath(),
                        "-hide_banner", "-loglevel", "error");
            } catch (IOException | InterruptedException e) { e.printStackTrace(); return null; }
        }
        return outFile.getPath();
    }
    
    /**
     * Sets the JLabel image viewer icon to be one of the passed sources, with the
     * raw String being the preference. After setting the image, this method
     * automatically sets the aspect ratio, changes the window size, and ensures the
     * window is on screen. Since the size is requested execution,
     * {@link #hasAttributedMedia()} should be true before calling this method.
     * 
     * @param src    - a String with the image source, or <code>null</code> to use
     *               the other argument.
     * @param urlSrc - a URL with the image source, or <code>null</code> to use the
     *               other argument.
     * @throws InvalidMediaException if the neither of the passed sources was
     *                               non-<code>null</code>.
     */
    private void setImgViewerSrc(final String src, final URL urlSrc) throws InvalidMediaException {
        // Determine type of source and create icon.
        if (src != null)
            imgLabelIcon = new StretchIcon(src, true) {
                /** The randomly-generated, default serial ID. */
                private static final long serialVersionUID = 1759259882823854325L;
                @Override
                public void requestPaint() { imgLabel.repaint(); }
                @Override
                public <T> T propertyState(PiPProperty prop, Class<T> rtnType) { return PiPWindow.this.propertyState(prop, rtnType); }
            };
        else if (urlSrc != null)
            imgLabelIcon = new StretchIcon(urlSrc, true) {
                /** The randomly-generated, default serial ID. */
                private static final long serialVersionUID = 1759259882823854325L;
                @Override
                public void requestPaint() { imgLabel.repaint(); }
                @Override
                public <T> T propertyState(PiPProperty prop, Class<T> rtnType) { return PiPWindow.this.propertyState(prop, rtnType); }
            };
        else throw new InvalidMediaException("Cannot set image viewer source: Neither image source option is valid.");
            
        state.on(RESIZING);
        media.getAttributes().setSize(imgLabelIcon.getImgWidth(), imgLabelIcon.getImgHeight());
        PiPWindow.this.cr.setAspectRatio(media.getAttributes().getSize());
        adaptMinimumSize();
        resetImgViewerSnapshots();
        SwingUtilities.invokeLater(() -> {
            imgLabel.setIcon(imgLabelIcon);
            changeSize(media.getAttributes().getScaledSize(DEFAULT_MEDIA_SIZE));
            ensureOnScreen();
            state.on(READY);
        });
    }
    
    /**
     * Clears the JLabel image viewer of its icon, flushing the image and setting
     * the icon to <code>null</code>. Without properly flushing the image, there's a
     * chance that it will not have its resources properly released. This can result
     * in a troublesome memory leak. Therefore, the image is flushed and icon set to
     * be <code>null</code> via this method, which should allow for proper garbage
     * collection.
     * <p>
     * This method <b>does not</b> run on the EDT by default, but <b>it should be
     * called from the EDT</b>.
     */
    private void clearImgViewer() {
        if (imgLabelIcon != null) {
            // Only flush the image if it has no duplicates -- prevents duplicates freezing on close of one instance.
            if (! managerListener.hasDuplicates(this)) {
                System.err.println("IMG Viewer window DOESN'T have duplicates: Flushing image.");
                // Flush the image, and nullify the entire icon itself to ensure GC eligibility.
                imgLabelIcon.getImage().flush();
            }
            imgLabelIcon = null;
            resetImgViewerSnapshots();
        }
        imgLabel.setIcon(null);
    }
    
    /**
     * Resets the Normal and Fullscreen mode zoom and pan snapshots to the default.
     * This method is intended to be used when changing media in the image viewer,
     * as it clears snapshot data relating to the current media and its state.
     * 
     * @since 0.9.5
     */
    private void resetImgViewerSnapshots() {
        this.imgSnapshotNorm = ZoomPanSnapshot.DEFAULT;
        this.imgSnapshotFull = ZoomPanSnapshot.DEFAULT;
    }
    
    /**
     * Replaces the artwork for the current audio media. If the current media does
     * not support artwork, this method does nothing.
     * <p>
     * The VLC bindings require that, in order for local artwork to load and work,
     * <code>"file:///"</code> must be prepended to the passed String location. If
     * this is not already present in the passed String, it will be prepended
     * automatically.
     * 
     * @param artLocation - a String with the file location of the artwork.
     */
    public void replaceArtwork(String artLocation) {
        // Cancel if no attributed media, or media does not support artwork, or media player invalid.
        if (!hasAttributedMedia() || !MediaExt.supportsArtwork(getMedia().getAttributes().getFileExtension()) || !mediaPlayerValid())
            return;
        
        // Delete Current Artwork from VLC Art Cache to Prevent Persistence
        final String currentArt = mediaPlayer.mediaPlayer().media().meta().get(Meta.ARTWORK_URL);
        if (currentArt != null && !currentArt.isEmpty()) {
            try {
                final File fileCurr = new File(URI.create(currentArt));
                if (fileCurr.exists()) {
                    fileCurr.delete();
                    System.err.println("Art for media already in VLC Art Cache, replacing...");
                }
            } catch (IllegalArgumentException ex) { ex.printStackTrace(); }
        }
        // Set Artwork Metadata, Save it to Underlying Media, then Reload to Show Art
//        System.out.println(mediaPlayer.mediaPlayer().media().meta().get(Meta.ARTWORK_URL));
        mediaPlayer.mediaPlayer().media().meta().set(Meta.ARTWORK_URL,
                artLocation.startsWith("file:///") ? artLocation : "file:///" + artLocation);
        if (mediaPlayer.mediaPlayer().media().meta().save())
            mediaCommand(PiPMediaCMD.RELOAD, SnapshotData.PLAYER, SnapshotData.MEDIA_SOURCES, SnapshotData.MEDIA_ATTRIBUTES);
        else
            EasyTopDialog.showMsg(this, "Could not save artwork to media.",
                    PropDefault.THEME.matchAny(propertyState(PiPProperty.THEME, String.class)), true);
    }
    
    /**
     * Briefly flashes and displays the border of the window, then immediately
     * begins to fade it back to transparency.
     * <p>
     * This method automatically executes on the event-dispatch thread (EDT). Will
     * execute immediately if already on the EDT, versus being invoked later.
     */
    public void flashBorder(final Color c) {
        PiPAAUtils.invokeNowOrLater(() -> fadingBorder.fade(c));
    }
        
    /**
     * Updates the status pretext within the window's title to the passed String. To
     * reset the title status, pass <code>null</code> or an empty String. <b>This
     * method automatically runs on the EDT, so there is no need to nest it within a
     * <code>SwingUtilities.invokeLater(Runnable)</code> call.</b> Passing
     * <code>null</code> to this method will result in a blank String, just as
     * passing <code>""</code> would.
     * 
     * @param txt - the String with the new status pretext for the title.
     */
    public void titleStatusUpdate(String txt) {
        // Sets the text on the EDT, then starts the text reset timer.
        SwingUtilities.invokeLater(() -> {
            final String title = (hasAttributedMedia() ? media.getAttributes().getTitle() : "");
            PiPWindow.this.setTitle((txt == null) ? title : (txt + " " + title).trim());
        });
    }
    
    /**
     * Temporarily updates the status text field within the window to the passed
     * String. This method then starts the default text timer, which will reset the
     * text to the default after running. <b>This method automatically runs on the
     * EDT, so there is no need to nest it within a
     * <code>SwingUtilities.invokeLater(Runnable)</code> call.</b> Passing
     * <code>null</code> to this method will result in a blank String, just as
     * passing <code>""</code> would.
     * 
     * @param txt - the String with the new status for the text field.
     */
    public void statusUpdate(String txt) {
        // Sets the text on the EDT, then starts the text reset timer.
        SwingUtilities.invokeLater(() -> {
            PiPWindow.this.textField.setText((txt == null) ? "" : txt.trim());
            PiPWindow.this.textResetTimer.restart();
        });
    }
    
    /**
     * Updates the window's icon to be the passed {@link Image}.
     * This method is shorthand for calling:
     * <pre>
     *   SwingUtilities.invokeLater(() -> setIconImage(img));
     * </pre>
     * 
     * @param img - the {@link Image} to use for this window's icon.
     */
    public void iconUpdate(final Image img) {
        SwingUtilities.invokeLater(() -> setIconImage(img));
    }
    
    /**
     * Changes the size to be the passed Dimension, plus the necessary border width
     * and height. Call this method from the EDT. This method is shorthand for
     * calling {@link #changeSize(Dimension, boolean)} with a boolean of
     * <code>false</code>.
     * 
     * @param size - the Dimension to set the size to.
     * @see {@link #changeSize(Dimension, boolean)} to have the ability to ignore
     *      borders.
     */
    public void changeSize(final Dimension size) {
        changeSize(size, false);
    }
    
    /**
     * Changes the size to be the passed Dimension, plus the necessary border width
     * and height if not ignored. Call this method from the EDT.
     * 
     * @param size          - the Dimension to set the size to.
     * @param ignoreBorders - a boolean for whether or not to ignore the window
     *                      borders and set the size without appending the border
     *                      widths.
     * @see {@link #changeSize(Dimension)} for a shorthand version of calling this
     *      method with a <code>false</code> boolean value.
     */
    public void changeSize(final Dimension size, final boolean ignoreBorders) {
        if (!ignoreBorders) size.setSize(size.width + (BORDER_SIZE * 2), size.height + (BORDER_SIZE * 2));
        PiPWindow.this.setSize(size);
        state.off(RESIZING);
    }
    
    /**
     * Scales the size, maintaining the current aspect ratio, to match the passed
     * width or height amount. The size value will not be respected only if it would
     * push either the width or height below the {@link #MINIMUM_SIZE_VALUE} for the
     * window.
     * <p>
     * This method does some additional computation, even if minimal, so it should
     * ideally be called off of the Swing event-dispatch thread (EDT). The final
     * adjustment to the window size is handled on the EDT.
     * <p>
     * Examples:
     * <pre>
     * // Examples assume minimum width of 64. All maintain ratio.
     * (1280, 720) → scaleSize(480,  true) → (480, 270)
     * (1280, 720) → scaleSize(480, false) → (853, 480)
     * ( 300, 100) → scaleSize( 80, false) → (240,  80)
     * 
     * // Too small. Width/Height would be below minimum. Adjusts automatically.
     * ( 300, 100) → scaleSize( 70,  true) → (192,  64)
     * ( 300, 100) → scaleSize( 32,  true) → (192,  64)
     * ( 300, 100) → scaleSize( 32, false) → (192,  64)
     * </pre>
     * 
     * @param size  - an int with the target size value.
     * @param width - a boolean for whether the target size is for the width
     *              (<code>true</code>) or height (<code>false</code>)
     */
    public void scaleSize(final int size, final boolean width) {
        // Scale current dimensions to desired width/height while respecting window minimum.
        final ScalingDimension dim = new ScalingDimension(getInnerWidth(), getInnerHeight()).setMinimumSize(this.minMediaSize);
        if (width) dim.scaleToWidth(size);  // Resize Based on Width
        else       dim.scaleToHeight(size); // Resize Based on Height
        SwingUtilities.invokeLater(() -> changeSize(dim));
    }

    /**
     * Resets the size of this window to the default.
     * <p>
     * If this window has media, the {@link #DEFAULT_MEDIA_SIZE} will be used,
     * scaled appropriately. Otherwise, the {@link #DEFAULT_SIZE} will be applied.
     * <p>
     * This method does <b>not</b> automatically run on the EDT, but it <b>should be
     * called from the EDT</b>.
     */
    public void resetSize() {
        if (hasMedia()) changeSize(media.getAttributes().getScaledSize(DEFAULT_MEDIA_SIZE));
        else            changeSize(DEFAULT_SIZE, true);
    }
    
    /**
     * Relocates this window to be within the pixel bounds of the display. It
     * ensures that the window is on screen, not unreachable off-screen. <b>This
     * method does not automatically run on the EDT</b>, but it should execute on
     * the EDT.
     */
    public void ensureOnScreen() {
        final int x = getX(), y = getY();
        final GraphicsDevice screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        // Call once, as each call creates a new DisplayMode object. This is slightly more efficient.
        final DisplayMode displayMode = screen.getDisplayMode();
        final int adjX  = Math.max(0, Math.min(x, displayMode.getWidth()  - getWidth())),
                  adjY  = Math.max(0, Math.min(y, displayMode.getHeight() - getHeight()));
        
        PiPWindow.this.setLocation(adjX, adjY);
    }
    
    /**
     * Gets the PiPWindowState (state) of this window.
     * A window's state contains important fields which indicate
     * what it is currently doing or has done.
     * 
     * @return a PiPWindowState object with the window's state.
     */
    public PiPWindowState state() {
        return this.state;
    }
    
    
    /**
     * Requests that this window be closed by its manager.
     * This method is intended to be called by non-manager objects.
     */
    public void requestClose() {
        PiPWindow.this.managerListener.windowCloseRequested();
    }

    /**
     * Closes this PiPWindow and releases its media player resources. It is NOT
     * advised to call this from within this class. Windows are intended to be
     * closed by their manager. If a window closes itself, other objects may not be
     * notified, and therefore, some code may not be executed when it should be.
     * <p>
     * <b>Instead</b>, call the <code>windowCloseRequested()</code> method within
     * the PiPListener provided by the window's manager.
     */
    public void closeWindow() {
        System.out.println("> close window req. received");
        // Do not execute closing code if window has already been closed.
        if (state.is(CLOSING)) return;
        state.on(CLOSING);

        // Ensure closing code will start execution off of EDT.
        PiPAAUtils.invokeNowOrAsync(() -> {
            // Only attempt to close and release media if it hasn't crashed.
            if (state.not(CRASHED) && hasMedia()) mediaCommand(PiPMediaCMD.CLOSE);
            if (mediaPlayerValid()) {
                // Release off of EDT -- Releasing on EDT had sporadic errors.
                mediaPlayer.release();
            }

            // Remove pending window state hooks.
            state.destroyHooks();
            
            // Run part on EDT.
            SwingUtilities.invokeLater(() -> {
                state.on(CLOSED);
                this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
                this.dispose();
                this.managerListener.windowClosed();
            });
            System.out.println("X> should be done with close window req.");
        });
    }
    
    /**
     * Gets the listener for this PiPWindow. This is intended to be the
     * listener for communicating with the manager of this window.
     * If the listener is not set, this method may return <code>null</code>.
     * 
     * @return the {@link PiPWindowManagerAdapter} being used.
     */
    public PiPWindowManagerAdapter getListener() {
        return this.managerListener;
    }
    
    /**
     * Sets the listener for this PiPWindow. This is intended to be the
     * listener for communicating with the manager of this window.
     * 
     * @param pl - the PiPListener to use.
     */
    public void setListener(PiPWindowManagerAdapter pl) {
        this.managerListener = pl;
    }
    
    @Override
    public void setExtendedState(int state) {
        final int prevState = getExtendedState();
        super.setExtendedState(state);
        
        // Only continue if using the Swing player and displaying an image.
        if (this.state.is(PLAYER_SWING) && imgLabel.getIcon() != null) {
            // Fullscreen -> Normal -- Capture Fullscreen snapshot then apply Normal snapshot.
            if (prevState == JFrame.MAXIMIZED_BOTH) {
                imgSnapshotFull = imgLabelIcon.snapshot();
                imgLabelIcon.applySnapshot(imgSnapshotNorm);
            }
            // Normal -> Fullscreen -- Capture Normal snapshot then apply Fullscreen snapshot.
            else if (prevState == JFrame.NORMAL) {
                imgSnapshotNorm = imgLabelIcon.snapshot();
                imgLabelIcon.applySnapshot(imgSnapshotFull);
            }
        }
    }
    
    @Override
    public void propertyChanged(PiPProperty prop, String value) {
        // Return if property value is null. This is currently not an acceptable value.
        if(value == null || state.is(CLOSING)) return;
        System.out.println("prop changed in PiPWindow");
        
        // Already on EDT since prop change occurs in a Swing GUI.
        switch(prop) {
        case THEME          -> pickTheme(PropDefault.THEME.matchAny(value));
        case GLOBAL_MUTED   -> {
            if (Boolean.valueOf(value))
                mediaCommand(PiPMediaCMD.MUTE, "false");
            else
                mediaCommand(PiPMediaCMD.UNMUTE, "false");
        }
        case SET_ALL_PAUSED -> {
            // Do nothing if no media or using image viewer.
            if (!hasMedia() || state.is(PLAYER_SWING))
                return;
            
            if (Boolean.valueOf(value))
                mediaCommand(PiPMediaCMD.PAUSE, "false", "true");
            else
                mediaCommand(PiPMediaCMD.PLAY);
        }
        case SET_ALL_MUTED         -> mediaCommand(Boolean.valueOf(value) ? PiPMediaCMD.MUTE : PiPMediaCMD.UNMUTE);
        case SET_ALL_VOLUME        -> mediaCommand(PiPMediaCMD.VOLUME_ADJUST, "SET", value);
        case SET_ALL_PLAYBACK_RATE -> mediaCommand(PiPMediaCMD.SPEED_ADJUST,  "SET", value);
        // Do Nothing -- Does not relate to a window instance.
        default -> {}
        }
    }
    
    // To Be Overridden
    @Override
    public <T> T propertyState(PiPProperty prop, Class<T> rtnType) { return null; }
    @Override
    public PiPWindowManager getManager() { return null; }

    /**
     * A custom version of {@link #toString()} which allows specification of whether
     * or not to include debug printouts.
     * 
     * @param debug - a boolean for whether or not to include debug information in
     *              the final string.
     * @return a String representation of this window.
     */
    public String toString(boolean debug) {
        final boolean hasAttrMedia = hasAttributedMedia();
        
        // Initialize variables.
        final StringBuilder info = new StringBuilder();
        final String mediaString = (hasAttrMedia ? this.media.toString() : null);
        
        // Find character length of longest line to allow for centering of some text lines.
        int longestLine = -1;
        int longestDebugLine = -1;
        if (hasAttrMedia) {
            for (final String line : mediaString.split("\n")) {
                if (line.length() > longestLine)
                    longestLine = line.length();
            }
        }
        
        // Debug Info First (if requested)
        if (debug) {
            info.append("Borderless Size: ").append(PiPAAUtils.toStringSize(getInnerWidth(), getInnerHeight())).append("\n")
                .append("     Frame Size: ").append(PiPAAUtils.toString(PiPWindow.this.getSize())).append("\n")
                .append("  ContPane Size: ").append(PiPAAUtils.toString(contentPane.getSize())).append("\n")
                .append("   VidComp Size: ").append(PiPAAUtils.toString(mediaPlayer.videoSurfaceComponent().getSize())).append("\n")
                .append("  ImgLabel Size: ").append(PiPAAUtils.toString(imgLabel.getSize())).append("\n\n")
                .append(state.toString()).append("\n\n");
            for (final String line : info.toString().split("\n")) {
                if (line.length() > longestDebugLine)
                    longestDebugLine = line.length();
            }
            info.insert(0, StringUtils.center("-----=== DEBUG INFO REQUESTED ====-----", Math.max(longestLine, longestDebugLine)) + "\n");
        }
        
        // Media Info, if there is attributed media.
        if (hasAttrMedia) {
            // Form Info String -- Use longest line to center header.
            info.append(StringUtils.center("-----=== Media Information ====-----", longestLine)).append("\n")
            .append(media.getAttributes().getFileNameID()).append("\n");
            // Audio Media
            if (media.getAttributes().isAudio()) {
                final List<AudioTrackInfo> tracks = mediaPlayer.mediaPlayer().media().info().audioTracks();
                final AudioTrackInfo track = (tracks != null && tracks.size() > 0 ? tracks.get(0) : null);
                if (track != null) {
                    info
                    .append("Duration: ").append(PiPAAUtils.toStringHMS(mediaPlayer.mediaPlayer().media().info().duration())).append("\n")
                    .append("Quality: ").append(track.bitRate()/1000).append(" kbps").append("\n")
                    .append("Codec: ").append(track.codecDescription()).append("\n");
                }
                final MetaApi meta = mediaPlayer.mediaPlayer().media().meta();
                if (meta != null) {
                    info.append("\n")
                    .append("      Title: ").append(Objects.toString(meta.get(Meta.TITLE),       "<none>")).append("\n")
                    .append("     Artist: ").append(Objects.toString(meta.get(Meta.ARTIST),      "<none>")).append("\n")
                    .append("      Album: ").append(Objects.toString(meta.get(Meta.ALBUM),       "<none>")).append("\n")
                    .append("       Date: ").append(Objects.toString(meta.get(Meta.DATE),        "<none>")).append("\n")
                    .append("Description: ").append(Objects.toString(meta.get(Meta.DESCRIPTION), "<none>")).append("\n")
                    .append("      Genre: ").append(Objects.toString(meta.get(Meta.GENRE),       "<none>")).append("\n");
                }
            }
            // Video Media
            else if (media.getAttributes().isVideo()) {
                final List<VideoTrackInfo> tracks = mediaPlayer.mediaPlayer().media().info().videoTracks();
                final VideoTrackInfo track = (tracks != null && tracks.size() > 0 ? tracks.get(0) : null);
                if (track != null) {
                    final int qualityX = track != null ? track.width() : -1, qualityY = track != null ? track.height() : -1;
                    info.append("Duration: ").append(PiPAAUtils.toStringHMS(mediaPlayer.mediaPlayer().media().info().duration())).append("\n")
                    .append("Quality: ").append((qualityX == -1 ? "Err" : qualityX) + "x" + (qualityY == -1 ? "Err" : qualityY))
                    .append(" @ ").append(String.format("%.2f", ((float) track.frameRate() / track.frameRateBase()))).append(" FPS\n")
                    .append("Codec: ").append(track.codecDescription()).append("\n")
                    .append("Bitrate: ").append(track.bitRate()).append(" kbps").append("\n");
                }
            }
            // Image/GIF Media
            else {
                final Dimension quality = media.getAttributes().getSize();
                info.append("Quality: ").append(quality != null ? quality.width + "x" + quality.height : "N/A");
            }
            final String srcLoc = (media.getAttributes().isLocal() ? "Local" : "Remote") + (media.isCached() ? " (Cached)" : "");
            info.append("\n").append("Source Location: ").append(srcLoc).append("\n\n")
            .append(media.toString()).toString();
        }
        
        return info.toString();
    }
    
    @Override
    public String toString() {
        return toString(false);
    }
}
