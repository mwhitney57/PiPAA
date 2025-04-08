package dev.mwhitney.gui.binds;

/**
 * A simple interface which outlines the structure in handling a
 * {@link Shortcut} activated by a {@link Bind} which has been executed.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public interface BindHandler {
    /**
     * Handles the {@link Shortcut} associated with the passed bind described by its
     * {@link BindDetails}. This is typically where the logic is found or executed
     * from after determining that a shortcut should be activated based on user
     * input and matching of a {@link Bind}.
     * 
     * @param bind - the {@link BindDetails} which describe the shortcut and bind
     *             pairing which should be handled.
     */
    public void handleShortcutBind(BindDetails<?> bind);
}
