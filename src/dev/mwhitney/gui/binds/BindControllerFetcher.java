package dev.mwhitney.gui.binds;

/**
 * A simple interface for fetching the application's {@link BindController}
 * instance.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
@FunctionalInterface
public interface BindControllerFetcher {
    /**
     * Gets the {@link BindController} instance shared throughout the application.
     * 
     * @return the {@link BindController} instance.
     */
    public BindController getController();

    /**
     * Checks if the {@link BindController} returned from {@link #getController()}
     * is non-<code>null</code>.
     * 
     * @return <code>true</code> if the controller is present; <code>false</code>
     *         otherwise.
     */
    public default boolean hasController() {
        return (getController() != null);
    }
}
