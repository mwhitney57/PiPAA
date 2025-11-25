package dev.mwhitney.gui.interfaces;

/**
 * An interface for components that adapt their painting logic depending on the
 * opacity of the window in which they are used.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public interface AdaptiveOpacity {
    /**
     * Adapts the implementing component to the window-wide opacity parameter. This
     * allows specific parts of the window to adjust how they paint under different
     * display conditions.
     * <p>
     * Since this method is intended to be called as part of the Swing process, it
     * should be called from the event-dispatch thread (EDT).
     * 
     * @param opacity - a float with the window opacity to adapt to.
     */
    public void adaptToOpacity(float opacity);
}
