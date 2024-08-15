package dev.mwhitney.listeners;

import dev.mwhitney.gui.PiPWindow;
import dev.mwhitney.gui.PiPWindowManager;

/**
 * An interface for managing {@link PiPWindow} instances.
 * 
 * @author mwhitney57
 */
public interface PiPWindowManagerListener extends PiPListener {
    // Override to Force Correct Object Return Type
    @Override
    public PiPWindowManager get();
    /**
     * Adds a new window.
     */
    public default void addWindow()             { if (get() != null) get().addWindow(); };
    /**
     * Removes the last window that held user focus.
     */
    public default void removeWindow()          { if (get() != null) get().removeFocusedWindow(); };
    /**
     * Removes the window at the passed index.
     */
    public default void removeWindow(int index) { if (get() != null) get().removeWindow(index); }
    /**
     * Minimizes all windows, setting them to an ICONIFIED state.
     */
    public default void minimizeWindows()       { if (get() != null) get().minimizeWindows(); }
    /**
     * Restores all windows, setting them to a NORMAL state.
     */
    public default void restoreWindows()        { if (get() != null) get().restoreWindows(); }
    /**
     * Hides all windows, making them invisible.
     */
    public default void hideWindows()           { if (get() != null) get().hideWindows(); }
    /**
     * Shows all windows, making them visible.
     */
    public default void showWindows()           { if (get() != null) get().showWindows(); }
    /**
     * Clears all windows, closing them entirely.
     */
    public default void clearWindows()          { if (get() != null) get().clearWindows(); }
    /**
     * Called when the window is requesting for its manager to close it.
     */
    public void windowCloseRequested();
    /**
     * Called when a window has executed its closing code.
     */
    public void windowClosed();
    /**
     * Called when a window's media player(s) crashes.
     */
    public void windowMediaCrashed();
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
    public boolean hasDuplicates(final PiPWindow win);
}
