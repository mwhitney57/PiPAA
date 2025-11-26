package dev.mwhitney.gui;

import static dev.mwhitney.gui.PiPWindowState.StateProp.CLOSED;
import static dev.mwhitney.gui.PiPWindowState.StateProp.CLOSING;
import static dev.mwhitney.gui.PiPWindowState.StateProp.CLOSING_MEDIA;
import static dev.mwhitney.gui.PiPWindowState.StateProp.FLIP_HORIZONTAL;
import static dev.mwhitney.gui.PiPWindowState.StateProp.FLIP_VERTICAL;
import static dev.mwhitney.gui.PiPWindowState.StateProp.FULLSCREEN;
import static dev.mwhitney.gui.PiPWindowState.StateProp.LOADING;
import static dev.mwhitney.gui.PiPWindowState.StateProp.LOCALLY_MUTED;
import static dev.mwhitney.gui.PiPWindowState.StateProp.LOCKED_FULLSCREEN;
import static dev.mwhitney.gui.PiPWindowState.StateProp.LOCKED_MEDIA;
import static dev.mwhitney.gui.PiPWindowState.StateProp.LOCKED_POSITION;
import static dev.mwhitney.gui.PiPWindowState.StateProp.LOCKED_SIZE;
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
import java.awt.event.MouseEvent;
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
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import dev.mwhitney.gui.binds.BindDetails;
import dev.mwhitney.gui.binds.BindHandler;
import dev.mwhitney.gui.binds.BindHook;
import dev.mwhitney.gui.binds.HSMBTracker;
import dev.mwhitney.gui.binds.KeyInput;
import dev.mwhitney.gui.binds.MouseInput;
import dev.mwhitney.gui.binds.Shortcut;
import dev.mwhitney.gui.components.better.BetterTextArea;
import dev.mwhitney.gui.popup.ArtSelectionPopup;
import dev.mwhitney.gui.popup.LockSelectionPopup;
import dev.mwhitney.gui.popup.SelectionPopup;
import dev.mwhitney.gui.popup.TopDialog;
import dev.mwhitney.listeners.PiPCommandListener;
import dev.mwhitney.listeners.PiPHandoffListener;
import dev.mwhitney.listeners.PiPMediaTransferListener;
import dev.mwhitney.listeners.PiPWindowListener;
import dev.mwhitney.main.Binaries;
import dev.mwhitney.media.MediaExt;
import dev.mwhitney.media.MediaFlavorPicker;
import dev.mwhitney.media.MediaFlavorPicker.MediaFlavor;
import dev.mwhitney.media.PiPMedia;
import dev.mwhitney.media.PiPMediaAttributes;
import dev.mwhitney.media.PiPMediaCMD;
import dev.mwhitney.media.attribution.AttributeRequestListener;
import dev.mwhitney.media.attribution.AttributeUpdateListener;
import dev.mwhitney.media.attribution.AttributionFlag;
import dev.mwhitney.media.attribution.AttributionRequest;
import dev.mwhitney.media.exceptions.InvalidTransferMediaException;
import dev.mwhitney.properties.PiPProperty;
import dev.mwhitney.properties.PiPProperty.PropDefault;
import dev.mwhitney.properties.PropertiesManager;
import dev.mwhitney.resources.AppRes;
import dev.mwhitney.util.PiPAAUtils;
import dev.mwhitney.util.UnsetBool;
import dev.mwhitney.util.selection.ReloadSelection.ReloadSelections;

/**
 * The listeners for PiPWindows and their components, especially those relating
 * to media transfers such as drag and drop and copy/paste.
 * 
 * @author mwhitney57
 */
public abstract class PiPWindowListeners implements PiPWindowListener, PiPCommandListener, PiPMediaTransferListener, PiPHandoffListener, AttributeRequestListener, BindHandler {
    /** A DataFlavor containing a web URL. */
    private DataFlavor flavorWebURL;
    
    /** A DropTarget for drag 'n drop or clipboard media transfers. */
    private final DropTarget dndTarget;
    /** A DropTarget for drag 'n drop or clipboard media transfers that should be handed off to a new window. */
    private final DropTarget dndTargetSecondary;
    /** The AttributeUpdateListener which listens for updates to each window's media's attributes. */
    private final AttributeUpdateListener attributeListener;
    /**
     * The high-speed mouse button tracker. Used instead of {@link BindHook} data
     * for maximum speed and readability. This is used for drag events, which must
     * be as fast as possible. While {@link BindHook} should be fast, this approach
     * should be faster, simpler, and certainly more readable.
     */
    private final HSMBTracker mouse = new HSMBTracker();
    /** The keyboard and mouse hook which triggers bind actions when certain configured inputs are received that match shortcuts. */
    private final BindHook kbmHook;
    
    /**
     * Creates a new window listeners instance. Each instance handles multiple
     * listeners and related logic.
     */
    public PiPWindowListeners() {
        // Window null safety check warning.
        if (get() == null) System.err.println("<!> Critical window error: get() returned null PiPWindow during listeners instance construction.");
        
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
        
        kbmHook = new BindHook(get().getManager().getController()) {
            @Override
            public void onKeyBind(BindDetails<KeyInput> bind)       { handleShortcutBind(bind); }
            @Override
            public void onKeyBindUp(BindDetails<KeyInput> bind)     { handleShortcutBind(bind); }
            @Override
            public void onMouseBind(BindDetails<MouseInput> bind)   { handleShortcutBind(bind); }
            @Override
            public void onMouseBindUp(BindDetails<MouseInput> bind) { handleShortcutBind(bind); }
            @Override
            public void onScrollBind(BindDetails<MouseInput> bind)  { handleShortcutBind(bind); }
            @Override
            public void onMouse(MouseInput input) {
                switch(input.code()) {
                case MouseInput.LMB:
                    // Just store on-screen position for PAN actions.
                    mouse.usePointLMB(new Point(input.getOnScreenX(), input.getOnScreenY()));
                    // LMB was already thought to be down. Ensure pan action was stopped before another is potentially started.
                    if (get().state().is(PLAYER_SWING) && mouse.isLMB())
                        sendMediaCMD(PiPMediaCMD.PAN);
                    // Paste Media Shortcut - Default, Built-in Bind. Cannot be reconfigured. However, additional binds may be added for the shortcut.
                    if (input.isDoubleClick() && !get().hasMedia())
                        handleShortcutBind(BindDetails.createDummy(Shortcut.PASTE_MEDIA));
                    break;
                case MouseInput.MMB:
                    mouse.onMMB();
                    break;
                case MouseInput.RMB:
                    // Store difference between on screen points and in-window points.
                    mouse.usePointRMB(new Point(input.getOnScreenX() - get().getX(), input.getOnScreenY() - get().getY()));
                    break;
                }
            }
            @Override
            public void onMouseUp(MouseInput input) {
                switch(input.code()) {
                case MouseInput.LMB:
                    mouse.offLMB();
                    // Ensure PAN is cancelled on LMB release.
                    if (get().state().is(PLAYER_SWING)) sendMediaCMD(PiPMediaCMD.PAN);
                    break;
                case MouseInput.MMB:
                    mouse.offMMB();
                    break;
                case MouseInput.RMB:
                    mouse.offRMB();
                    break;
                }
            }
            @Override
            public void onScroll(MouseInput input) {
                // Adjust as a Multiplier of the Mouse Wheel Clicks (Negated to Give Proper, Default Scroll Direction)
                final int wheelClicks   = -(input.getWheelRotation());
                final boolean shiftDown = input.maskDown(KeyEvent.SHIFT_DOWN_MASK);
                final boolean ctrlDown  = input.maskDown(KeyEvent.CTRL_DOWN_MASK);
                
                // Zoom Shortcuts - Default, Built-in Bind. Cannot be reconfigured. However, additional binds may be added for these shortcuts.
                if (get().state().is(PLAYER_SWING)) {
                    final Shortcut zoomShortcut = ( wheelClicks > 0
                        ? (ctrlDown ? Shortcut.ZOOM_IN_MORE  : (shiftDown ? Shortcut.ZOOM_IN_LESS  : Shortcut.ZOOM_IN))
                        : (ctrlDown ? Shortcut.ZOOM_OUT_MORE : (shiftDown ? Shortcut.ZOOM_OUT_LESS : Shortcut.ZOOM_OUT))
                    );
                    get().handleShortcutBind(new BindDetails<MouseInput>(zoomShortcut, input));
                }
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                // Only Drag if RMB is Pressed and Has Point
                if (mouse.hasPointRMB() && get().state().not(FULLSCREEN, LOCKED_POSITION)) {
                    // Save location from before drag in case its needed for determining distance.
                    final Point prevPos = get().getLocation();
                    // Move dragged window.
                    get().setLocation(e.getXOnScreen() - mouse.getPointRMB().x, e.getYOnScreen() - mouse.getPointRMB().y);
                    // Check if all other windows should be moved as well.
                    if (e.isShiftDown()) {
                        // Get the dragged window's new and current position.
                        final Point currPos = get().getLocation();
                        get().getManager().callInLiveWindows(window -> {
                            // Don't set location again for window which drag originated in. Also skip if window is in fullscreen or position locked.
                            if (window == get() || window.state().any(FULLSCREEN, LOCKED_POSITION)) return;
                            // Get window's X/Y distance from originating window's previous position.
                            final int distX = window.getX() - prevPos.x;
                            final int distY = window.getY() - prevPos.y;
                            // Set window's location to be the same distance, but from the originating window's new location.
                            window.setLocation(currPos.x + distX, currPos.y + distY);
                        });
                    }
                }
                // Only Pan if LMB is Pressed and Has Point
                else if (mouse.hasPointLMB())
                    sendMediaCMD(PiPMediaCMD.PAN, Integer.toString(e.getXOnScreen() - mouse.getPointLMB().x),
                            Integer.toString(e.getYOnScreen() - mouse.getPointLMB().y));
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
        win.state().hook(LOADING, false, loadingLatch::countDown);
        win.state().hook(CLOSING_MEDIA, false, closingLatch::countDown);
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
        if (clipboardSrc) PiPAAUtils.ensureExistence(AppRes.APP_CLIPBOARD_FOLDER);

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
        
        // Pick the next supported media flavor, attempt to handle it, and repeat until completion. Don't continue if window is closing though.
        CompletableFuture.runAsync(() -> {
            MediaFlavor pick = picker.pick();
            while (pick != null && get().state().not(CLOSING)) {
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
                    if (get().state().any(CLOSING, CLOSED)) {
                        System.err.println("Flavor picking and media loading process aborted. Window closing...");
                        break;
                    }
                    else System.err.println("Flavor Pick [" + Objects.toString(pick, "<null>") + "]: " + "[" + e.getClass().getSimpleName() + "] " + e.getMessage());
                }
                pick = picker.pick();
            }
            // Delete leftover URL files placed in cache, if any are present.
            if (dataFile  != null) dataFile.stream()
                .filter(f -> f.getPath().toLowerCase().endsWith(".url"))
                .filter(f -> f.getPath().startsWith(new File(AppRes.APP_CLIPBOARD_FOLDER).getPath()))
                .forEach(f -> f.delete());
            // Flush image at the end if not null, ensuring no memory leak.
            if (dataImage != null) dataImage.flush();
        });
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
            outFile = new File(AppRes.APP_CLIPBOARD_FOLDER + name);
        }
        outFile.mkdirs();
        ImageIO.write(img, "png", outFile);
        img.flush();
        img = null;
        final String outPath = outFile.getPath();
        if (occupied) {
            if (get().hasAttributedMedia() && MediaExt.supportsArtwork(get().getMedia().getAttributes().getFileExtension())) {
                // Prompt user to either replace artwork or open media.
                SelectionPopup.showAndBlock(() -> new ArtSelectionPopup(
                        PropDefault.THEME.matchAny(get().propertyState(PiPProperty.THEME, String.class)))
                        .setReceiver(i -> CompletableFuture.runAsync(i == 0 ? () -> get().replaceArtwork(outPath)
                                : () -> handoff(possiblyMarkedMedia(outPath))))
                        .moveRelTo(get()).display());
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
            final boolean fileInTemp = droppedFile.getPath().startsWith(new File(AppRes.APP_CLIPBOARD_FOLDER).getPath());
            
            // Only prompt the user for artwork replacement if on the first file and the window has attributed, artwork-supporting media. 
            if (f == 0 && get().hasAttributedMedia() && MediaExt.supportsArtwork(get().getMedia().getAttributes().getFileExtension())) {
                // Perform quick attribution and only continue if the file media is supported as artwork.
                final PiPMediaAttributes attributes = requestAttributes(get(), getMediaFromFile(droppedFile, fileInTemp), AttributionFlag.QUICK);
                if (MediaExt.supportedAsArtwork(attributes.getFileExtension())) {
                    // Prompt user to either replace artwork or open media.
                    final UnsetBool replaceArtwork = new UnsetBool();
                    SelectionPopup.showAndBlock(() -> new ArtSelectionPopup(
                            PropDefault.THEME.matchAny(get().propertyState(PiPProperty.THEME, String.class)))
                            .setReceiver(i -> {
                                replaceArtwork.set(i == 0 ? true : false);
                                CompletableFuture.runAsync(replaceArtwork.isTrue()
                                        ? () -> get().replaceArtwork(droppedFile.getPath())
                                        : () -> handoff(getMediaFromFile(droppedFile, fileInTemp)));
                            }).moveRelTo(get()).display(), 10);

                    // Opened media. Read next files in loop.
                    if (replaceArtwork.isFalse()) continue;
                    // Replaced artwork or canceled action. End loop early. Ignore other files.
                    break;
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
     * Retrieves the {@link BindHook} used for keyboard and mouse inputs.
     * 
     * @return the window-wide keyboard and mouse hook.
     */
    public BindHook kbmHook() {
        return this.kbmHook;
    }
    
    /**
     * Retrieves the AttributeUpdateListener listener for listening to attribute updates.
     * 
     * @return the AttributeUpdateListener listener.
     */
    public AttributeUpdateListener attributeListener() {
        return this.attributeListener;
    }
    
    // Listener Methods
    @Override
    public void setWindowMedia(PiPMedia media)  { PiPAAUtils.invokeNowOrAsync(() -> get().setMedia(media)); }
    @Override
    public void transferFailed(String msg)      { get().statusUpdate(msg); }
    @Override
    public PiPWindow handoff(PiPMedia media)    { return get().getListener().handoff(media); }
    @Override
    public PiPMediaAttributes requestAttributes(AttributionRequest req) { return get().getListener().requestAttributes(req); }
    @Override
    public void handleShortcutBind(BindDetails<?> bind) {
        // Null safety check.
        if (bind == null) {
            System.err.println("<!> Received null BindDetails when handling window-wide key control.");
            return;
        }
        
        /*
         * Shortcut Hierarchy:
         * Handling for shortcuts starts here. If not handled here, the window receives the shortcut, then the manager.
         * This is a great place to handle GUI-related tasks, as this is called on the EDT directly from listeners.
         */
        final Shortcut shortcut = bind.shortcut();
        switch (shortcut) {
        // Add a new empty window.
        case ADD_WINDOW:
            handoff(null);
            break;
        // Close either the current media or the entire window.
        case CLOSE_FLEX:
            if (get().hasMedia()) setWindowMedia(null);
            else CompletableFuture.runAsync(get()::requestClose);
            break;
        // Pop-up with lock selection options for user to decide.
        case LOCK_WINDOW_MENU:
            new LockSelectionPopup(PropDefault.THEME.matchAny(get().propertyState(PiPProperty.THEME, String.class)), get().state()).moveRelTo(get()).display();
            break;
        // Enable size & pos locks. TODO Allow customization instead for this option in config. User can check select each they want to be able to enable.
        case LOCK_WINDOW_SIZEPOS:
            get().state().on(LOCKED_SIZE, LOCKED_POSITION);
            break;
        // Disable all locks.
        case LOCK_WINDOW_ALLOFF:
            get().state().off(LOCKED_SIZE, LOCKED_POSITION, LOCKED_FULLSCREEN, LOCKED_MEDIA);
            break;
        // SHOW KEYBOARD SHORTCUTS
        case KEYBOARD_SHORTCUTS:
            final BetterTextArea shortcutsComp = new BetterTextArea(AppRes.SHORTCUTS);
            TopDialog.showMsg(shortcutsComp, "Keyboard and Mouse Shortcuts", JOptionPane.PLAIN_MESSAGE);
            break;
        // OPEN LOCAL/CACHED FILE DIRECTORY
        case OPEN_MEDIA_DIRECTORY:
        case OPEN_CACHE_DIRECTORY:
            // Should the cached folder locations be shown over any non-cached, local location of the media?
            final boolean showCache = shortcut == Shortcut.OPEN_CACHE_DIRECTORY;
            // Default to cache folder.
            String openSrc = AppRes.APP_CACHE_FOLDER;
            if (get().hasAttributedMedia()) {
                // Determine local media location (if any)
                final PiPMedia media = get().getMedia();
                openSrc = showCache && media.hasTrimSrc()    ? media.getTrimSrc()         // Use Trimmed Cache
                        : showCache && media.hasConvertSrc() ? media.getConvertSrc()      // Use Convert Cache
                        : media.isCached()                   ? media.getCacheSrc()        // Use Cache
                        : media.getAttributes().isLocal()    ? media.getSrc() : openSrc;  // Use Local
            }
            // Open the cache folder or parent folder containing the media file.
            try {
                File openFile = new File(openSrc);
                if (openSrc.equals(AppRes.APP_CACHE_FOLDER)) {
                    openFile.mkdirs();
                    Desktop.getDesktop().open(openFile);
                } else {
                    final String openCmd = "explorer.exe /select,\"" + openFile.getAbsolutePath() + "\"";
                    Binaries.exec("cmd.exe", "/c", openCmd);
                }
            } catch (IOException ioe) { ioe.printStackTrace(); }
            break;
        // PASTE MEDIA
        case PASTE_MEDIA:
            try {
                clipboardPasted();
            } catch (InvalidTransferMediaException itme) { System.err.println(itme.getMessage()); }
            break;
        // FOCUS CHANGES
        case SEND_TO_BACK:
            get().getManager().callInLiveWindows(w -> {
                if (w != get()) w.requestFocus();
            });
            break;
        case SEND_TO_FRONT:
            get().requestFocus();
            break;
        // GLOBAL MUTE
        case GLOBAL_MUTE:
            PropertiesManager.mediator.propertyChanged(PiPProperty.GLOBAL_MUTED,
                    String.valueOf(!PropertiesManager.mediator.propertyState(PiPProperty.GLOBAL_MUTED, Boolean.class)));
            break;
        // RESTART/RELOAD
        case RELOAD:
        case RELOAD_QUICK:
            sendMediaCMD(PiPMediaCMD.RELOAD, shortcut == Shortcut.RELOAD ? ReloadSelections.REGULAR : ReloadSelections.QUICK);
            break;
        // DELETE & CONTINUE (CLOSE MEDIA) OR DUPLICATE WINDOW
        case DELETE_MEDIA:
            if (get().hasMedia()) {
                get().getMedia().markForDeletion();
                setWindowMedia(null);
            }
            break;
        case DUPLICATE_WINDOW:
            final PiPWindow dupeWindow = handoff(null);
            final Runnable duplication = () -> {
                dupeWindow.changeSize(get().getSize(), true);
                dupeWindow.setLocation(get().getX() + 40, get().getY() + 25);
                dupeWindow.ensureOnScreen();
            };
            // Media-pending duplication translations.
            if (get().hasMedia()) {
                // Create READY hook and adjust size and location relative to current window.
                dupeWindow.state().hook(READY, true, () -> SwingUtilities.invokeLater(duplication::run));
                // Set media, which eventually executes the above hook or unhooks it if the window fails to load.
                dupeWindow.setMedia(new PiPMedia(get().getMedia()));
            } else {
                duplication.run();
            }
            // Sharing of general properties.
            dupeWindow.setOpacity(get().getOpacity());
            dupeWindow.state().copyFrom(get().state(), FLIP_HORIZONTAL, FLIP_VERTICAL);
            if (get().state().is(LOCALLY_MUTED)) dupeWindow.handleShortcutBind(BindDetails.createDummy(Shortcut.VOLUME_MUTE));
            break;
        // CLOSE MEDIA
        case CLOSE_MEDIA:
            setWindowMedia(null);
            break;
        // MINIMIZE WINDOW(S)
        case MINIMIZE_WINDOW:
            get().setExtendedState(JFrame.ICONIFIED);
            break;
        case MINIMIZE_WINDOWS:
            get().getManager().minimizeWindows();
            break;
        // RESTORE WINDOW(S)
        case RESTORE_WINDOW:
            get().setExtendedState(JFrame.NORMAL);
            break;
        case RESTORE_WINDOWS:
            get().getManager().restoreWindows();
            break;
        // HIDE WINDOW(S)
        case HIDE_WINDOW:
            if (get().state().not(CLOSED)) get().setVisible(false);
            break;
        case HIDE_WINDOWS:
            get().getListener().hideWindows();
            break;
        // SHOW WINDOW(S)
        case SHOW_WINDOW:
            if (get().state().not(CLOSED)) get().setVisible(true);
            break;
        case SHOW_WINDOWS:
            get().getListener().showWindows();
            break;
        // CLOSE WINDOW(S)
        case CLOSE_WINDOW:
            System.out.println("Close window request received in listeners");
            get().requestClose();
            break;
        case CLOSE_WINDOWS:
            // Immediately clear all windows if configuration option is off. Otherwise confirm with the user first.
            if (!get().propertyState(PiPProperty.CONFIRM_CLOSE_ALL, Boolean.class)
                    || TopDialog.showConfirm("Are you sure you want to close all windows?", "Clear Windows", JOptionPane.YES_NO_OPTION) == 0)
                get().getListener().clearWindows();
            break;
        default:
            // Pass off to window's implementation in case it handles this shortcut.
            get().handleShortcutBind(bind);
            break;
        }
    }
}
