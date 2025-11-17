package dev.mwhitney.gui.interfaces;

import dev.mwhitney.gui.PiPWindowState;

/**
 * An interface that allows for fetching of the window state via
 * {@link #getState()}.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public interface StateFetcher {
    /**
     * Checks if a non-{@code null} window state is provided via
     * {@link #getState()}.
     * 
     * @return {@code true} if a valid {@link PiPWindowState} instance is provided;
     *         {@code false} otherwise.
     */
    public default boolean hasState() {
        return this.getState() != null;
    }
    /**
     * Gets the {@link PiPWindowState} relevant to the implementing class.
     * 
     * @return the {@link PiPWindowState}, which may be {@code null}.
     * @see {@link #hasState()} to check if the state is valid beforehand.
     */
    public PiPWindowState getState();
}
