package dev.mwhitney.listeners;

import dev.mwhitney.gui.PiPWindow;
import dev.mwhitney.media.PiPMedia;
import dev.mwhitney.media.PiPMediaAttributes;
import dev.mwhitney.media.attribution.AttributionFlag;
import dev.mwhitney.media.attribution.AttributeRequestListener;

/**
 * An adapter for the {@link PiPWindowManagerListener} and other related listeners.
 * 
 * @author mwhitney57
 */
public abstract class PiPWindowManagerAdapter implements PiPWindowManagerListener, PiPHandoffListener, AttributeRequestListener  {
    // No Forced User-Implementation
    @Override
    public boolean hasDuplicates(final PiPWindow win) { return get().hasDuplicates(win); }
    @Override
    public PiPWindow handoff(PiPMedia media) { return get().addWindow(media); }
    // No Forced User-Implementation -- No Default Behavior
    @Override
    public void windowCloseRequested() {}
    @Override
    public void windowClosed() {}
    @Override
    public void windowMediaCrashed() {}
    @Override
    public PiPMediaAttributes requestAttributes(PiPMedia media, AttributionFlag... flags) { return null; }
}
