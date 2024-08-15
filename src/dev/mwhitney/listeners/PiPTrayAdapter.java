package dev.mwhitney.listeners;

import dev.mwhitney.gui.PiPWindow;

/**
 * An adapter for the PiPAA tray item/icon which utilizes a few PiPListeners.
 * 
 * @author mwhitney57
 */
public abstract class PiPTrayAdapter implements PiPTrayListener, PiPWindowManagerListener {
    // No Forced User-Implementation
    @Override
    public void applicationClosing() { if (get() != null ) get().exit(); }
    // No Forced User-Implementation -- No Default Behavior
    @Override
    public void windowCloseRequested() {}
    @Override
    public void windowClosed() {}
    @Override
    public void windowMediaCrashed() {}
    @Override
    public boolean hasDuplicates(PiPWindow win) { return false; }
}
