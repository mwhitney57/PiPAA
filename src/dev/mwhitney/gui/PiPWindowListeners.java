package dev.mwhitney.gui;

import static dev.mwhitney.gui.PiPWindowState.StateProp.CLOSED;
import static dev.mwhitney.gui.PiPWindowState.StateProp.CLOSING_MEDIA;
import static dev.mwhitney.gui.PiPWindowState.StateProp.FULLSCREEN;
import static dev.mwhitney.gui.PiPWindowState.StateProp.LOADING;
import static dev.mwhitney.gui.PiPWindowState.StateProp.LOCKED_FULLSCREEN;
import static dev.mwhitney.gui.PiPWindowState.StateProp.LOCKED_MEDIA;
import static dev.mwhitney.gui.PiPWindowState.StateProp.LOCKED_POSITION;
import static dev.mwhitney.gui.PiPWindowState.StateProp.LOCKED_SIZE;
import static dev.mwhitney.gui.PiPWindowState.StateProp.PLAYER_COMBO;
import static dev.mwhitney.gui.PiPWindowState.StateProp.PLAYER_SWING;
import static dev.mwhitney.gui.PiPWindowState.StateProp.READY;
import static dev.mwhitney.media.MediaFlavorPicker.MediaFlavor.FILE;
import static dev.mwhitney.media.MediaFlavorPicker.MediaFlavor.IMAGE;
import static dev.mwhitney.media.MediaFlavorPicker.MediaFlavor.STRING;
import static dev.mwhitney.media.MediaFlavorPicker.MediaFlavor.WEB_URL;

import java.awt.Desktop;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import dev.mwhitney.exceptions.InvalidTransferMediaException;
import dev.mwhitney.gui.components.BetterTextArea;
import dev.mwhitney.gui.popup.ArtSelectionPopup;
import dev.mwhitney.gui.popup.LockSelectionPopup;
import dev.mwhitney.gui.popup.TopDialog;
import dev.mwhitney.listeners.AttributeUpdateListener;
import dev.mwhitney.listeners.PiPAttributeRequestListener;
import dev.mwhitney.listeners.PiPCommandListener;
import dev.mwhitney.listeners.PiPHandoffListener;
import dev.mwhitney.listeners.PiPMediaTransferListener;
import dev.mwhitney.listeners.PiPWindowListener;
import dev.mwhitney.listeners.simplified.KeyPressListener;
import dev.mwhitney.main.Initializer;
import dev.mwhitney.main.PiPProperty;
import dev.mwhitney.main.PiPProperty.PropDefault;
import dev.mwhitney.main.PropertiesManager;
import dev.mwhitney.media.MediaExt;
import dev.mwhitney.media.MediaFlavorPicker;
import dev.mwhitney.media.MediaFlavorPicker.MediaFlavor;
import dev.mwhitney.media.PiPMedia;
import dev.mwhitney.media.PiPMediaAttributes;
import dev.mwhitney.media.PiPMediaAttributor.Flag;
import dev.mwhitney.media.PiPMediaCMD;
import dev.mwhitney.util.PiPAAUtils;
import dev.mwhitney.util.UnsetBool;

/**
 * The listeners for PiPWindows and their components, especially those relating
 * to media transfers such as drag and drop and copy/paste.
 * 
 * @author mwhitney57
 */
public abstract class PiPWindowListeners implements PiPWindowListener, PiPCommandListener, PiPMediaTransferListener, PiPHandoffListener, PiPAttributeRequestListener {
    /** A DataFlavor containing a web URL. */
    private DataFlavor flavorWebURL;
    
    /** A DropTarget for drag 'n drop or clipboard media transfers. */
    private DropTarget dndTarget;
    /** A DropTarget for drag 'n drop or clipboard media transfers that should be handed off to a new window. */
    private DropTarget dndTargetSecondary;
    /** The KeyListener for all windows. */
    private KeyListener keyListener;
    /** The AttributeUpdateListener which listens for updates to each window's media's attributes. */
    private AttributeUpdateListener attributeListener;
    
    /** The Point at which a drag action originated from. */
    private Point dragOrigin;
    /** The Point at which a LMB drag action originated from. */
    private Point dragOriginLMB;
    /** A boolean for whether or not the LMB is down. */
    private boolean leftMouseDown;
    /** A boolean for whether or not the MMB is down. */
    private boolean middleMouseDown;
    /** The MouseAdapter for all windows. */
    private MouseAdapter mouseAdapter;
    /** The MouseMotionAdapter for all windows. */
    private MouseMotionAdapter mouseDrag;
    
    public PiPWindowListeners() {
        // Create custom flavor(s).
        try {
            flavorWebURL = new DataFlavor("application/x-java-url;class=java.net.URL");
        } catch (ClassNotFoundException e) {
            System.err.println("Error creating custom data flavors for drag and drop/copy and paste.");
        }

        // Multiple DropTargets are used to prevent situational blocking of accepting a drop (block cursor).
        dndTarget = new DropTarget() {
            /** The serial ID for the <tt>DropTarget</tt>. */
            private static final long serialVersionUID = -6081451068101077824L;
            @Override
            public synchronized void drop(DropTargetDropEvent evt) { dropReceived(evt); }
        };
        dndTargetSecondary = new DropTarget() {
            /** The serial ID for the <tt>DropTarget</tt>. */
            private static final long serialVersionUID = 7736789682292741649L;
            @Override
            public synchronized void drop(DropTargetDropEvent evt) { dropReceived(evt); }
        };

        keyListener = (KeyPressListener) (e) -> {
            final int keyCode = e.getKeyCode();
            final boolean shiftDown = (e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0;
            final boolean ctrlDown  = (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK)  != 0;
            final boolean altDown   = (e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK)   != 0;

            switch (keyCode) {
            // RELOCATE WINDOW ON SCREEN IF OFF
            case KeyEvent.VK_L:
                if (ctrlDown) {
                    // Enable size & pos locks. TODO Allow customization instead for this option in config. User can check select each they want to be able to enable.
                    if (shiftDown)
                        get().state().on(LOCKED_SIZE, LOCKED_POSITION);
                    // Disable all locks.
                    else if (altDown)
                        get().state().off(LOCKED_SIZE, LOCKED_POSITION, LOCKED_FULLSCREEN, LOCKED_MEDIA);
                    // Pop-up with lock selection options for user to decide.
                    else new LockSelectionPopup(PropDefault.THEME.matchAny(get().propertyState(PiPProperty.THEME, String.class)),
                            get().state()).moveRelTo(get()).display();
                }
                else get().ensureOnScreen();
                break;
            // SHOW KEYBOARD SHORTCUTS
            case KeyEvent.VK_K:
                final BetterTextArea shortcutsComp = new BetterTextArea(Initializer.SHORTCUTS);
                TopDialog.showMsg(shortcutsComp, "Keyboard and Mouse Shortcuts", JOptionPane.PLAIN_MESSAGE);
                break;
            // SHOW WINDOW BORDERS
            case KeyEvent.VK_B:
                get().flashBorder(null);
                break;
            // OPEN LOCAL/CACHED FILE DIRECTORY
            case KeyEvent.VK_O:
                if (!ctrlDown) break;

                // Default to cache folder.
                String openSrc = Initializer.APP_CACHE_FOLDER;
                if (get().hasAttributedMedia()) {
                    // Determine local media location (if any)
                    if (get().getMedia().isCached())
                        openSrc = get().getMedia().getCacheSrc();
                    else if (get().getMedia().getAttributes().isLocal())
                        openSrc = get().getMedia().getSrc();
                }
                // Open the cache folder or parent folder containing the media file.
                try {
                    File openFile = new File(openSrc);
                    if (openSrc.equals(Initializer.APP_CACHE_FOLDER))
                        openFile.mkdirs();
                    else
                        openFile = openFile.getParentFile();
                    Desktop.getDesktop().open(openFile);
                } catch (IOException ioe) { ioe.printStackTrace(); }
                break;
            // PASTE MEDIA
            case KeyEvent.VK_V:
                if (ctrlDown) {
                    try {
                        clipboardPasted();
                    } catch (InvalidTransferMediaException itme) { System.err.println(itme.getMessage()); }
                }
                break;
            // GLOBAL MUTE
            case KeyEvent.VK_M:
                if (ctrlDown && shiftDown)
                    PropertiesManager.mediator.propertyChanged(PiPProperty.GLOBAL_MUTED,
                            String.valueOf(!PropertiesManager.mediator.propertyState(PiPProperty.GLOBAL_MUTED, Boolean.class)));
                break;
            // ADD NEW WINDOW
            case KeyEvent.VK_A:
                if (shiftDown) handoff(null);
                break;
            // RESTART/RELOAD
            case KeyEvent.VK_R:
                if (ctrlDown) {
                    get().flashBorderEDT(PiPWindow.BORDER_OK);
                    sendMediaCMD(PiPMediaCMD.RELOAD, (shiftDown ? "true" : "false"));
                }
                break;
            // DELETE & CONTINUE (CLOSE MEDIA) OR DUPLICATE WINDOW
            case KeyEvent.VK_D:
                // All actions below require shift, return if not pressed.
                if (!shiftDown || !get().hasMedia())
                    break;
                
                // Mark the media for deletion and continue to next case to close.
                if (ctrlDown)
                    get().getMedia().markForDeletion();
                // Duplicate the window.
                else {
                    // Create a new window.
                    final PiPWindow dupeWindow = handoff(null);
                    // Create READY hook and adjust size and location relative to current window.
                    dupeWindow.state().hook(READY, true, () -> {
                        dupeWindow.changeSize(get().getSize(), true);
                        dupeWindow.setLocation(get().getX() + 40, get().getY() + 25);
                        dupeWindow.ensureOnScreen();
                    });
                    // Set media, which eventually executes the above hook or unhooks it if the window fails to load.
                    dupeWindow.setMedia(new PiPMedia(get().getMedia()));
                    break;
                }
            // CLOSE MEDIA
            case KeyEvent.VK_C:
                if (ctrlDown) setWindowMedia(null);
                break;
            // HIDE WINDOW
            case KeyEvent.VK_H:
                if (ctrlDown && shiftDown)
                    get().getListener().hideWindows();
                else if (ctrlDown && get().state().not(CLOSED))
                    get().setVisible(false);
                break;
            // CLOSE WINDOW(S)
            case KeyEvent.VK_ESCAPE:
                if (shiftDown && TopDialog.showConfirm("Are you sure you want to close all windows?", "Clear Windows", JOptionPane.YES_NO_OPTION) == 0)
                    get().getListener().clearWindows();
                else if (!shiftDown)
                    get().requestClose();
                break;
            }
        };

        attributeListener = new AttributeUpdateListener() {
            @Override
            public void allUpdated() {
                if (get().hasAttributedMedia())
                    titleUpdated(get().getMedia().getAttributes().getTitle());
            }
            
            @Override
            public void titleUpdated(String title) {
                // Set title. Per documentation on setTitle(String), null value is treated as an empty string.
                SwingUtilities.invokeLater(() -> get().setTitle(title));
            }
        };

        mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                final boolean ctrlDown = (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0;

                // Right-Click
                if (e.getButton() == MouseEvent.BUTTON3) {
                    // Seek Forward Shortcut
                    if (middleMouseDown)
                        sendMediaCMD(PiPMediaCMD.SEEK, "SKIP", "5000");
                    // Save Point of RMB Click for Potential Drag
                    else
                        dragOrigin = new Point(e.getXOnScreen() - get().getX(), e.getYOnScreen() - get().getY());
                }
                // Left-Click
                else if (e.getButton() == MouseEvent.BUTTON1) {
                    // LMB was already thought to be down. Ensure pan action was stopped.
                    if (leftMouseDown && dragOriginLMB != null)
                        sendMediaCMD(PiPMediaCMD.PAN);

                    leftMouseDown = true;
                    dragOriginLMB = new Point(e.getXOnScreen(), e.getYOnScreen());

                    // Seek Backwards Shortcut
                    if (middleMouseDown) {
                        sendMediaCMD(PiPMediaCMD.SEEK, "SKIP", "-5000");
                        return;
                    }

                    // Double Click
                    if (e.getClickCount() == 2) {
                        // Toggles Fullscreen if Window Has Media
                        if (get().hasMedia())
                            sendMediaCMD(PiPMediaCMD.FULLSCREEN);
                        // Pastes and Sets Clipboard Media Source
                        else {
                            leftMouseDown = false; // Reset state -- Mouse down feels more responsive for multi-click actions.
                            try {
                                clipboardPasted();
                            } catch (InvalidTransferMediaException itme) { System.err.println(itme.getMessage()); }
                        }
                    }
                    
                    // Play/Pause Shortcut
                    if (get().state().not(PLAYER_SWING)) {
                        if (get().state().is(PLAYER_COMBO))
                            sendMediaCMD(PiPMediaCMD.PLAYPAUSE, "true", "true");
                        else
                            sendMediaCMD(PiPMediaCMD.PLAYPAUSE, "false", "true");
                    }
                }
                // Middle-Mouse-Click
                else if (e.getButton() == MouseEvent.BUTTON2) {
                    middleMouseDown = true;

                    // Double Middle-Click Opens a New Empty Window
                    if (e.getClickCount() == 2)
                        handoff(null);
                    else if (ctrlDown && get().state().is(PLAYER_SWING))
                        sendMediaCMD(PiPMediaCMD.ZOOM, "SET", "1.00f", "0", "0");
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // Right-Click
                if (e.getButton() == MouseEvent.BUTTON3) {
                    // Different Set of Commands, So Return
                    if (middleMouseDown)
                        return;

                    // Discard Point of RMB Click for Potential Drag
                    dragOrigin = null;

                    // Triple Right-Click -- Mouse Release Prevents Input on Content Behind Window When Its Size Changes
                    if (e.getClickCount() == 3) {
                        // Closes Window Media
                        if (get().hasMedia()) setWindowMedia(null);
                        // Closes Window If No Media
                        else CompletableFuture.runAsync(() -> get().requestClose());
                    }
                }
                // Left-Click
                else if (e.getButton() == MouseEvent.BUTTON1) {
                    leftMouseDown = false;
                    dragOriginLMB = null;
                    if (get().state().is(PLAYER_SWING)) {
                        sendMediaCMD(PiPMediaCMD.PAN);
                    }
                }
                // Middle-Mouse-Click
                else if (e.getButton() == MouseEvent.BUTTON2) {
                    middleMouseDown = false;

                    // Playback Rate/Speed Reset Shortcut
                    if (dragOrigin != null)
                        sendMediaCMD(PiPMediaCMD.SPEED_ADJUST, "SET", "1.00f");
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                // Adjust as a Multiplier of the Mouse Wheel Clicks (Negated to Give Proper, Default Scroll Direction)
                final int wheelClicks   = -(e.getWheelRotation());
                final boolean shiftDown = (e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0;
                final boolean ctrlDown  = (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK)  != 0;

                // Playback Rate/Speed Adjust Shortcut
                if (dragOrigin != null)
                    sendMediaCMD(PiPMediaCMD.SPEED_ADJUST, "SKIP", Float.toString(wheelClicks * 0.1f));
                // Zoom Adjust Shortcut
                else if (get().state().is(PLAYER_SWING))
                    sendMediaCMD(PiPMediaCMD.ZOOM, "SKIP",
                            Float.toString(wheelClicks * (ctrlDown ? 0.25f : (shiftDown ? 0.05f : 0.10f))),
                            Integer.toString(e.getX() - (get().getInnerWidth() / 2)),
                            Integer.toString(e.getY() - (get().getInnerHeight() / 2)));
                // Volume Adjust Shortcut
                else
                    sendMediaCMD(PiPMediaCMD.VOLUME_ADJUST, "SKIP", Integer.toString(wheelClicks * 3));
            }
        };

        mouseDrag = new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // Only Drag if a Drag Origin is Saved (RMB is Pressed)
                if (dragOrigin != null && get().state().not(FULLSCREEN, LOCKED_POSITION))
                    get().setLocation(e.getXOnScreen() - dragOrigin.x, e.getYOnScreen() - dragOrigin.y);
                else if (dragOriginLMB != null)
                    sendMediaCMD(PiPMediaCMD.PAN, Integer.toString(e.getXOnScreen() - dragOriginLMB.x),
                            Integer.toString(e.getYOnScreen() - dragOriginLMB.y));
            }
        };
    }
    
    /**
     * The code executed by DropTargets when a drop is received.
     * 
     * @param evt - the DropTargetDropEvent passed by a DropTarget.
     */
    private void dropReceived(DropTargetDropEvent evt) {
        evt.acceptDrop(DnDConstants.ACTION_REFERENCE);
        try {
            handleMediaTransfer(evt.getTransferable(), false);
            return;
        } catch(IOException e) {
            System.err.println("Error: IOException while handling DnD data.");
        } catch (UnsupportedFlavorException e) {
            System.err.println("Error: Attempted to get DnD transfer data under unsupported flavor type.");
        } catch (InvalidTransferMediaException e) {
            System.err.println("Error: The current implementation was unable to get valid media from the DnD transfer. (" + e.getMessage() + ")");
        }
        // Handle Failed Drag and Drop
        transferFailed("Could not get media from drag and drop content.");
    }

    /**
     * Called when the user attempts to paste contents from the clipboard. This
     * method will get the contents from the system clipboard. Then, if it will
     * attempt to parse them and retrieve media. If the clipboard contains valid
     * media, it will play it.
     * 
     * @throws InvalidTransferMediaException when the contents of the clipboard are
     *                                       empty or do not contain valid media.
     */
    private void clipboardPasted() throws InvalidTransferMediaException {
        // Get the system clipboard contents and check for validity.
        final Transferable content = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (content == null)
            throw new InvalidTransferMediaException("The clipboard has no contents.");
        
        try {
            handleMediaTransfer(content, true);
            return;
        } catch(IOException e) {
            System.err.println("Error: IOException while handling clipboard data.");
        } catch (UnsupportedFlavorException e) {
            System.err.println("Error: Attempted to get clipboard transfer data under unsupported flavor type.");
        } catch (InvalidTransferMediaException e) {
            System.err.println("Error: The current implementation was unable to get valid media from the clipboard transfer. (" + e.getMessage() + ")");
        }
        // Handle Failed Copy/Paste
        transferFailed("Could not interpret clipboard contents as valid media.");
    }
    
    /**
     * Attempts to load the passed {@link PiPMedia} in the passed {@link PiPWindow}.
     * This method sets hooks on {@link PiPWindowState} properties, allowing it to
     * await certain value changes. It then continues once the hooks execute. This
     * method will pause until loading is complete, then it will check if the media
     * is being closed.
     * <p>
     * If it is, the method will again await completion of this task, then continue
     * to return false if no media is present in the window. That indicates a failed
     * loading attempt.
     * <p>
     * If it is not, the method will not require a second waiting period, returning
     * immediately.
     * 
     * @param win   - the {@link PiPWindow} to load the media in.
     * @param media - the {@link PiPMedia} to load in the window.
     * @return <code>true</code> if the load attempt was successful;
     *         <code>false</code> otherwise.
     */
    private boolean attemptLoad(final PiPWindow win, final PiPMedia media) {
        // Setup latches and hooks.
        final CountDownLatch loadingLatch = new CountDownLatch(1);
        final CountDownLatch closingLatch = new CountDownLatch(1);
        win.state().hook(LOADING, false, () -> loadingLatch.countDown());
        win.state().hook(CLOSING_MEDIA, false, () -> closingLatch.countDown());
        // Set media.
        win.setMedia(media);
        // Await loading completion -- either success or failure.
        try {
            loadingLatch.await();
        } catch (InterruptedException e) { System.err.println("Unexpected interruption during D&D loading latch."); }
        // If closing the media, assume failure and await for closing to complete.
        if (win.state().is(CLOSING_MEDIA))  {
            try {
                closingLatch.await();
            } catch (InterruptedException e) { System.err.println("Unexpected interruption during D&D closing latch."); }
        }
        // Remove hook in case media loaded successfully -- latch only needed for invalid media attempts.
        win.state().unhook(CLOSING_MEDIA, false);
        // If the window has no media, confirmed failed loading attempt. Close window and throw error to try another flavor in a new window.
        if (!win.hasMedia()) return false;
        return true;
    }

    /**
     * Handles the transfer of media from a drag and drop event or the system
     * clipboard to a window.
     * 
     * @param t               - the {@link Transferable} potentially containing media.
     * @param clipboardSrc    - a boolean for whether or not the transfer originated from the clipboard.
     * @param flavorOverrides - any {@link MediaFlavor} values which should override and take precedence
     *                          over the typical order.
     * @throws IOException                   when there are input/output errors with
     *                                       the potential media.
     * @throws UnsupportedFlavorException    when there are transferable contents,
     *                                       but it is not in an acceptable format.
     * @throws InvalidTransferMediaException when the transfer media is not valid.
     */
    @SuppressWarnings("unchecked")
    private void handleMediaTransfer(Transferable t, boolean clipboardSrc, MediaFlavor... flavorOverrides) throws IOException, UnsupportedFlavorException, InvalidTransferMediaException {
        if (t == null) {
            System.err.println("Error: Nothing found in " + (clipboardSrc ? "clipboard." : "drag and drop."));
            return;
        }
        
        // Handoff the media to another window if current window already has media.
        final boolean OCCUPIED = get().hasMedia();
        // Get Prefer Link Configuration Status
        final boolean PREFER_LINK = get().propertyState(PiPProperty.DND_PREFER_LINK, Boolean.class);
        
        // Ensure clipboard directory exists.
        if (clipboardSrc) PiPAAUtils.ensureExistence(Initializer.APP_CLIPBOARD_FOLDER);

        // ##### START 0.9.4-SNAPSHOT New Approach (IN TESTING)
        final boolean hasFlavorOverrides = flavorOverrides != null && flavorOverrides.length > 0;
        final MediaFlavorPicker picker = new MediaFlavorPicker(hasFlavorOverrides
            ? flavorOverrides
            : (clipboardSrc
            ? new MediaFlavor[] { IMAGE, STRING, FILE, WEB_URL }
            : (PREFER_LINK
            ? new MediaFlavor[] { WEB_URL, FILE, IMAGE, STRING }
            : MediaFlavorPicker.DEFAULT_PICK_ORDER)))
        .support(STRING,  t.isDataFlavorSupported(DataFlavor.stringFlavor))
        .support(IMAGE,   t.isDataFlavorSupported(DataFlavor.imageFlavor))
        .support(FILE,    t.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
        .support(WEB_URL, t.isDataFlavorSupported(flavorWebURL));
        // Get transfer data for all supported media flavors.
        // For files, also relocate any and all files originating in the temporary directory to the PiPAA cache. See method doc. for why.
        final String        dataString = (picker.supports(STRING)  ? (String) t.getTransferData(DataFlavor.stringFlavor)                              : null);
        final BufferedImage  dataImage = (picker.supports(IMAGE)   ? (BufferedImage) t.getTransferData(DataFlavor.imageFlavor)                        : null);
        final List<File>      dataFile = (picker.supports(FILE)    ? relocateTempFiles((List<File>) t.getTransferData(DataFlavor.javaFileListFlavor)) : null);
        final URL           dataWebURL = (picker.supports(WEB_URL) ? (URL) t.getTransferData(flavorWebURL)                                            : null);
        
        // Pick the next supported media flavor, attempt to handle it, and repeat until completion.
        CompletableFuture.runAsync(() -> {
            MediaFlavor pick = picker.pick();
            while(pick != null) {
                System.err.println("##### Handling Media -- MediaFlavor Pick: " + pick);
                try {
                    switch (pick) {
                    case FILE    -> handleFileListDrop(dataFile, OCCUPIED);
                    case IMAGE   -> handleImageDrop(dataImage, OCCUPIED);
                    case STRING  -> {
                        /* String flavored media should not require a complex approach as seen with WEB_URL...for now.
                         * If flavor order preference becomes customizable via the configuration, that should change. */
                        if (OCCUPIED) handoff(new PiPMedia(dataString));
                        else  setWindowMedia(new PiPMedia(dataString));
                    }
                    case WEB_URL -> {
                        final URL url = dataWebURL;
                        
                        if (OCCUPIED) {
                            // Attempt loading of media in new window.
                            // If the window has no media, confirmed failed loading attempt. Close window and throw error to try another flavor in a new window.
                            final PiPWindow win = handoff(null);
                            if (!attemptLoad(win, new PiPMedia(url.toString()))) {
                                win.requestClose();
                                throw new InvalidTransferMediaException("Web URL media flavor failed. Trying next.");
                            }
                        } else {
                            // Attempt loading of media in this window.
                            // If the window has no media, confirmed failed loading attempt. Update title and throw error to try another flavor.
                            if (!attemptLoad(get(), new PiPMedia(url.toString()))) {
                                get().titleStatusUpdate("[Loading: No URL...]");
                                throw new InvalidTransferMediaException("Web URL media flavor failed. Trying next.");
                            }
                        }
                    }
                    }
                    // If reached: Indicates the media was accepted -- success.
                    System.err.println("##### Done Handling Media");
                    break;  // Break outside of loading while loop into cleanup section below.
                } catch (IOException | InvalidTransferMediaException e) {
                    System.err.println("Flavor Pick [" + Objects.toString(pick, "<null>") + "]: " + "[" + e.getClass().getSimpleName() + "] " + e.getMessage());
                }
                pick = picker.pick();
            }
            // Delete leftover URL files placed in cache, if any are present.
            if (dataFile  != null) dataFile.stream()
                .filter(f -> f.getPath().toLowerCase().endsWith(".url"))
                .filter(f -> f.getPath().startsWith(new File(Initializer.APP_CLIPBOARD_FOLDER).getPath()))
                .forEach(f -> f.delete());
            // Flush image at the end if not null, ensuring no memory leak.
            if (dataImage != null) dataImage.flush();
        });
        // ##### END 0.9.4-SNAPSHOT New Approach
    }
    
    /**
     * Handles the transfer of raw images.
     * 
     * @param img - the BufferedImage containing the raw image.
     * @param occupied - a boolean for whether or not this media should be handed off to a new window instead of the current one.
     * @throws IOException when there are input/output errors with the image.
     */
    private void handleImageDrop(BufferedImage img, boolean occupied) throws IOException {
        // Try as IMAGE FLAVOR
        String name = null;
        File outFile = null;
        while (outFile == null || outFile.exists()) {
            name = "/cachedImg" + (int) (Math.random() * 100000) + ".png";
            outFile = new File(Initializer.APP_CLIPBOARD_FOLDER + name);
        }
        outFile.mkdirs();
        ImageIO.write(img, "png", outFile);
        img.flush();
        img = null;
        final String outPath = outFile.getPath();
        if (occupied) {
            if (get().hasAttributedMedia() && MediaExt.supportsArtwork(get().getMedia().getAttributes().getFileExtension())) {
                // Prompt user to either replace artwork or open media.
                new ArtSelectionPopup(PropDefault.THEME.matchAny(get().propertyState(PiPProperty.THEME, String.class))).moveRelTo(get())
                    .setReceiver((i) -> {
                        switch (i) {
                        case 0 -> get().replaceArtwork(outPath);
                        case 1 -> handoff(possiblyMarkedMedia(outPath));
                        }
                    }).display();
            }
            else handoff(possiblyMarkedMedia(outPath));
        }
        else setWindowMedia(possiblyMarkedMedia(outPath));
//        System.err.println("image copied to: " + outfile.getAbsolutePath());  // Debug
    }
    
    /**
     * Handles the transfer of one or more media files.
     * 
     * @param files - a List of File objects connected to potential media sources.
     * @param occupied - a boolean for whether or not this media should be handed off to a new window instead of the current one.
     * @throws InvalidTransferMediaException when one or more media file(s) is not valid.
     */
    private void handleFileListDrop(List<File> files, boolean occupied) throws InvalidTransferMediaException {
        // Loop through each file 
        for (int f = 0; f < files.size(); f++) {
            final File droppedFile = files.get(f);
            
            // Debug
//            System.out.println("NAME: " + droppedFile.getName() + " | PATH: " + droppedFile.getPath() + " | EXISTS? " + droppedFile.exists());
//            System.out.println(droppedFile.getPath());
//            System.out.println(new File(Initializer.APP_CLIPBOARD_FOLDER).getPath());
//            System.out.println(System.getProperty("java.io.tmpdir"));
            /* Since 0.9.4-SNAPSHOT, it is assumed that temporary directory files have already been moved to the cache prior to calling this method. */
            final boolean fileInTemp = droppedFile.getPath().startsWith(new File(Initializer.APP_CLIPBOARD_FOLDER).getPath());
            
            // Only prompt the user for artwork replacement if on the first file and the window has attributed, artwork-supporting media. 
            if (f == 0 && get().hasAttributedMedia() && MediaExt.supportsArtwork(get().getMedia().getAttributes().getFileExtension())) {
                // Perform quick attribution and only continue if the file media is supported as artwork.
                final PiPMediaAttributes attributes = get().getListener().requestAttributes(getMediaFromFile(droppedFile, fileInTemp), Flag.QUICK);
                if (MediaExt.supportedAsArtwork(attributes.getFileExtension())) {
                    // Prompt user to either replace artwork or open media.
                    final UnsetBool replaceArtwork = new UnsetBool();
                    new ArtSelectionPopup(PropDefault.THEME.matchAny(get().propertyState(PiPProperty.THEME, String.class))).moveRelTo(get())
                        .setReceiver((i) -> {
                            switch (i) {
                            case 0 -> { // Replace Artwork
                                replaceArtwork.set(true);
                                get().replaceArtwork(droppedFile.getPath());
                            }
                            case 1 -> { // Open Media
                                replaceArtwork.set(false);
                                handoff(getMediaFromFile(droppedFile, fileInTemp));
                            }
                            }
                        }).display().block(10);
                    
                    // Opened media. Read next files in loop.
                    if (replaceArtwork.isFalse()) continue;     // False:       Continue to handle next files.
                    // Replaced artwork or canceled action.
                    else                          break;        // True/Unset:  End loop early. Ignore other files.
                }
            }
            
            // If multiple files were dropped, pass them off to open another PiPWindow for each.
            if (f > 0) {
                System.out.println("Extra File " + (f+1));
                handoff(getMediaFromFile(droppedFile, fileInTemp));
                continue;
            }
            
            // Finally, set the window media.
            if (occupied) handoff(getMediaFromFile(droppedFile, fileInTemp));
            else  setWindowMedia(getMediaFromFile(droppedFile, fileInTemp));
        }
    }
    
    /**
     * Relocates any {@link File} objects in the passed {@link List} within the
     * temporary directory into the PiPAA cache.
     * <p>
     * <b>It is advised to call this method early.</b> Files in the temporary folder
     * <i>are</i> temporary and can almost immediately be overwritten or deleted.
     * Immediately relocating files that <i>may</i> be needed into the PiPAA cache
     * folder ensures there is no loss.
     * <p>
     * For example, if dragging and dropping an image <b>file</b> from a browser,
     * the system may download the image into the temporary directory first. If that
     * file remains in that directory while another similar drag and drop action is
     * performed, that file may be overwritten and PiPAA will not be able to load
     * the first image.
     * <p>
     * The temporary folder is determined via: <code>System.getProperty("java.io.tmpdir")</code>
     * 
     * @param files - a {@link List} of {@link File} objects to relocate out of the
     *              temporary directory, if they are even in it.
     * @return an adjusted {@link List} of {@link File} objects with their new
     *         paths. If no files originated in the temporary directory, then there
     *         should be no difference between the passed and returned lists.
     * @throws InvalidTransferMediaException if the first file throws an
     *                                       {@link IOException} during processing.
     */
    private List<File> relocateTempFiles(final List<File> files) throws InvalidTransferMediaException {
        final List<File> relocatedFiles = new ArrayList<File>();
        // Loop through each file
        for (int f = 0; f < files.size(); f++) {
            File file = files.get(f);
            
            // If file was copied from non-local source, it can be put in the TEMP directory until used. Save it by moving to cache folder.
            if (file.getPath().startsWith(System.getProperty("java.io.tmpdir"))) {
                /*
                 * Perform cache check on file. If it exists (its contents/raw data), that is returned.
                 * The move will fail, since the file will already exist, which works perfectly and it will be loaded.
                 * If it doesn't exist, it will be given a random unique name and returned.
                 * The move method below will rename it and move the file there.
                 */
                final File movedFile = PiPAAUtils.fileCacheCheck(file);
                try {
                    Files.move(file.toPath(), movedFile.toPath());
                } catch (FileAlreadyExistsException faee) {
                    // Ignore. File already existing is fine, since that's the point of the cache.
                } catch (IOException ioe) {
                    if (f == 0) throw new InvalidTransferMediaException("File not valid or does not exist.");
                    else continue;
                }
                file = movedFile;
            }
            relocatedFiles.add(file);
        }
        return relocatedFiles;
    }
    
    /**
     * Attempts to get media from the passed File. This method will scan
     * <code>.url</code> files for their URL link. While doing so, it will also
     * ensure that no resource leaks occur. This method will <b>only</b> return
     * <code>null</code> if the passed File is <code>null</code>. Otherwise, it
     * should always return a PiPMedia object, even if that media will ultimately be
     * invalid.
     * 
     * @param f       - the File to attempt to get media from.
     * @param fromTmp - a boolean for whether or not this file originated from the
     *                temp directory, indicating that it was not originally a local
     *                file.
     * @return a PiPMedia object with the media, or <code>null</code> if the passed
     *         File was <code>null</code>.
     */
    private PiPMedia getMediaFromFile(File f, boolean fromTmp) {
        // Cannot get media from a null File.
        if (f == null)
            return null;
        
        PiPMedia rtnMedia = null;
        
        // If URL File, Scan for URL Link
        if (f.getPath().toLowerCase().endsWith(".url")) {
            // Drag & Drop Likely a Web Image/GIF/Link to Media via a .url file (likely placed in %temp% after DnD)
            // try-with-resources block ensures resources are closed after execution.
            try (final FileInputStream fis = new FileInputStream(f); final Scanner scanner = new Scanner(fis);){
                // Loop until the URL line is found, if one exists.
                while(scanner.hasNextLine()) {
                    final String line = scanner.nextLine();
                    if(line.toLowerCase().startsWith("url=")) {
                        rtnMedia = new PiPMedia(line.substring(4));
                        break;
                    }
                }
            } catch (IOException ieo) {
                System.err.println("Error while attempting to get media from URL file.");
            }
        }
        
        // General File or URL File Failed - Consider marking media for deletion if file originated from temp directory.
        if (rtnMedia == null)
            rtnMedia = (fromTmp ? possiblyMarkedMedia(f.getPath()) : new PiPMedia(f.getPath()));
        
        return rtnMedia;
    }
    
    /**
     * Returns a PiPMedia that is possible marked for deletion, depending on the
     * current application configuration. Specifically, if the cache is disabled,
     * the media will be marked for deletion.
     * 
     * @param src - the String source of the media.
     * @return a new PiPMedia object which may be already marked for deletion.
     */
    private PiPMedia possiblyMarkedMedia(String src) {
        if (get().propertyState(PiPProperty.DISABLE_CACHE, Boolean.class))
            return new PiPMedia(src).markForDeletion().setCacheSrc(src);
        else
            return new PiPMedia(src).setCacheSrc(src);
    }
    
    /**
     * Retrieves the drag and drop DropTarget listener.
     * 
     * @return the DropTarget listener.
     */
    public DropTarget dndTarget() {
        return this.dndTarget;
    }
    
    /**
     * Retrieves the secondary drag and drop DropTarget listener. There are multiple
     * DropTargets, since having certain components share the same one results in
     * the inability to drag and drop in some cases.
     * 
     * @return the secondary DropTarget listener.
     */
    public DropTarget dndTargetSecondary() {
        return this.dndTargetSecondary;
    }
    
    /**
     * Retrieves the {@link KeyListener} used for keyboard inputs.
     * 
     * @return the window-wide KeyListener.
     */
    public KeyListener keyListener() {
        return this.keyListener;
    }
    
    /**
     * Retrieves the AttributeUpdateListener listener for listening to attribute updates.
     * 
     * @return the AttributeUpdateListener listener.
     */
    public AttributeUpdateListener attributeListener() {
        return this.attributeListener;
    }
    
    /**
     * Retrieves the MouseAdapter listener used for mouse inputs.
     * 
     * @return the MouseAdapter listener.
     */
    public MouseAdapter mouseAdapter() {
        return mouseAdapter;
    }
    
    /**
     * Retrieves the MouseMotionAdapter listener used for mouse drag inputs.
     * 
     * @return the MouseMotionAdapter listener.
     */
    public MouseMotionAdapter mouseDrag() {
        return mouseDrag;
    }
    
    // Listener Methods
    @Override
    public void setWindowMedia(PiPMedia media)  {
        if (SwingUtilities.isEventDispatchThread()) CompletableFuture.runAsync(() -> get().setMedia(media));
        else get().setMedia(media);
    }
    @Override
    public void transferFailed(String msg)      { get().statusUpdate(msg); }
    @Override
    public PiPWindow handoff(PiPMedia media)    { return get().getListener().handoff(media); }
    @Override
    public PiPMediaAttributes requestAttributes(PiPMedia media, Flag... flags) { return get().getListener().requestAttributes(media, flags); }
}
