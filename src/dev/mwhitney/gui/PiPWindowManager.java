package dev.mwhitney.gui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import dev.mwhitney.gui.PiPWindowState.StateProp;
import dev.mwhitney.gui.binds.BindController;
import dev.mwhitney.gui.binds.BindControllerFetcher;
import dev.mwhitney.gui.binds.BindDetails;
import dev.mwhitney.gui.binds.BindHandler;
import dev.mwhitney.gui.binds.Shortcut;
import dev.mwhitney.listeners.PiPWindowCountListener;
import dev.mwhitney.listeners.PiPWindowManagerAdapter;
import dev.mwhitney.listeners.WindowFocusGainedListener;
import dev.mwhitney.main.CFExec;
import dev.mwhitney.media.PiPMedia;
import dev.mwhitney.media.PiPMediaAttributes;
import dev.mwhitney.media.attribution.AttributionRequest;
import dev.mwhitney.media.attribution.PiPMediaAttributor;
import dev.mwhitney.media.exceptions.InvalidMediaException;
import dev.mwhitney.properties.PiPProperty;
import dev.mwhitney.properties.PropertyListener;
import dev.mwhitney.util.FileSelection;
import dev.mwhitney.util.PiPAAUtils;
import dev.mwhitney.util.monitor.ProcessMonitor;
import dev.mwhitney.util.monitor.ThreadMonitor;

/**
 * Manages any existing PiPWindows.
 * 
 * @author mwhitney57
 */
public class PiPWindowManager implements PropertyListener, BindControllerFetcher, BindHandler, ProcessMonitor {
    /** The size of the user's screen. */
    private final Rectangle userScreen = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
    /** The media attributor that assists in determining the attributes for PiPMedia. */
    private final PiPMediaAttributor attributor = new PiPMediaAttributor() {
        @Override
        public <T> T propertyState(PiPProperty prop, Class<T> rtnType) { return PiPWindowManager.this.propertyState(prop, rtnType); }
    };
    /** The List of PiPWindows managed by this manager. */
    private final List<PiPWindow> windows = new ArrayList<PiPWindow>();
    
    /** The {@link CountDownLatch} which gives the manager time to clear windows during exit, but only up to a set timeout. */
    private CountDownLatch exitLatch;
    /** The window count listener that gets called when the live window count changes. */
    private PiPWindowCountListener countListener;
    /** The count/amount of <b>live</b>, unclosed PiPWindows managed by this manager.*/
    private volatile int liveWindowCount;
    /** An int index for the last window that received user focus. */
    private volatile int lastWindowFocused;
    /** The Point (x,y) of the top-left corner of the last added window. */
    private Point lastSpawnLocation;
    /** The Point (x,y) of the bottom-right corner of the last added window. */
    private Point lastSpawnLocationMax;

    /**
     * Creates a new PiPWindowManager for managing PiPWindows. <br/>
     * This class was created with the intention of only having one window manager
     * in existence at a time.
     */
    public PiPWindowManager() {
        this.liveWindowCount = 0;
        this.lastWindowFocused = 0;
        this.lastSpawnLocation = new Point(-1, -1);
        this.lastSpawnLocationMax = new Point(-1, -1);
    }
    
    /**
     * Creates a new {@link PiPWindow} safely on the event-dispatch thread (EDT),
     * regardless of the current thread.
     * <p>
     * This method should always return a valid window, <b>unless</b> there is a
     * rare, unexpected error thrown by
     * {@link SwingUtilities#invokeAndWait(Runnable)} within
     * {@link PiPAAUtils#makeOnEDT(Supplier)}.
     * 
     * @return the new {@link PiPWindow} instance.
     * @since 0.9.5
     */
    private PiPWindow createWindow() {
        // Window's Target Index in List
        final int index = windows.size();
        
        // Ensure window is created on the EDT then return it.
        return PiPAAUtils.makeOnEDT(() -> {
            // Construct Window
            final PiPWindow window = new PiPWindow() {
                /** The randomly-generated serial UID for PiPWindows. */
                private static final long serialVersionUID = 4710798365075224246L;
                
                @Override
                public <T> T propertyState(PiPProperty prop, Class<T> rtnType) { return PiPWindowManager.this.propertyState(prop, rtnType); }
                @Override
                public PiPWindowManager getManager() { return PiPWindowManager.this; }
            };
            
            // Set Window Size to Default
            window.setSize(PiPWindow.DEFAULT_SIZE);
            
            // Set Window Location on Screen
            if (lastSpawnLocation.x == -1 || !userScreen.contains(lastSpawnLocation)
                    || !userScreen.contains(lastSpawnLocationMax)) {
                lastSpawnLocation.setLocation(0, 0);
                lastSpawnLocationMax.setLocation(PiPWindow.DEFAULT_SIZE.width + PiPWindow.DEFAULT_MEDIA_SIZE,
                        PiPWindow.DEFAULT_SIZE.height + PiPWindow.DEFAULT_MEDIA_SIZE);
            } else {
                lastSpawnLocation.translate(40, 25);
                lastSpawnLocationMax.translate(40, 25);
            }
            window.setLocation(lastSpawnLocation);
            
            // Set Window Listeners
            window.addWindowFocusListener((WindowFocusGainedListener) (e) -> lastWindowFocused = index);
            window.setListener(new PiPWindowManagerAdapter() {  // Communicates back to the manager.
                @Override
                public PiPWindowManager get() { return PiPWindowManager.this; }
                
                @Override
                public void windowCloseRequested() { PiPWindowManager.this.removeWindow(windows.indexOf(window)); }
                
                @Override
                public void windowClosed() {
                    final int index = windows.indexOf(window);
                    if (index != -1) windows.set(index, null);
                }
                
                @Override
                public void windowMediaCrashed() {
                    // Removes the window with crashed media, then adds a new one.
                    // The new window has updated display text to notify the user of the crash.
                    final boolean wasClosing = window.state().any(StateProp.CLOSING, StateProp.CLOSED);
                    PiPWindowManager.this.removeWindow(windows.indexOf(window));
                    if (!wasClosing) {
                        System.err.println("Error: Media player crashed in window. Opening replacement...");
                        PiPWindowManager.this.addWindow().statusUpdate("Media player crashed...");
                    }
                    else System.err.println("Warning: Media player crashed in window during close. Can safely ignore.");
                }
                
                @Override
                public PiPMediaAttributes requestAttributes(AttributionRequest req) {
                    try {
                        return PiPWindowManager.this.attributor.determineAttributes(req);
                    } catch (InvalidMediaException ime) {
                        System.err.println(ime.getMessage());
                    } catch (InterruptedException ie) {
                        System.err.println("Attribution process interrupted, likely due to window closing: " + ie.getMessage());
                    }
                    return null;
                }
            });
            // Window setup complete. Make window visible AFTER setting size and location to avoid Swing issues.
            window.setVisible(true);
            // Return Created Window
            return window;
        });
    }

    /**
     * Adds/generates a new window with default parameters and no media to start.
     * 
     * @return the added {@link PiPWindow}.
     */
    public PiPWindow addWindow() {
        return addWindow(null);
    }

    /**
     * Adds/generates a new window with default parameters. Immediately loads and
     * plays the passed media if it is not <code>null</code>.
     * 
     * @param media - the {@link PiPMedia} to load and play, or <code>null</code> if
     *              the window should be created and start empty.
     * @return the added {@link PiPWindow}.
     */
    public PiPWindow addWindow(PiPMedia media) {
        // Create Window Safely on EDT
        final PiPWindow window = createWindow();

        // Add Window to List
        this.windows.add(window);

        // Set Window Media (if NOT null)
        if (media != null) window.setMedia(media);
        
        // Update Live Window Count
        liveWindowCountInc();
        
        return window;
    }

    /**
     * Removes the last PiPWindow that received focus. The <i>last</i> window to
     * have user focus may not <i>currently</i> be focused. The user could be
     * focused on another application entirely and this method would still function.
     * 
     * @param index - an int for the index.
     * @return <code>true</code> if the removal succeeded; <code>false</code>
     *         otherwise.
     */
    public boolean removeFocusedWindow() {
        return removeWindow(this.lastWindowFocused);
    }

    /**
     * Removes the PiPWindow in this manager's list at the specified index.
     * 
     * @param index - an int for the index.
     * @return <code>true</code> if the removal succeeded; <code>false</code>
     *         otherwise.
     */
    public boolean removeWindow(int index) {
        if (index < 0 || index >= windows.size() || windows.get(index) == null) return false;
        windows.get(index).closeWindow();
        
        liveWindowCountDec(); // Update Live Window Count
        return true;
    }
    
    /**
     * Sets the window state for all windows managed by this window manager.
     * A window's state relates to how or if it is drawn on the screen.
     * A <code>NORMAL</code> state indicates that it is a normal window.
     * An <code>ICONIFIED</code> state indicates that it is minimized.
     * 
     * @param state - an int value with the window state to set across all windows.
     */
    private void setWindowsState(int state) {
        // Only Accept Certain States
        if (state != JFrame.ICONIFIED && state != JFrame.NORMAL && state != JFrame.MAXIMIZED_BOTH)
            return;
        
        // Set State in All Windows
        callInLiveWindows(window -> SwingUtilities.invokeLater(() -> window.setExtendedState(state)));
    }
    
    /**
     * Sets all windows to be visible or invisible depending on the passed boolean.
     * 
     * @param visible - a boolean for whether or not all windows should be visible.
     */
    private void setWindowsVisible(boolean visible) {
        // Set Visibility State for All Windows
        callInLiveWindows(window -> SwingUtilities.invokeLater(() -> window.setVisible(visible)));
    }
    
    /**
     * Executes the passed {@link Consumer} with every <b>live</b> window.
     * <p>
     * As a reminder, <b>live</b> windows are those which have not been nullified
     * and are not {@link StateProp#CLOSING} nor {@link CLOSED}.
     * 
     * @param action - the {@link Consumer} to accept with every live window.
     */
    public void callInLiveWindows(Consumer<PiPWindow> action) {
        for(final PiPWindow window : this.windows) {
            if (window == null || window.state().any(StateProp.CLOSING, StateProp.CLOSED))
                continue;
            
            action.accept(window);
        }
    }
    
    /**
     * Minimizes all windows managed by this window manager.
     */
    public void minimizeWindows() {
        setWindowsState(JFrame.ICONIFIED);
    }
    
    /**
     * Restores (un-minimizes) all windows managed by this window manager.
     */
    public void restoreWindows() {
        setWindowsState(JFrame.NORMAL);
    }
    
    /**
     * Hides all windows managed by this window manager.
     */
    public void hideWindows() {
        setWindowsVisible(false);
    }
    
    /**
     * Shows all windows managed by this window manager.
     */
    public void showWindows() {
        setWindowsVisible(true);
    }

    /**
     * <b>Deprecated</b> in favor of {@link #clearWindowsQuickly()}. This method
     * actually performs sequential clearing of windows. It runs asynchronously from
     * the thread it was called on, but the window closings do not run
     * asynchronously of each other. This can be incredibly slow, as windows can
     * hang during the closing process. It is unlikely that this method will be
     * needed over {@link #clearWindowsQuickly()}.
     * <p>
     * Clears (removes) all windows managed by this window manager. Runs
     * <b>asynchronously</b>.
     */
    @Deprecated(since = "0.9.5")
    public void clearWindows() {
        // Used CFExec approach to avoid error handling. Can safely ignore errors from pruning.
        CFExec.runSequential(() -> {
            clearWindowsInSync();
            if (propertyState(PiPProperty.DISABLE_CACHE, Boolean.class))
                PiPAAUtils.pruneCacheFolder();
        });
    }
    
    /**
     * Clears (removes) all windows managed by this window manager. Runs
     * <b>asynchronously</b>. Furthermore, each window closing operation is handled
     * asynchronously and near-simultaneously instead of happening sequentially.
     * This allows for much quicker clearing of windows, even when some of the
     * windows hang during close.
     * 
     * @since 0.9.5
     */
    public void clearWindowsQuickly() {
        // Run clearing operation asynchronously.
        CompletableFuture.runAsync(this::clearWindowsQuicklyInSync);
    }
    
    /**
     * Clears (removes) all windows managed by this window manager. Runs each window
     * close operation <b>synchronously</b> (together). However, these operations
     * are handled asynchronously, off of the calling thread, and
     * near-simultaneously instead of happening sequentially. This allows for much
     * quicker clearing of windows, even when some of the windows hang during close.
     * 
     * @since 0.9.5
     */
    public void clearWindowsQuicklyInSync() {
        // Map each window close operation to an asynchronous task.
        CompletableFuture<?>[] closeTasks = this.windows.stream().filter(window -> window != null && window.state().not(StateProp.CLOSING, StateProp.CLOSED))
            .map(window -> CompletableFuture.runAsync(window::closeWindow))
            .toArray(CompletableFuture[]::new);
        // Execute all of the asynchronous closing tasks, then set live window count which should ultimately perform cleanup.
        CompletableFuture.allOf(closeTasks).whenComplete((result, throwable) -> setLiveWindowCount(0));
    }

    /**
     * Clears (removes) all windows managed by this window manager. Runs
     * <b>synchronously</b>.
     * 
     * @see {@link #clearWindows()} to run this code asynchronously.
     * @see {@link #clearWindowsQuickly()} to run this code asynchronously and have
     *      each window close simultaneously instead of sequentially.
     */
    private void clearWindowsInSync() {
        // Iterate. Don't need Iterator -- not removing from List during loop.
        for (int i = 0; i < this.windows.size(); i++) {
            final PiPWindow window = windows.get(i);
            if (window != null && window.state().not(StateProp.CLOSED)) {
                removeWindow(i);
            }
        }
    }
    
    /**
     * Performs cleanup operations when there are zero <b>live</b> windows, per the
     * {@link #liveWindowCount()}.
     * <p>
     * This method should only be called when there are no live windows, as it
     * clears the internal {@link List} containing references to these windows.
     * 
     * @since 0.9.5
     */
    private void onZeroCleanup() {
        // Only perform cleanup if there are no live windows.
        if (this.liveWindowCount != 0) return;
        
        // Clear internal list and update window count.
        this.windows.clear();
        
        // Conditionally prune the cache folder.
        if (propertyState(PiPProperty.DISABLE_CACHE, Boolean.class)) try {
            PiPAAUtils.pruneCacheFolder();
        } catch (IOException e) {}
        
        // GC -- See Method's Doc. As To Why
        gcIfZeroCount();
        
        // Application is Exiting -- Count down latch to indicate windows are closed.
        if (this.exitLatch != null) exitLatch.countDown();
    }
    
    /**
     * Prepares for application exit/close. Clears all windows asynchronously, but
     * executes <b>synchronously</b>.
     * <p>
     * This method will attempt to wait for all of the windows to close, but has a
     * set timeout for that operation. It will ultimately proceed and return if the
     * timeout is exceeded, which would indicate that it is taking too long to close
     * the windows.
     * <p>
     * Ideally all windows are closed properly before application exit. That is best
     * practice. However, the user experience would suffer greatly if there was no
     * cap on how long such an operation could take. A reasonable timeout is crucial
     * for keeping the application quick and responsive to user input.
     */
    public void exit() {
        exitLatch = new CountDownLatch(1);
        clearWindowsQuicklyInSync();
        
        // Wait a maximum of 3 seconds for windows to close properly, then forcibly continue with exit.
        try {
            if (exitLatch.await(3, TimeUnit.SECONDS)) System.out.println("[EXIT] Window manager exited normally.");
            else System.err.println("[EXIT] Window manager exited forcibly: Took too long to clear windows.");
        } catch (InterruptedException e) {
            System.err.println("[EXIT] Window manager exited OK, but was interrupted while closing windows.");
        }
        
        // Ensure all attribution processes have been interrupted.
        interruptAll();
    }
    
    /**
     * Calls {@link System#gc()} if the {@link #windowCount()} proves there are no
     * open windows.
     * <p>
     * Though often bad practice, this call is ideal in this circumstance. After
     * clearing windows, especially ones with images, the garbage collector has
     * proven to be lazy in testing. Even after flushing the images, it waits for a
     * long time before freeing up that memory (if ever.) This could lead to memory
     * issues for the user if more and more windows with images are added and
     * removed, assuming that Java does not catch on before that happens. The
     * benefit is worth the cost.
     * <p>
     * Calling the garbage collector once the window count hits zero is ideal. This
     * is helpful when the user wants to not use the application, but leaves it
     * running. Idle background applications should take up as little memory as
     * possible.
     */
    private void gcIfZeroCount() {
        if (windowCount() <= 0) System.gc();
    }
    
    /**
     * Checks if the passed PiPWindow has any duplicates. The term "duplicate" is
     * used very loosely here. For a duplicate match to be found, both windows must
     * be non-<code>null</code>, have the same original source, and use the same
     * player.
     * 
     * @param win - the PiPWindow to check for duplicates for.
     * @return <code>true</code> if any existing PiPWindow matches the duplicate
     *         criteria; <code>false</code> otherwise.
     */
    public boolean hasDuplicates(final PiPWindow win) {
        if (win == null) return false;
        
        for (final PiPWindow window : this.windows) {
            // Skip self in list.
            if (win.equals(window)) continue;
            
            // Check for another window that meets "duplicate" criteria.
            if (window != null && window.state().not(StateProp.CLOSED) && win.hasMedia()
                    && win.getMedia().sameSrcAs(window.getMedia())
                    && win.state().get(StateProp.PLAYER_NONE).equals(window.state().get(StateProp.PLAYER_NONE))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the PiPWindow in this manager's list at the specified index.
     * 
     * @param index - an int for the index.
     * @return the PiPWindow at the specified index, or null if out of bounds.
     */
    public PiPWindow getWindow(int index) {
        return (index >= 0 && index < windowCount()) ? this.windows.get(index) : null;
    }
    
    /**
     * Gets the window's number, which is derived from, but not equal to, its index.
     * <p>
     * The window's number is a positive integer (1 or greater) which identifies the
     * window numerically. The higher the number, the newer the window is.
     * <p>
     * Any closing, crashed, or otherwise invalid windows are ignored during the
     * numbering process. Similarly, if the passed window is invalid, a value of
     * {@code -1} will be returned.
     * 
     * @param window - the {@link PiPWindow} whose number is to be retrieved.
     * @return the window's number, or {@code -1} if the passed window is invalid.
     */
    public int getWindowNumber(PiPWindow window) {
        // Return -1 if window is null, not in the list, or invalid.
        if (window == null) return -1;
        final int startIndex = this.windows.indexOf(window);
        if (startIndex == -1 || window.state().any(StateProp.CLOSING, StateProp.CLOSED, StateProp.CRASHED)) return -1;
        
        // Get and return the window's valid number.
        return (int) this.windows.stream()
                // Trims and ignores elements AFTER target window, making it the last element.
                .limit(startIndex + 1)
                // Filters out invalid windows that precede the passed window.
                .filter(w -> w != null && w.state().not(StateProp.CLOSING, StateProp.CLOSED, StateProp.CRASHED))
                // Gets the last number, which is the target window.
                .count();
    }

    /**
     * Returns the size of the PiPWindow list. The list contains each and every
     * PiPWindow this manager is responsible for.
     * 
     * @return the window count as an int.
     */
    public int windowCount() {
        return this.windows.size();
    }
    
    /**
     * Sets the window count listener for this manager.
     * The listener receives calls when the window count changes.
     * 
     * @param wcl - the PiPWindowCountListener to set.
     */
    public void setWindowCountListener(PiPWindowCountListener wcl) {
        this.countListener = wcl;
    }
    
    /**
     * Returns the number of <b>live</b> PiPWindows currently managed
     * by this manager. The live window count may differ from the <code>windowCount()</code>,
     * as it only includes windows that have not been closed.
     * 
     * @return the live window count as an int.
     */
    public int liveWindowCount() {
        return this.liveWindowCount;
    }
    
    /**
     * Sets the live window count to the passed int.
     * 
     * @param count - the int for the new live window count.
     */
    private void setLiveWindowCount(int count) {
        this.liveWindowCount = count;
        
        // Update Displayed Window Count Elsewhere (Ex: Tray Menu)
        if(this.countListener != null)
            this.countListener.windowCountChanged();
        
        // Perform Cleanup On Zero Window Count
        onZeroCleanup();
    }
    
    /**
     * Increments the live window count by 1.
     */
    private void liveWindowCountInc() {
        setLiveWindowCount(this.liveWindowCount + 1);
    }
    
    /**
     * Decrements the live window count by 1.
     */
    private void liveWindowCountDec() {
        setLiveWindowCount(Math.max(0, this.liveWindowCount - 1));
    }
    
    /**
     * <b>Deprecated</b>, as {@link #callInLiveWindows(Consumer)} simplifies this
     * kind of logic such that this function is no longer necessary.
     * <p>
     * Fires the propertyChanged method in each PiPWindow.
     * 
     * @param prop  - the PiPProperty that has changed.
     * @param value - the new property value.
     */
    @SuppressWarnings("unused")
    @Deprecated(since = "0.9.5", forRemoval = true)
    private void setPropertyInWindows(PiPProperty prop, String value) {
        for(final PiPWindow window : this.windows) {
            if(window != null && window.state().not(StateProp.CLOSED))
                window.propertyChanged(prop, value);
        }
    }

    @Override
    public ThreadMonitor getMonitor() {
        return this.attributor.getMonitor();
    }

    @Override
    public void propertyChanged(PiPProperty prop, String value) {
        // Return if property value is null. This is currently not an acceptable value.
        if(value == null) return;
        
        callInLiveWindows(window -> window.propertyChanged(prop, value));
    }

    @Override
    public void handleShortcutBind(BindDetails<?> bind) {
        // Grab Shortcut from details.
        final Shortcut shortcut = bind.shortcut();
        switch(shortcut) {
        case COPY_ALL_MEDIA:
            final List<File> files = new ArrayList<>();
            callInLiveWindows(window -> {
                final File mediaFile = window.hasMedia() ? window.getMedia().asFile() : null;
                if (mediaFile != null) files.add(mediaFile);
            });
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new FileSelection(files), null);
            break;
        case COPY_ALL_MEDIA_SRC:
            final StringBuilder sources = new StringBuilder();
            callInLiveWindows(window -> {
                if (window.hasMedia() && window.getMedia().hasSrc())
                    sources.append("\n").append(window.getMedia().getSrc());
            });
            final String str = sources.toString();
            if (!str.isBlank()) Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(str.replaceFirst("\n", "")), null);
            break;
        case FLASH_BORDERS_ALL:
            callInLiveWindows(window -> window.handleShortcutBind(BindDetails.createDummy(Shortcut.FLASH_BORDERS)));
            break;
        case PAUSE_ALL:
            callInLiveWindows(window -> window.handleShortcutBind(BindDetails.createDummy(Shortcut.PAUSE)));
            break;
        case PLAY_ALL:
            callInLiveWindows(window -> window.handleShortcutBind(BindDetails.createDummy(Shortcut.PLAY)));
            break;
        case PLAY_PAUSE_ALL:
            callInLiveWindows(window -> window.handleShortcutBind(BindDetails.createDummy(Shortcut.PLAY_PAUSE)));
            break;
        case RELOCATE_WINDOWS:
            callInLiveWindows(window -> window.handleShortcutBind(BindDetails.createDummy(Shortcut.RELOCATE_WINDOW)));
            break;
        case RESET_SIZE_ALL:
            callInLiveWindows(window -> SwingUtilities.invokeLater(window::resetSize));
            break;
        case SEEK_ALL:
            // TODO Implement after SEEK in PiPWindow.
            break;
        case SEEK_0_ALL:
            callInLiveWindows(window -> window.handleShortcutBind(BindDetails.createDummy(Shortcut.SEEK_0)));
            break;
        case SEEK_1_ALL:
            callInLiveWindows(window -> window.handleShortcutBind(BindDetails.createDummy(Shortcut.SEEK_1)));
            break;
        case SEEK_2_ALL:
            callInLiveWindows(window -> window.handleShortcutBind(BindDetails.createDummy(Shortcut.SEEK_2)));
            break;
        case SEEK_3_ALL:
            callInLiveWindows(window -> window.handleShortcutBind(BindDetails.createDummy(Shortcut.SEEK_3)));
            break;
        case SEEK_4_ALL:
            callInLiveWindows(window -> window.handleShortcutBind(BindDetails.createDummy(Shortcut.SEEK_4)));
            break;
        case SEEK_5_ALL:
            callInLiveWindows(window -> window.handleShortcutBind(BindDetails.createDummy(Shortcut.SEEK_5)));
            break;
        case SEEK_6_ALL:
            callInLiveWindows(window -> window.handleShortcutBind(BindDetails.createDummy(Shortcut.SEEK_6)));
            break;
        case SEEK_7_ALL:
            callInLiveWindows(window -> window.handleShortcutBind(BindDetails.createDummy(Shortcut.SEEK_7)));
            break;
        case SEEK_8_ALL:
            callInLiveWindows(window -> window.handleShortcutBind(BindDetails.createDummy(Shortcut.SEEK_8)));
            break;
        case SEEK_9_ALL:
            callInLiveWindows(window -> window.handleShortcutBind(BindDetails.createDummy(Shortcut.SEEK_9)));
            break;
        case WINDOWS_SIZE_DECREASE_LESS:
            callInLiveWindows(window -> window.handleShortcutBind(BindDetails.createDummy(Shortcut.WINDOW_SIZE_DECREASE_LESS)));
            break;
        case WINDOWS_SIZE_DECREASE:
            callInLiveWindows(window -> window.handleShortcutBind(BindDetails.createDummy(Shortcut.WINDOW_SIZE_DECREASE)));
            break;
        case WINDOWS_SIZE_DECREASE_MORE:
            callInLiveWindows(window -> window.handleShortcutBind(BindDetails.createDummy(Shortcut.WINDOW_SIZE_DECREASE_MORE)));
            break;
        case WINDOWS_SIZE_INCREASE_LESS:
            callInLiveWindows(window -> window.handleShortcutBind(BindDetails.createDummy(Shortcut.WINDOW_SIZE_INCREASE_LESS)));
            break;
        case WINDOWS_SIZE_INCREASE:
            callInLiveWindows(window -> window.handleShortcutBind(BindDetails.createDummy(Shortcut.WINDOW_SIZE_INCREASE)));
            break;
        case WINDOWS_SIZE_INCREASE_MORE:
            callInLiveWindows(window -> window.handleShortcutBind(BindDetails.createDummy(Shortcut.WINDOW_SIZE_INCREASE_MORE)));
            break;
        default: break; // Do nothing for the rest. Some actions handled in PiPWindow or PiPWindowListeners.
        }
    }
    
    // To Be Overridden
    @Override
    public <T> T propertyState(PiPProperty prop, Class<T> rtnType) { return null; }
    @Override
    public BindController getController() { return null; }
}
