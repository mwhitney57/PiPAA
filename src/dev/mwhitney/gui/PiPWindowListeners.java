package dev.mwhitney.gui;

import static dev.mwhitney.gui.PiPWindowState.StateProp.PLAYER_COMBO;
import static dev.mwhitney.gui.PiPWindowState.StateProp.PLAYER_SWING;

import java.awt.Desktop;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import dev.mwhitney.exceptions.InvalidTransferMediaException;
import dev.mwhitney.gui.PiPWindowState.StateProp;
import dev.mwhitney.listeners.AttributeUpdateListener;
import dev.mwhitney.listeners.PiPAttributeRequestListener;
import dev.mwhitney.listeners.PiPCommandListener;
import dev.mwhitney.listeners.PiPHandoffListener;
import dev.mwhitney.listeners.PiPMediaTransferListener;
import dev.mwhitney.listeners.PiPWindowListener;
import dev.mwhitney.main.Initializer;
import dev.mwhitney.main.PiPProperty;
import dev.mwhitney.main.PropertiesManager;
import dev.mwhitney.media.PiPMedia;
import dev.mwhitney.media.PiPMediaAttributes;
import dev.mwhitney.media.PiPMediaCMD;

/**
 * The listeners for PiPWindows and their components, especially those relating
 * to media transfers such as drag and drop and copy/paste.
 * 
 * @author mwhitney57
 */
public abstract class PiPWindowListeners implements PiPWindowListener, PiPCommandListener, PiPMediaTransferListener, PiPHandoffListener, PiPAttributeRequestListener {
    /** A somewhat-useless DataFlavor, as it typically just contains the plain text URL provided by the WebURL flavor. */
    private DataFlavor flavorWebMedia;
    /** A DataFlavor containing a web URL. */
    private DataFlavor flavorWebURL;
    
    /** A DropTarget for drag 'n drop or clipboard media transfers. */
    private DropTarget dndTarget;
    /** A DropTarget for drag 'n drop or clipboard media transfers that should be handed off to a new window. */
    private DropTarget dndTargetSecondary;
    /** The KeyAdapter for all windows. */
    private KeyAdapter keyAdapter;
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
        try {
            flavorWebMedia = new DataFlavor("text/plain;class=java.io.Reader");
            flavorWebURL   = new DataFlavor("application/x-java-url;class=java.net.URL");
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

        keyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                final int keyCode = e.getKeyCode();
                final boolean shiftDown = (e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0;
                final boolean ctrlDown  = (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK)  != 0;
//                final boolean altDown = (e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK)   != 0;

                switch (keyCode) {
                // RELOCATE WINDOW ON SCREEN IF OFF
                case KeyEvent.VK_L:
                    get().ensureOnScreen();
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
                        PropertiesManager.MEDIATOR.propertyChanged(PiPProperty.GLOBAL_MUTED,
                                String.valueOf(!PropertiesManager.MEDIATOR.propertyState(PiPProperty.GLOBAL_MUTED, Boolean.class)));
                    break;
                // ADD NEW WINDOW
                case KeyEvent.VK_A:
                    if (shiftDown) handoff(null);
                    break;
                // RESTART/RELOAD
                case KeyEvent.VK_R:
                    if (ctrlDown) {
                        get().flashBorderEDT(PiPWindow.BORDER_OK);
                        sendMediaCMD(PiPMediaCMD.RELOAD);
                    }
                    break;
                // DELETE & CONTINUE (CLOSE MEDIA) OR DUPLICATE WINDOW
                case KeyEvent.VK_D:
                    // All actions below require shift, return if not pressed.
                    if (!shiftDown || !get().hasMedia())
                        break;
                    
                    if (ctrlDown)
                        get().getMedia().markForDeletion();
                    else {
                        // Duplicate the window.
                        final PiPWindow dupeWindow = handoff(new PiPMedia(get().getMedia()));
                        CompletableFuture.runAsync(() -> {
                            // Wait for the media to be loaded, then set the window size and location relative to this window.
                            float sleepMS = 1.00f;
                            while (dupeWindow.hasMedia() && dupeWindow.getMedia().isLoading()) {
                                try { Thread.sleep((int) sleepMS); }
                                catch (InterruptedException ie) { ie.printStackTrace(); }
                                sleepMS += Math.log10(sleepMS + 1);
                            }
                            SwingUtilities.invokeLater(() -> {
                                dupeWindow.changeSize(get().getSize(), true);
                                dupeWindow.setLocation(get().getLocation().x + 40, get().getLocation().y + 25);
                                dupeWindow.ensureOnScreen();
                            });
                        });
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
                    else if (ctrlDown && get().state().not(StateProp.CLOSED))
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
                    if (leftMouseDown)
                        sendMediaCMD(PiPMediaCMD.SPEED_ADJUST, "SET", "1.00f");
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
//                System.out.println("MOUSE WHEEL MOVED");
                // Adjust as a Multiplier of the Mouse Wheel Clicks (Negated to Give Proper, Default Scroll Direction)
                final int wheelClicks   = -(e.getWheelRotation());
                final boolean shiftDown = (e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0;
                final boolean ctrlDown  = (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK)  != 0;

                // Playback Rate/Speed Adjust Shortcut
                if (leftMouseDown)
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
                if (dragOrigin != null)
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
        
        transferFailed("Could not interpret clipboard contents as valid media.");
    }

    /**
     * Handles the transfer of media from a drag and drop event or the system clipboard to a window.
     * 
     * @param t - the Transferable potentially containing media.
     * @param clipboardSrc - a boolean for whether or not the transfer source was the clipboard.
     * @param handoff - a boolean for whether or not this media should be handed off to a new window instead of the current one.
     * @throws IOException when there are input/output errors with the potential media.
     * @throws UnsupportedFlavorException when there are transferable contents, but it is not in an acceptable format.
     * @throws InvalidTransferMediaException when the transfer media is not valid.
     */
    @SuppressWarnings("unchecked")
    private void handleMediaTransfer(Transferable t, boolean clipboardSrc) throws IOException, UnsupportedFlavorException, InvalidTransferMediaException {
        if (t == null) {
            System.err.println("Error: Nothing found in " + (clipboardSrc ? "clipboard." : "drag and drop."));
            return;
        }
        
        // Handoff the media to another window if current window already has media.
        final boolean HANDOFF = get().hasMedia();
        
        // Ensure clipboard directory exists.
        if (clipboardSrc) new File(Initializer.APP_CLIPBOARD_FOLDER).mkdirs();

        final boolean PREFER_LINK = get().propertyState(PiPProperty.DND_PREFER_LINK, Boolean.class);

        final boolean FLAV_STRING_SUPPORTED   = t.isDataFlavorSupported(DataFlavor.stringFlavor);
        final boolean FLAV_IMG_SUPPORTED      = t.isDataFlavorSupported(DataFlavor.imageFlavor);
        final boolean FLAV_WEBMEDIA_SUPPORTED = t.isDataFlavorSupported(flavorWebMedia);
        final boolean FLAV_WEBURL_SUPPORTED   = t.isDataFlavorSupported(flavorWebURL);
        final boolean FLAV_FILELIST_SUPPORTED = t.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
        if (FLAV_STRING_SUPPORTED)
            System.out.println("String flavor supported.");
        if (FLAV_IMG_SUPPORTED)
            System.out.println("Image flavor supported.");
        if (FLAV_WEBMEDIA_SUPPORTED)
            System.out.println("WebMedia flavor supported.");
        if (FLAV_WEBURL_SUPPORTED)
            System.out.println("Web URL flavor supported.");
        if (FLAV_FILELIST_SUPPORTED)
            System.out.println("File List flavor supported.");
//        return;
        // DEBUG ALL FLAVORS PRINTOUT
//        for(final DataFlavor flavor : t.getTransferDataFlavors()) {
//            System.out.println(flavor);
//        }
        
        // Try specific flavors first in order of precedence.
//        boolean flavSucceeded = false;
//        final Transferable content = t;
        // Clipboard-specific flavor handling precedence.
        if (clipboardSrc && !FLAV_IMG_SUPPORTED) {
            if(FLAV_STRING_SUPPORTED) {
                System.out.println("YEP CLIPBOARD STRING SUP");
                if (HANDOFF)
                    handoff(new PiPMedia((String) t.getTransferData(DataFlavor.stringFlavor)));
                else
                    setWindowMedia(new PiPMedia((String) t.getTransferData(DataFlavor.stringFlavor)));
                return;
            }
            else if (FLAV_FILELIST_SUPPORTED) {
                handleFileListDrop((List<File>) t.getTransferData(DataFlavor.javaFileListFlavor), HANDOFF);
                return;
            }
            else
                throw new InvalidTransferMediaException("The clipboard does not contain an image or valid media reference.");
        }
        
        /*
         * TODO Optimize this flavor picking process.
         * Consider changing Prefer Links with DnD to be a Prefer Loading (or other name) option.
         * This option would be a combo box like the theme or overwrite cache options.
         * Potential options could be:
         * - Always Prefer Links
         * - Always Prefer Raw Files & Images
         * - etc....
         * 
         * Better yet, give the usage of the word "Prefer" some more functional meaning.
         * If handling the drop fails with a certain flavor/type, then try another one.
         * Right now, this doesn't really happen. It would be most difficult/lengthy to
         * go through the URL process and try another method after that.
         * Perhaps the current config option would make sense after all if the process
         * is implemented in this described way.
         */
        // URL Flavor
        if (FLAV_WEBURL_SUPPORTED && ((!FLAV_FILELIST_SUPPORTED && !FLAV_IMG_SUPPORTED) || PREFER_LINK)) {
            final URL url = (URL) t.getTransferData(flavorWebURL);
            System.out.println(url.toString());
            
            if (HANDOFF)
                handoff(new PiPMedia(url.toString()));
            else
                setWindowMedia(new PiPMedia(url.toString()));
            return;
        }
        // File Flavor
        else if (FLAV_FILELIST_SUPPORTED) {
            handleFileListDrop((List<File>) t.getTransferData(DataFlavor.javaFileListFlavor), HANDOFF);
            return;
        }
        // Image Flavor
        else if (FLAV_IMG_SUPPORTED) {
            handleImageDrop((BufferedImage) t.getTransferData(DataFlavor.imageFlavor), HANDOFF);
            return;
        }
        // String Flavor -- Typically Text Paths or URLs
        else if (FLAV_STRING_SUPPORTED) {
            if (HANDOFF) handoff(new PiPMedia((String) t.getTransferData(DataFlavor.stringFlavor)));
            else setWindowMedia(new PiPMedia((String) t.getTransferData(DataFlavor.stringFlavor)));
            return;
        }
        
        // Check for alternative flavors.
        for(final DataFlavor flavor : t.getTransferDataFlavors()) {
            // Image Flavor
            if (flavor.getSubType().equalsIgnoreCase("x-java-file-list")) {
                System.err.println("DONT IGNORE THIS -- TAKE NOTE!! Alternate File Flavor Found?? --> " + flavor.toString());
                handleFileListDrop((List<File>) t.getTransferData(DataFlavor.javaFileListFlavor), HANDOFF);
                return;
            }
        }
    }
    
    /**
     * Handles the transfer of raw images.
     * 
     * @param img - the BufferedImage containing the raw image.
     * @param handoff - a boolean for whether or not this media should be handed off to a new window instead of the current one.
     * @throws IOException when there are input/output errors with the image.
     */
    private void handleImageDrop(BufferedImage img, boolean handoff) throws IOException {
        // Try as IMAGE FLAVOR
        String name = null;
        File outfile = null;
        while (outfile == null || outfile.exists()) {
            name = "/cachedImg" + (int) (Math.random() * 100000) + ".png";
            outfile = new File(Initializer.APP_CLIPBOARD_FOLDER + name);
        }
        outfile.mkdirs();
        ImageIO.write(img, "png", outfile);
        img.flush();
        img = null;
        if (handoff)
            handoff(possiblyMarkedMedia(outfile.getPath()));
        else
            setWindowMedia(possiblyMarkedMedia(outfile.getPath()));
        System.err.println("image copied to: " + outfile.getAbsolutePath());
    }
    
    /**
     * Handles the transfer of one or more media files.
     * 
     * @param files - a List of File objects connected to potential media sources.
     * @param handoff - a boolean for whether or not this media should be handed off to a new window instead of the current one.
     * @throws IOException when there are input/output errors with the file(s).
     * @throws InvalidTransferMediaException when one or more media file(s) is not valid.
     */
    private void handleFileListDrop(List<File> files, boolean handoff) throws IOException, InvalidTransferMediaException {
        // Loop through each file 
        for (int f = 0; f < files.size(); f++) {
            File droppedFile = files.get(f);
            
            System.out.println("NAME: " + droppedFile.getName() + " | PATH: " + droppedFile.getPath() + " | EXISTS? " + droppedFile.exists());
            System.out.println(droppedFile.getPath());
            System.out.println(System.getProperty("java.io.tmpdir"));
            boolean fileInTemp = false;
            // If file was copied from non-local source, it can be put in the TEMP directory until used. Save it by moving to cache folder.
            if (droppedFile.getPath().startsWith(System.getProperty("java.io.tmpdir"))) {
                try {
                    final File movedFile = new File(Initializer.APP_CLIPBOARD_FOLDER + "/" + droppedFile.getName());
                    Files.move(droppedFile.toPath(), movedFile.toPath());
                    droppedFile = movedFile;
                    fileInTemp = true;
                } catch (FileAlreadyExistsException faee) {
                    // Ignore. File already existing is fine, since that's the point of the cache.
                } catch (IOException ioe) {
                    if (f == 0)
                        throw new InvalidTransferMediaException("File not valid or does not exist.");
                    else
                        continue;
                }
            }
            
            // If multiple files were dropped, pass them off to open another PiPWindow for each.
            if(f > 0) {
                System.out.println("Extra File " + (f+1));
                handoff(getMediaFromFile(droppedFile, fileInTemp));
                continue;
            }
            
            // Finally, set the window media.
            if (handoff)
                handoff(getMediaFromFile(droppedFile, fileInTemp));
            else
                setWindowMedia(getMediaFromFile(droppedFile, fileInTemp));
        }
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
     * Retrieves the KeyAdapter listener used for keyboard inputs.
     * 
     * @return the KeyAdapter listener.
     */
    public KeyAdapter keyAdapter() {
        return this.keyAdapter;
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
    public PiPMediaAttributes requestAttributes(PiPMedia media, boolean raw) { return get().getListener().requestAttributes(media, raw); }
}
