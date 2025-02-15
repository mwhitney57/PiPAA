package dev.mwhitney.gui;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import dev.mwhitney.exceptions.InvalidMediaException;
import dev.mwhitney.gui.PiPWindowState.StateProp;
import dev.mwhitney.listeners.PiPWindowCountListener;
import dev.mwhitney.listeners.PiPWindowManagerAdapter;
import dev.mwhitney.listeners.PropertyListener;
import dev.mwhitney.listeners.WindowFocusGainedListener;
import dev.mwhitney.main.CFExec;
import dev.mwhitney.main.PiPProperty;
import dev.mwhitney.media.PiPMedia;
import dev.mwhitney.media.PiPMediaAttributes;
import dev.mwhitney.media.PiPMediaAttributor;
import dev.mwhitney.media.PiPMediaAttributor.Flag;
import dev.mwhitney.util.PiPAAUtils;

/**
 * Manages any existing PiPWindows.
 * 
 * @author mwhitney57
 */
public class PiPWindowManager implements PropertyListener {
    
    /** The size of the user's screen. */
    private Rectangle userScreen;
    
    /** The media attributor that assists in determining the attributes for PiPMedia. */
    private PiPMediaAttributor attributor;

    /** The List of PiPWindows managed by this manager. */
    private List<PiPWindow> windows;
    /** The window count listener that gets called when the live window count changes. */
    private PiPWindowCountListener countListener;
    /** The count/amount of <b>live</b>, unclosed PiPWindows managed by this manager.*/
    private int liveWindowCount;
    /** An int index for the last window that received user focus. */
    private int lastWindowFocused;
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
        this.windows = new ArrayList<PiPWindow>();

        this.userScreen = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        this.attributor = new PiPMediaAttributor() {
            @Override
            public <T> T propertyState(PiPProperty prop, Class<T> rtnType) { return PiPWindowManager.this.propertyState(prop, rtnType); }
        };
        this.liveWindowCount = 0;
        this.lastWindowFocused = 0;
        this.lastSpawnLocation = new Point(-1, -1);
        this.lastSpawnLocationMax = new Point(-1, -1);

        addWindow();
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
        // Window Creation
        final PiPWindow window = new PiPWindow() {
            /**
             * The randomly-generated serial UID for PiPWindows.
             */
            private static final long serialVersionUID = 4710798365075224246L;
            
            @Override
            public <T> T propertyState(PiPProperty prop, Class<T> rtnType) { return PiPWindowManager.this.propertyState(prop, rtnType); }
        };
        
        // Window Size
        window.setSize(PiPWindow.DEFAULT_SIZE);

        // Window Location on Screen
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

        // Window Index in List
        final int index = windows.size();
        window.addWindowFocusListener((WindowFocusGainedListener) (e) -> lastWindowFocused = index);
        // PiPAdapter for Communications Back to Manager
        window.setListener(new PiPWindowManagerAdapter() {
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
                PiPWindowManager.this.removeWindow(windows.indexOf(window));
                PiPWindowManager.this.addWindow().statusUpdate("Media player crashed...");
            }
            
            @Override
            public PiPMediaAttributes requestAttributes(PiPMedia media, Flag... flags) {
                try {
                    return PiPWindowManager.this.attributor.determineAttributes(media, flags);
                } catch (InvalidMediaException ime) { System.err.println(ime.getMessage()); }
                return null;
            }
        });

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
        gcIfZeroCount();      // GC If Cleared Last Window -- See Method's Doc. As To Why
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
        for(final PiPWindow window : windows) {
            if (window == null || window.state().is(StateProp.CLOSED))
                continue;
            SwingUtilities.invokeLater(() -> window.setExtendedState(state));
        }
    }
    
    /**
     * Sets all windows to be visible or invisible depending on the passed boolean.
     * 
     * @param visible - a boolean for whether or not all windows should be visible.
     */
    private void setWindowsVisible(boolean visible) {
        // Set Visibility State for All Windows
        for(final PiPWindow window : windows) {
            if (window == null || window.state().is(StateProp.CLOSED))
                continue;
            SwingUtilities.invokeLater(() -> window.setVisible(visible));
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
     * Clears (removes) all windows managed by this window manager.
     * Runs <b>asynchronously</b>.
     */
    public void clearWindows() {
        CFExec.runSequential(() -> {
            clearWindowsInSync();
            if (propertyState(PiPProperty.DISABLE_CACHE, Boolean.class))
                PiPAAUtils.pruneCacheFolder();
        });
    }
    
    /**
     * Clears (removes) all windows managed by this window manager.
     * Runs <b>synchronously</b>.
     * 
     * @see {@link #clearWindows()} to run this code asynchronously.
     */
    private void clearWindowsInSync() {
        // Iterate. Don't need Iterator -- not removing from List during loop.
        for (int i = 0; i < this.windows.size(); i++) {
            final PiPWindow window = windows.get(i);
            if (window != null && window.state().not(StateProp.CLOSED)) {
                removeWindow(i);
            }
        }
        this.windows.clear();
    }
    
    /**
     * Prepares for application exit/close.
     * Clears all windows. Executes <b>synchronously</b>.
     */
    public void exit() {
        clearWindowsInSync();
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
        if(this.countListener != null)
            this.countListener.windowCountChanged();
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
     * Fires the propertyChanged method in each PiPWindow.
     * 
     * @param prop - the PiPProperty that has changed.
     * @param value - the new property value.
     */
    private void setPropertyInWindows(PiPProperty prop, String value) {
        for(final PiPWindow window : this.windows) {
            if(window != null && window.state().not(StateProp.CLOSED))
                window.propertyChanged(prop, value);
        }
    }

    @Override
    public void propertyChanged(PiPProperty prop, String value) {
        // Return if property value is null. This is currently not an acceptable value.
        if(value == null) return;
        
        setPropertyInWindows(prop, value);
    }

    // To Be Overriden
    @Override
    public <T> T propertyState(PiPProperty prop, Class<T> rtnType) { return null; }
}
