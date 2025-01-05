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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.apache.commons.lang3.StringUtils;

import darrylbu.icon.StretchIcon;
import dev.mwhitney.exceptions.InvalidMediaException;
import dev.mwhitney.exceptions.MediaModificationException;
import dev.mwhitney.gui.PiPWindowSnapshot.SnapshotData;
import dev.mwhitney.listeners.PiPWindowManagerAdapter;
import dev.mwhitney.listeners.PropertyListener;
import dev.mwhitney.main.Binaries;
import dev.mwhitney.main.Binaries.Bin;
import dev.mwhitney.main.Initializer;
import dev.mwhitney.main.Loop;
import dev.mwhitney.main.PermanentRunnable;
import dev.mwhitney.main.PiPProperty;
import dev.mwhitney.main.PiPProperty.DOWNLOAD_OPTION;
import dev.mwhitney.main.PiPProperty.OVERWRITE_OPTION;
import dev.mwhitney.main.PiPProperty.PLAYBACK_OPTION;
import dev.mwhitney.main.PiPProperty.PropDefault;
import dev.mwhitney.main.PiPProperty.THEME_OPTION;
import dev.mwhitney.main.PiPProperty.THEME_OPTION.COLOR;
import dev.mwhitney.main.PiPProperty.TRIM_OPTION;
import dev.mwhitney.media.MediaExt;
import dev.mwhitney.media.PiPMedia;
import dev.mwhitney.media.PiPMediaAttributes;
import dev.mwhitney.media.PiPMediaAttributes.SRC_PLATFORM;
import dev.mwhitney.media.PiPMediaCMD;
import dev.mwhitney.media.WebMediaFormat;
import dev.mwhitney.util.PiPAAUtils;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.media.AudioTrackInfo;
import uk.co.caprica.vlcj.media.Meta;
import uk.co.caprica.vlcj.media.MetaApi;
import uk.co.caprica.vlcj.media.VideoTrackInfo;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.base.TrackDescription;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.fullscreen.windows.Win32FullScreenStrategy;

/**
 * A picture-in-picture window capable of displaying media, in an always-on-top
 * state.
 * 
 * @author mwhitney57
 */
public class PiPWindow extends JFrame implements PropertyListener, Themed {
    /** The randomly-generated serial UID for the PiPWindow class. */
    private static final long serialVersionUID = 6508277241367437180L;
    
    /** The default size (x and/or y) of media (at maximum) when first set. */
    public  static final int DEFAULT_MEDIA_SIZE = 480;
    /** The width (in px) of each side of the window's border. */
    public  static final int BORDER_SIZE = 20;
    /** The window insets where the user can drag and resize the window. */
    private static final Insets BORDER_RESIZE_INSETS  = new Insets(BORDER_SIZE, BORDER_SIZE, BORDER_SIZE, BORDER_SIZE);
    /** The minimum size of PiPWindows. */
    private static final Dimension MINIMUM_SIZE       = new Dimension(64  + (BORDER_SIZE*2), 64  + (BORDER_SIZE*2));
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
    private static final Image ICON_NORMAL   = new ImageIcon(PiPWindow.class.getResource("/dev/mwhitney/images/icon32.png")).getImage();
    /** The working icon for PiPWindows that are doing a background task. */
    private static final Image ICON_WORK     = new ImageIcon(PiPWindow.class.getResource("/dev/mwhitney/images/icon32Working.png")).getImage();
    /** The working icon for PiPWindows that are doing a background task. */
    private static final Image ICON_DOWNLOAD = new ImageIcon(PiPWindow.class.getResource("/dev/mwhitney/images/icon32Downloading.png")).getImage();
    /** The working icon for PiPWindows that are doing a background task. */
    private static final Image ICON_TRIM     = new ImageIcon(PiPWindow.class.getResource("/dev/mwhitney/images/icon32Trimming.png")).getImage();

    /** Manages user resizing of this window, despite its undecorated state. */
    private ComponentResizer cr;

    /** The vlcj Media Player Component object. */
    private EmbeddedMediaPlayerComponent mediaPlayer;
    /**
     * The media currently being displayed in this window. Can be <code>null</code>,
     * in which case no media is currently displayed.
     */
    private volatile PiPMedia media;
    /** A boolean which is true when the current media is being saved. */
    private PiPWindowState state;
    
    /** This window's content pane. */
    private JPanel contentPane;
    /** This window's JLabel for displaying images. */
    private JLabel imgLabel;
    /** This window's JLabel's StretchIcon for displaying an image. */
    private StretchIcon imgLabelIcon;
    /** The default text to reset the text field to. */
    private static final String DEFAULT_FIELD_TXT    = "Drop media here...";
    /** The text font used in the text field. */
    private static final Font DEFAULT_FIELD_TXT_FONT = new Font(Font.DIALOG, Font.ITALIC, 20);
    /** The text field shown when no media is loaded to communicate with the user. */
    private JTextField textField;
    /** The Timer responsible for resetting the text field's text after a set interval. */
    private Timer textResetTimer;
    /** The FadingLineBorder bordering the content pane which can show itself and fade back to transparency. */
    private FadingLineBorder fadingBorder;
    /** An object for retrieving presets of various listeners used by PiPWindows. */
    private PiPWindowListeners listeners;
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

        // Initialize Window State
        this.state = new PiPWindowState();

        // Listeners
        this.listeners = new PiPWindowListeners() {
            @Override
            public PiPWindow get() { return PiPWindow.this; }
            @Override
            public void sendMediaCMD(PiPMediaCMD cmd, String... args) {
                if (cmd != null) mediaCommand(cmd, args);
            }
        };

        // Media Player -- Start with MediaPlayerFactory to provide audio separation fix option.
        setupMediaPlayer();
        
        // Setup Fading Border for Content Pane
        fadingBorder = new FadingLineBorder(BORDER_NORMAL, BORDER_SIZE) {
            /** The randomly-generated serial UID for the FadingLineBorder class. */
            private static final long serialVersionUID = 616805148296397651L;
            
            @Override
            public void requestPaint() { contentPane.repaint(); }
        };
        
        // Establish Permanent Full Screen Border Hooks
        state.hook(FULLSCREEN, true,  (PermanentRunnable) () -> {
            if (state.is(PLAYER_SWING))
                SwingUtilities.invokeLater(() -> setExtendedState(JFrame.MAXIMIZED_BOTH));
            else mediaPlayer.mediaPlayer().fullScreen().set(true);
            SwingUtilities.invokeLater(() -> contentPane.setBorder(null));  // Must be placed after mediaPlayer call to prevent occasional failure.
        });
        state.hook(FULLSCREEN, false, (PermanentRunnable) () -> {
            if (state.is(PLAYER_SWING))
                SwingUtilities.invokeLater(() -> setExtendedState(JFrame.NORMAL));
            else mediaPlayer.mediaPlayer().fullScreen().set(false);
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
        imgLabel.addMouseMotionListener(listeners.mouseDrag());
        imgLabel.addMouseListener(listeners.mouseAdapter());
        imgLabel.addMouseWheelListener(listeners.mouseAdapter());
        imgLabel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyControl(e);

                // Forward key press event to window in case its a global control.
                PiPWindow.this.dispatchEvent(e);
            }
        });

        // Text Field (with Drag and Drop)
        textField = new JTextField(DEFAULT_FIELD_TXT);
        textField.setFont(DEFAULT_FIELD_TXT_FONT);
        textField.setEditable(false);
        textField.setHighlighter(null);
        textField.setFocusable(false);
        textField.setHorizontalAlignment(JTextField.CENTER);
        textField.setBorder(null);
        textField.setDropTarget(listeners.dndTarget());
        textField.addMouseMotionListener(listeners.mouseDrag());
        textField.addMouseListener(listeners.mouseAdapter());
        // Text Field-Related Objects
        textResetTimer = new Timer(3000, (e) -> PiPWindow.this.textField.setText(DEFAULT_FIELD_TXT));
        textResetTimer.setRepeats(false);

        // Window-wide Key Listener
        this.addKeyListener(listeners.keyAdapter());

        // ComponentResizer (With Modifications)
        cr = new ComponentResizer();
        cr.setMinimumSize(MINIMUM_SIZE);
        cr.setDragInsets(BORDER_RESIZE_INSETS);
        cr.registerComponent(this);

        // Pick Theme Based on Property
        pickTheme(PropDefault.THEME.matchAny(propertyState(PiPProperty.THEME, String.class)));
        
        // Frame
        this.setAlwaysOnTop(true);
        this.setUndecorated(true);
        this.setBackground(TRANSPARENT_BG);
        this.setIconImage(ICON_NORMAL);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Ensure PiPWindow is closed properly even if initiated unconventionally.
                if (state.not(CLOSING)) requestClose();
            }
        });

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
        // NVIDIA RTX Video Super Resolution Configuration -- Uses Hardware Decoding/Direct3D11
        if (propertyState(PiPProperty.USE_SUPER_RES, Boolean.class)) {
            state.on(RTX_SUPER_RES);
            playerArgs.add("--avcodec-hw=d3d11va");
            playerArgs.add("--vout=direct3d11");
            playerArgs.add("--d3d11-upscale-mode=super");   // Even if using old VLC version, this argument will not break the player.
        }
        
        // Create player with arguments.
        final MediaPlayerFactory fac = (Initializer.USING_BACKUP_LIBVLC
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
                    if (tracks.size() < 1) {
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
                        
                        // Ensure the window is on-screen after loading content.
                        ensureOnScreen();
                        state().on(READY);
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
                    state().off(LOADING);

                    applyVideo(mediaPlayer);
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
                    state().off(LOADING);
                    
                    // Run further operations asynchronously.
                    CompletableFuture.runAsync(() -> {
                        // Load album artwork (if available)
                        File file = null;
                        try {
//                            System.out.println(mediaPlayer.media().meta().get(Meta.ARTWORK_URL));
                            // Only bother attempting to change the artwork if the type of audio file supports it.
                            if (MediaExt.supportsArtwork(media.getAttributes().getFileExtension()))
                                file = new File(new URL(mediaPlayer.media().meta().get(Meta.ARTWORK_URL)).toURI());
                        } catch (Exception e) { System.err.println("Warning: Couldn't load media artwork, it may not exist. Using default..."); }
                        try {
                            setImgViewerSrc((file != null ? file.getPath() : null), PiPWindow.class.getResource("/dev/mwhitney/resources/audio128.png"));
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
        mediaPlayer.videoSurfaceComponent().addMouseMotionListener(listeners.mouseDrag());
        mediaPlayer.videoSurfaceComponent().addMouseListener(listeners.mouseAdapter());
        mediaPlayer.videoSurfaceComponent().addMouseWheelListener(listeners.mouseAdapter());
        mediaPlayer.videoSurfaceComponent().addKeyListener(listeners.keyAdapter());
        mediaPlayer.mediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void paused(MediaPlayer mediaPlayer) {
//                System.out.println("PAUSED");
                if(state.not(MANUALLY_PAUSED)) {
                    mediaCommand(PiPMediaCMD.SEEK, "SET", "0.00f");
                    mediaCommand(PiPMediaCMD.PLAY);
                }
            }
            @Override
            public void stopped(MediaPlayer mediaPlayer) {
//                System.out.println("STOPPED");
                // Only attempt to play stopped media again if window is not closing/closed.
                if(state.not(CLOSING, MANUALLY_STOPPED))
                    mediaCommand(PiPMediaCMD.PLAY);
            }
        });
        mediaPlayer.videoSurfaceComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) { handleKeyControl(e); }
        });
    }
    
    /**
     * Simply checks if the VLC media player component within the window is
     * non-<code>null</code> and therefore still valid to call upon.
     * 
     * @return <code>true</code> if valid; <code>false</code> otherwise.
     */
    private boolean mediaPlayerValid() {
        return (this.mediaPlayer != null);
    }
    
    /**
     * Handles media-related keyboard controls from the user.
     * 
     * @param e - the KeyEvent with the keyboard control/input information.
     */
    private void handleKeyControl(KeyEvent e) {
        // Run media player key controls code asynchronously (OFF of EDT).
        CompletableFuture.runAsync(() -> {
//            System.out.println("KEY PRESSED");
            final int keyCode = e.getKeyCode();
            final boolean shiftDown = (e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0;
            final boolean ctrlDown  = (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK)  != 0;
            final boolean altDown   = (e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK)   != 0;
            
            // Controls For Either Player
            switch (keyCode) {
            // FULLSCREEN
            case KeyEvent.VK_F:
                mediaCommand(PiPMediaCMD.FULLSCREEN);
                break;
            // DEBUG INFO PRINTS
            case KeyEvent.VK_I:
                if (!hasAttributedMedia()) {
                    System.out.println("Cancelling request to print info: Window is missing attributed media.");
                    flashBorderEDT(BORDER_WARNING);
                    break;
                }
                
                // Display Info in Top Dialog
                final BetterTextArea infoTxt = new BetterTextArea(this.toString(ctrlDown));
                TopDialog.showMsg(infoTxt, "Window Information", JOptionPane.PLAIN_MESSAGE);
                break;
            }
            
            if (state.is(PLAYER_SWING))
                return;
            
            // Controls for Media Player
            switch (keyCode) {
            // PLAY/PAUSE
            case KeyEvent.VK_SPACE:
                if (state().is(PLAYER_COMBO))
                    mediaCommand(PiPMediaCMD.PLAYPAUSE, "true", "true");
                else
                    mediaCommand(PiPMediaCMD.PLAYPAUSE, "false", "true");
                break;
            // SEEK BY PERCENTAGE
            case KeyEvent.VK_0:
            case KeyEvent.VK_NUMPAD0:
                mediaCommand(PiPMediaCMD.SEEK, "SET", "0.0f");
                break;
            case KeyEvent.VK_1:
            case KeyEvent.VK_NUMPAD1:
                mediaCommand(PiPMediaCMD.SEEK, "SET", "0.1f");
                break;
            case KeyEvent.VK_2:
            case KeyEvent.VK_NUMPAD2:
                mediaCommand(PiPMediaCMD.SEEK, "SET", "0.2f");
                break;
            case KeyEvent.VK_3:
            case KeyEvent.VK_NUMPAD3:
                mediaCommand(PiPMediaCMD.SEEK, "SET", "0.3f");
                break;
            case KeyEvent.VK_4:
            case KeyEvent.VK_NUMPAD4:
                mediaCommand(PiPMediaCMD.SEEK, "SET", "0.4f");
                break;
            case KeyEvent.VK_5:
            case KeyEvent.VK_NUMPAD5:
                mediaCommand(PiPMediaCMD.SEEK, "SET", "0.5f");
                break;
            case KeyEvent.VK_6:
            case KeyEvent.VK_NUMPAD6:
                mediaCommand(PiPMediaCMD.SEEK, "SET", "0.6f");
                break;
            case KeyEvent.VK_7:
            case KeyEvent.VK_NUMPAD7:
                mediaCommand(PiPMediaCMD.SEEK, "SET", "0.7f");
                break;
            case KeyEvent.VK_8:
            case KeyEvent.VK_NUMPAD8:
                mediaCommand(PiPMediaCMD.SEEK, "SET", "0.8f");
                break;
            case KeyEvent.VK_9:
            case KeyEvent.VK_NUMPAD9:
                mediaCommand(PiPMediaCMD.SEEK, "SET", "0.9f");
                break;
            // SEEK BACKWARD
            case KeyEvent.VK_LEFT: {
                String type = "SKIP";
                String amount = "-5000";
                if (shiftDown && ctrlDown) {
                    type = "SET";
                    amount = "0.0f";
                } else if (ctrlDown) {
                    amount = "-10000";
                } else if (shiftDown) {
                    amount = "-2000";
                }
                mediaCommand(PiPMediaCMD.SEEK, type, amount);
                break;
            }
            // SEEK FORWARD
            case KeyEvent.VK_RIGHT: {
                String type = "SKIP";
                String amount = "5000";
                if (shiftDown && ctrlDown) {
                    type = "SET";
                    amount = "1.0f";
                } else if (ctrlDown) {
                    amount = "10000";
                } else if (shiftDown) {
                    amount = "2000";
                } else if (altDown) {
                    // SEEK FRAME Alternative Binding
                    mediaCommand(PiPMediaCMD.SEEK_FRAME);
                    break;
                }
                mediaCommand(PiPMediaCMD.SEEK, type, amount);
                break;
            }
            // PLAYBACK RATE DECREASE
            case KeyEvent.VK_MINUS: {
                String type = "SKIP";
                String amount = "-0.5f";
                if (shiftDown && ctrlDown) {
                    type = "SET";
                    amount = "0.0f";
                } else if (ctrlDown) {
                    amount = "-1.0f";
                } else if (shiftDown) {
                    amount = "-0.2f";
                }
                mediaCommand(PiPMediaCMD.SPEED_ADJUST, type, amount);
                break;
            }
            // PLAYBACK RATE INCREASE
            case KeyEvent.VK_EQUALS: {
                String type = "SKIP";
                String amount = "0.5f";
                if (shiftDown && ctrlDown) {
                    type = "SET";
                    amount = "1.0f";
                } else if (ctrlDown) {
                    amount = "1.0f";
                } else if (shiftDown) {
                    amount = "0.2f";
                }
                mediaCommand(PiPMediaCMD.SPEED_ADJUST, type, amount);
                break;
            }
            // SEEK FRAME
            case KeyEvent.VK_PERIOD:
                mediaCommand(PiPMediaCMD.SEEK_FRAME);
                break;
            // VOLUME DOWN
            case KeyEvent.VK_DOWN: {
                String amount = "-5";
                if (shiftDown && ctrlDown) {
                    amount = "-100";
                } else if (ctrlDown) {
                    amount = "-10";
                } else if (shiftDown) {
                    amount = "-1";
                }
                mediaCommand(PiPMediaCMD.VOLUME_ADJUST, "SKIP", amount);
                break;
            }
            // VOLUME UP
            case KeyEvent.VK_UP: {
                String amount = "5";
                if (shiftDown && ctrlDown) {
                    amount = "100";
                } else if (ctrlDown) {
                    amount = "10";
                } else if (shiftDown) {
                    amount = "1";
                }
                mediaCommand(PiPMediaCMD.VOLUME_ADJUST, "SKIP", amount);
                break;
            }
            // MUTE/UNMUTE
            case KeyEvent.VK_M:
                // Don't mute/unmute if intended to global mute, which is handled in window-wide listener.
                if (!(ctrlDown && shiftDown)) {
                    flashBorderEDT(state.is(LOCALLY_MUTED) ? BORDER_OK : BORDER_ERROR);
                    mediaCommand(PiPMediaCMD.MUTEUNMUTE);
                }
                break;
            /* TODO Subtitle track switching. */
            // CYCLE AUDIO TRACKS
            case KeyEvent.VK_T:
                final List<TrackDescription> tracks = mediaPlayer.mediaPlayer().audio().trackDescriptions();
                if (tracks.size() > 1) {
                    // Determine starting index for loop, then find next track in cycle.
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
            // SAVE TO CACHE
            case KeyEvent.VK_S:
                if (ctrlDown && state.not(SAVING_MEDIA) && hasAttributedMedia() && !media.getAttributes().isLocal()) {
                    // Cancel if media is already cached.
                    if (media.isCached()) {
                        flashBorderEDT(BORDER_WARNING);
                        EasyTopDialog.showMsg(this, "Media is already cached.", PropDefault.THEME.matchAny(propertyState(PiPProperty.THEME, String.class)));
                        break;
                    }
                    
                    iconUpdate(ICON_WORK);
                    state.on(SAVING_MEDIA).hook(SAVING_MEDIA, false, () -> iconUpdate(ICON_NORMAL));
                    flashBorderEDT(BORDER_PROGRESS);
                    titleStatusUpdate("[Attempting to Cache...]");
                    
                    // Alternative Method -- Uses current media and attributes, sometimes resulting in a generic file name.
                    if (altDown) {
                        titleStatusUpdate("[Caching...]");
                        if (setRemoteMedia(media.getSrc(), media, true, false) != null) {
                            flashBorderEDT(BORDER_OK);
                            EasyTopDialog.showMsg(this, "Saved to Cache!", PropDefault.THEME.matchAny(propertyState(PiPProperty.THEME, String.class)));
                        }
                    }
                    // Standard Method -- Takes slightly longer but results in a more accurate/readable file name.
                    else {
                        final PiPMedia mediaCopy = new PiPMedia(media.getSrc());
                        if (updateMediaAttributes(mediaCopy, true)) {
                            titleStatusUpdate("[Caching...]");
                            if (setRemoteMedia(mediaCopy.getSrc(), mediaCopy, true, true) != null) {
                                flashBorderEDT(BORDER_OK);
                                media.setCacheSrc(mediaCopy.getCacheSrc());
                                mediaCommand(PiPMediaCMD.RELOAD, "true");
                            }
                        }
                    }
                    
                    if (!media.isCached()) EasyTopDialog.showMsg(this, "Could not save media to the cache.", PropDefault.THEME.matchAny(propertyState(PiPProperty.THEME, String.class)));
                    titleStatusUpdate(null);
                    state.off(SAVING_MEDIA);
                }
                break;
            // ADD ARTWORK
            case KeyEvent.VK_A:
                if (state().not(PLAYER_COMBO) || shiftDown || ctrlDown || altDown)
                    break;
                
                final StringBuilder imgLoc = new StringBuilder();
                try {
                    SwingUtilities.invokeAndWait(() -> {
                        final FileDialog dialog = new FileDialog(this, "Add Artwork to Media File", FileDialog.LOAD);
                        dialog.setAlwaysOnTop(true);
                        dialog.setDirectory("C:\\");
                        dialog.setFile("*.jpg;*.jpeg;*.png");
//                            dialog.setFilenameFilter((dir, name) -> name.endsWith(".jpg") || name.endsWith(".png"));
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
                if (imgLoc.isEmpty()) break;
                /*
                 * TODO Consider allowing drag and drop of an image on an image window instead
                 * of using this file chooser dialog. That might be more convenient in some
                 * cases. Already having it open, dragging and image onto it. Then, getting
                 * a quick prompt to either add the image as artwork to the audio media or
                 * open it in a new window. Maybe config option for this: Replace Artwork, Open Window, Ask?
                 * That last part might be over-complicating things.
                 * 
                 * REF: UI-Concept-Dnd-Artwork.png for UI visual.
                 */
                // Delete Current Artwork from VLC Art Cache to Prevent Persistence
                final String currentArt = mediaPlayer.mediaPlayer().media().meta().get(Meta.ARTWORK_URL);
                if (currentArt != null && !currentArt.isEmpty()) {
                    try {
                        final File fileCurr = new File(new URL(currentArt).toURI());
                        if (fileCurr.exists()) {
                            fileCurr.delete();
                            System.err.println("Art for media already in VLC Art Cache, replacing...");
                        }
                    } catch (MalformedURLException | URISyntaxException ex) { ex.printStackTrace(); }
                }
                // Set Artwork Metadata, Save it to Underlying Media, then Reload to Show Art
//                System.out.println(mediaPlayer.mediaPlayer().media().meta().get(Meta.ARTWORK_URL));
                mediaPlayer.mediaPlayer().media().meta().set(Meta.ARTWORK_URL, imgLoc.toString());
                mediaPlayer.mediaPlayer().media().meta().save();
                mediaCommand(PiPMediaCMD.RELOAD);
                break;
            }
        });
    }

    /**
     * Executes a media command based on the passed PiPMediaCMD.
     * This method will return a boolean value which is
     * dependent upon the success of the command execution.
     * If this method is called from the EDT, it will automatically
     * return <code>true</code>, since it would otherwise require
     * halting the EDT and awaiting the execution of the command in order
     * to know if it succeeded. Therefore, <b>to know if the command executed
     * successfully, call this method off of the EDT</b>.
     * 
     * @param cmd  - a PiPMediaCMD to execute.
     * @param args - a String[] of arguments to go with the command.
     * @return <code>true</code> if the command executed successfully; <code>false</code> otherwise.
     */
    private boolean mediaCommand(PiPMediaCMD cmd, String... args) {
        final Supplier<Boolean> cmdCode = () -> {
            final boolean anyArgs = (args != null && args.length > 0);
            
            switch (cmd) {
            case SET_SRC: {
                media.setLoading(true);
                
                // Declare Options
                String[] options = { ":play-and-pause", "" };
                
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
                    final String result = convertGIFToVideo(args[0], Initializer.APP_CONVERTED_FOLDER + "/" + mediaNameID + ".mp4");
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
                    state().off(LOADING);
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
                if (anyArgs && args[0] != null && Boolean.valueOf(args[0]))
                    flashBorderEDT(BORDER_OK);
                
                state.off(MANUALLY_STOPPED, MANUALLY_PAUSED);
                mediaPlayer.mediaPlayer().controls().play();
                break;
            case PAUSE:
                // Option Arguments (T/F): [0]=Flash Borders (DEFAULT: false), [1]=Manual (NO DEFAULT)
                if (anyArgs && args[0] != null && Boolean.valueOf(args[0]))
                    flashBorderEDT(BORDER_ERROR);
                if (anyArgs && args.length > 1 && args[1] != null)
                    state.set(MANUALLY_PAUSED, Boolean.valueOf(args[1]));
                
                // Using setPause(boolean), as pause() inverts the current pause state.
                mediaPlayer.mediaPlayer().controls().setPause(true);
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
                if (!anyArgs || Boolean.valueOf(args[0]))
                    state.on(LOCALLY_MUTED);
                
                mediaPlayer.mediaPlayer().audio().setMute(true);
                break;
            case UNMUTE:
                // Optional Argument: [0]=Locally Called (DEFAULT: true)
                // No Argument Provided or Argument is true.
                if (!anyArgs || Boolean.valueOf(args[0]))
                    state.off(LOCALLY_MUTED);
                
                if (state.not(LOCALLY_MUTED) && !propertyState(PiPProperty.GLOBAL_MUTED, Boolean.class))
                    mediaPlayer.mediaPlayer().audio().setMute(false);
                break;
            case VOLUME_ADJUST:
//                System.out.println("VOL ADJUST DETECTED ON: " + media.getAttributes().getTitle());
                // Expected Arguments: [0]=Type (Set or Skip), [1]=Amount
                int newVol = Integer.valueOf(args[1]);
                if (args[0].equals("SKIP")) {
                    newVol += mediaPlayer.mediaPlayer().audio().volume();
                }
                mediaPlayer.mediaPlayer().audio().setVolume(Math.max(0, Math.min(newVol, 100)));
                break;
            case SPEED_ADJUST:
//                System.out.println("SPEED ADJUST " + args[0] + args[1]);
                // Expected Arguments: [0]=Type (Set or Adjust), [1]=Amount
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
                if (args[0].equals("SKIP")) {
                    ((StretchIcon) imgLabel.getIcon()).incZoom(newZoom, new Point(Integer.valueOf(args[2]), Integer.valueOf(args[3])));
                } else {
                    ((StretchIcon) imgLabel.getIcon()).setZoom(newZoom, new Point(Integer.valueOf(args[2]), Integer.valueOf(args[3])));
                }
                SwingUtilities.invokeLater(() -> imgLabel.repaint());
                break;
            case PAN:
                if (state.not(PLAYER_SWING))
                    return false;
                
                // Expected Arguments: [0]=Mouse PositionX, [1]=Mouse PositionY, [2]=Mouse PositionXRel, [3]=Mouse PositionYRel
                if (!anyArgs) {
                    ((StretchIcon) imgLabel.getIcon()).stoppedPan();
                } else {
                    ((StretchIcon) imgLabel.getIcon()).pan(Integer.valueOf(args[0]), Integer.valueOf(args[1]));
                }
                SwingUtilities.invokeLater(() -> imgLabel.repaint());
                break;
            case FULLSCREEN:
                state.toggle(FULLSCREEN);
                break;
            case RELOAD:
                // Optional Argument: [0]=Preserve Attributes (DEFAULT: false)
                final boolean preserve = (anyArgs && Boolean.valueOf(args[0]));
                
                // Temporarily store parts of media before they get cleared.
                final boolean vlc = (state.not(PLAYER_SWING) && mediaPlayerValid());
                final PiPWindowSnapshot snapshot = new PiPWindowSnapshot(!vlc, (vlc ? SnapshotData.ALL : SnapshotData.PLAYER)).capture(this);
                
                // Close media before setting it again.
                mediaCommand(PiPMediaCMD.CLOSE);
                
                // Create new media and potentially preserve previous attributes.
                final PiPMedia relMedia = new PiPMedia(snapshot.getMediaSrc());
                if (preserve) {
                    snapshot.apply(relMedia);
                    // Mark Media for Deletion Upon Close if Cache is Disabled
                    if (!relMedia.getAttributes().isLocal() && relMedia.isCached() && propertyState(PiPProperty.DISABLE_CACHE, Boolean.class))
                        relMedia.markForDeletion();
                }
                // Only set source again if entire window is NOT Closing/Crashed.
                if (state.not(CLOSING, CRASHED)) {
                    // Setup Hook Before Setting Media
                    // Preserve aspects of media player and window after ready.
                    state.hookIf(preserve, READY, true, () -> {
                        System.out.println("Executing ready hook.");
                        // Ensure window is not closing media, though this shouldn't be an issue.
                        if (state.not(CLOSING_MEDIA)) {
                            // Run after window ready.
                            SwingUtilities.invokeLater(() -> snapshot.apply(this));
                            if (vlc) snapshot.apply(getMediaPlayer());
                        }
                    });
                    
                    // Finally, set the media.
                    setMedia(relMedia);
                }
                break;
            case CLOSE:
                System.out.println("Got req to close media.");
                titleStatusUpdate("[Closing...]");
                final boolean replacing = (anyArgs    ? Boolean.valueOf(args[0])    : false);
                final boolean marked    = (hasMedia() ? media.isMarkedForDeletion() : false);
                final PiPWindowSnapshot srcSnap = new PiPWindowSnapshot(SnapshotData.MEDIA_SOURCES).capture(this);
                final boolean hasCacheSrc = srcSnap.hasMediaCacheSrc(),
                              hasTrimSrc  = srcSnap.hasMediaTrimSrc(),
                              hasConvSrc  = srcSnap.hasMediaConvertSrc();
                
                // Stop the media player, but ensure that code does not freeze if the player is frozen and stop is called.
                state.off(LOADING);
                state.on(CLOSING_MEDIA, MANUALLY_STOPPED);
                if (state.is(PLAYER_SWING)) {
                    final Runnable run = () -> clearImgViewer();
                    try {
                        if (SwingUtilities.isEventDispatchThread()) run.run();
                        else SwingUtilities.invokeAndWait(run);
                    } catch (InvocationTargetException | InterruptedException e) { e.printStackTrace(); }
                    state.off(FULLSCREEN);
                }
                else {
                    final ExecutorService executor = Executors.newSingleThreadExecutor();
                    @SuppressWarnings("unchecked")
                    final Future<Void> future = (Future<Void>) executor.submit(() -> mediaPlayer.mediaPlayer().controls().stop());
                    try {
                        // Give the media player a max runtime to execute the command. If exceeded, it has likely crashed.
                        future.get(2000, TimeUnit.MILLISECONDS);
                        state.off(FULLSCREEN);  // Call here -- there are hooks into this prop with mediaPlayer commands which could crash it.
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        future.cancel(true);
                        System.err.println("Error: Media player crashed in window. Opening replacement...");
                        mediaPlayer = null;
                        state.on(CRASHED);
                        managerListener.windowMediaCrashed();
                    }
                }
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
            this.setSize(PiPWindow.DEFAULT_SIZE);
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
        state().off(READY).on(LOADING).hook(LOADING, false, () -> SwingUtilities.invokeLater(() -> {
            setExtendedState(JFrame.NORMAL); setIconImage(ICON_NORMAL);
        }));

        // Asynchronously fire the command to set the new media source.
        CompletableFuture.runAsync(() -> {
            // Cancel and return if updating media attributes failed.
            if (!updateMediaAttributes(mediaNew, false))
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
     * attribution can be requested, which ignores user configuration and attempts
     * to attribute the raw media as is (no pre-conversions). This method will
     * return a boolean value which is only <code>false</code> if media attribution
     * failed.
     * 
     * @param media - the PiPMedia to attribute.
     * @param raw   - a boolean for whether or not to perform raw attribution.
     * @return <code>true</code> if attribution succeeded; <code>false</code>
     *         otherwise.
     */
    private boolean updateMediaAttributes(PiPMedia media, boolean raw) {
        if (!media.isAttributed()) {
            // Setup Attribute Listener then Request Attribution of Media.
            media.setAttributeUpdateListener(listeners.attributeListener());
            media.setAttributes(managerListener.requestAttributes(media, raw));
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
        // New media to replace current media -- Close current media first.
        if(hasMedia() && !mediaCommand(PiPMediaCMD.CLOSE, "true")) {
            System.err.println("Warning: Cancelled setting media: Window's media player has crashed.");
            return;
        }
        
        this.media = media;
        
        // Update Window Loading Status
        titleStatusUpdate(media == null ? "[Closing...]" : "[Loading...]");
        
        // Fire Media Changed on EDT
        try {
            if (SwingUtilities.isEventDispatchThread()) this.mediaChanged();
            else     SwingUtilities.invokeAndWait(() -> this.mediaChanged());
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
        final boolean multiMedia = wmf.isItem();
        boolean useCookies = true;
        
        // Determine which binary to use, defaulting to the calculated value if no override was provided.
        boolean useYTDLP = false;
//        boolean useYTDLP = !FORMAT.GALLERY_DL.is(media.getAttributes().getWebFormat());
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
            useCookies = false;
        case X:
        case REDDIT:
        default:
            // Args = [0] Binary [1-2] Cookies
            platformArgs.add(useYTDLP ? Binaries.bin(Bin.YT_DLP) : Binaries.bin(Bin.GALLERY_DL));
            platformArgs.add(useYTDLP ? "--force-overwrites" : "--no-skip");
            if (useCookies) {
                platformArgs.add("--cookies");
                platformArgs.add(Binaries.COOKIES_PATH_ARG);
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
                platformArgs.add(Binaries.FFMPEG_LOC_ARG);
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
        // Return current source early if no download is true OR is web direct and download not forced.
        if (noDownload || ((attributes.isVideo() || attributes.isAudio() || attributes.isPlaylist()) && attributes.isWebDirect() && !forceDownload))
            return src;
        
        // Prepare Media Information, Cache Folder, and Arguments for Commands
        final StringBuilder cacheFolder = new StringBuilder(Initializer.APP_CACHE_FOLDER + "/web");
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
            System.out.println("Media DL CMD executing...");
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
        if (src != null)
            imgLabelIcon = new StretchIcon(src, true);
        else if (urlSrc != null)
            imgLabelIcon = new StretchIcon(urlSrc, true);
        else
            throw new InvalidMediaException("Neither image source option is valid.");
            
        state.on(RESIZING);
        media.getAttributes().setSize(imgLabelIcon.getImgWidth(), imgLabelIcon.getImgHeight());
        PiPWindow.this.cr.setAspectRatio(media.getAttributes().getSize());
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
        }
        imgLabel.setIcon(null);
    }
    
    /**
     * Briefly flashes and displays the border of the window, then immediately
     * begins to fade it back to transparency.
     */
    public void flashBorder(final Color c) {
        if (c == null) fadingBorder.fade();
        else fadingBorder.fade(c);
    }
        
    /**
     * Briefly flashes and displays the border of the window, then immediately
     * begins to fade it back to transparency.
     * <p>
     * <b>This method executes the code on the event-dispatch thread (EDT). If the
     * calling thread is on the EDT, then use <code>flashBorder(Color)</code>.</b>
     */
    public void flashBorderEDT(final Color c) {
        SwingUtilities.invokeLater(() -> flashBorder(c));
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

        final Runnable closeCode = () -> {
            // Only attempt to close and release media if it hasn't crashed.
            if (state.not(CRASHED) && hasMedia()) mediaCommand(PiPMediaCMD.CLOSE);
            if (mediaPlayerValid()) {
                // Release off of EDT -- Releasing on EDT had sporadic errors.
                mediaPlayer.release();
            }

            // Remove pending window state hooks.
            state.disableHooks();
            
            // Run part on EDT.
            SwingUtilities.invokeLater(() -> {
                state.on(CLOSED);
                this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
                this.dispose();
                this.managerListener.windowClosed();
            });
            System.out.println("X> should be done with close window req.");
        };

        // Ensure code will run off EDT to start.
        if (SwingUtilities.isEventDispatchThread()) CompletableFuture.runAsync(closeCode);
        else closeCode.run();
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
        
        if (state().is(PLAYER_SWING) && (prevState == JFrame.MAXIMIZED_BOTH || prevState == JFrame.NORMAL) && imgLabel.getIcon() != null)
            ((StretchIcon) imgLabel.getIcon()).setZoom(1.00f, new Point(0,0));
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
    
    // To Be Overriden
    @Override
    public <T> T propertyState(PiPProperty prop, Class<T> rtnType) { return null; }
    
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
            info.append("   Frame Size: ").append(PiPWindow.this.getSize()).append("\n")
                .append("ContPane Size: ").append(contentPane.getSize()).append("\n")
                .append(" VidComp Size: ").append(mediaPlayer.videoSurfaceComponent().getSize()).append("\n")
                .append("ImgLabel Size: ").append(imgLabel.getSize()).append("\n\n");
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
                    info.append("Quality: ").append((qualityX == -1 ? "Err" : qualityX) + "x" + (qualityY == -1 ? "Err" : qualityY))
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
