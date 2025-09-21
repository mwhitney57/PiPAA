package dev.mwhitney.gui.interfaces;

import java.awt.Component;

import dev.mwhitney.main.PiPProperty.THEME_OPTION;

/**
 * An interface for components or visual elements which can be "themed." Theming
 * allows multiple aspects of design to be recolored with a single
 * {@link #theme(THEME_OPTION)} call.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public interface ThemedComponent {
    /**
     * Themes the component, changing colors to match values provided with the
     * passed theme.
     * 
     * @param theme - the {@link THEME_OPTION} with the colors.
     * @return the {@link Component}, which is now themed.
     */
    public Component theme(THEME_OPTION theme);
}
