package dev.mwhitney.gui.interfaces;

import dev.mwhitney.properties.PiPProperty.THEME_OPTION;

/**
 * An interface representing objects which can be "themed." Such objects respect
 * the current color scheme of the application.
 * 
 * @author mwhitney57
 */
public interface Themed {
    /**
     * Picks the appropriate theme based on the passed {@link THEME_OPTION} option and sets the colors
     * of theme-respecting components.
     * <p>
     * This method is intended to run <b>off of the EDT</b> with most
     * implementations, but it is ultimately up to the user. It will often be better
     * by default to encapsulate <code>pickTheme(boolean)</code> method calls within
     * <code>SwingUtilities.invokeLater(Runnable)</code> if the implementation makes
     * calls to Swing components.
     * 
     * @param theme - a {@link THEME_OPTION} with the new theme.
     */
    public void pickTheme(THEME_OPTION theme);
}
