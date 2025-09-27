package dev.mwhitney.gui.popup;

import java.awt.Color;

import dev.mwhitney.gui.PiPWindowState;
import dev.mwhitney.properties.PiPProperty.THEME_OPTION;

/**
 * A simple, stated, pop-up for displaying the user with multiple selections.
 * This class is meant to be extended with specific functionality.
 * 
 * @author mwhitney57
 * @since 0.9.4
 * @see {@link SelectionPopup} - superclass for selection pop-ups.
 */
public abstract class StatedSelectionPopup extends SelectionPopup {
    /** The randomly-generated serial ID for StatedSelectionPopup. */
    private static final long serialVersionUID = -3126983453116292690L;
    
    /** A default {@link Color} for <b>enabled</b> states. */
    protected static final Color ENABLED          = new Color(0, 124, 59);
    /** A default pressed {@link Color} for <b>enabled</b> states. */
    protected static final Color ENABLED_PRESSED  = ENABLED.darker().darker();
    /** A default border {@link Color} for <b>enabled</b> states. */
    protected static final Color ENABLED_BORDER   = ENABLED.darker();
    /** A default {@link Color} for <b>disabled</b> states. */
    protected static final Color DISABLED         = new Color(237, 5, 8);
    /** A default pressed {@link Color} for <b>disabled</b> states. */
    protected static final Color DISABLED_PRESSED = DISABLED.darker().darker();
    /** A default border {@link Color} for <b>disabled</b> states. */
    protected static final Color DISABLED_BORDER  = DISABLED.darker();

    /** The {@link PiPWindowState} utilized by this pop-up for checking states. */
    protected PiPWindowState state;
    
    /**
     * Creates a new StatedSelectionPopup.
     * 
     * @param theme - the {@link THEME_OPTION} to use for styling this pop-up.
     * @param state - the {@link PiPWindowState} to use for checking states.
     * @see {@link #StatedSelectionPopup(THEME_OPTION, String, PiPWindowState)} to use a title within the pop-up.
     */
    public StatedSelectionPopup(THEME_OPTION theme, PiPWindowState state) {
        this(theme, null, state);
    }
    
    /**
     * Creates a new StatedSelectionPopup.
     * 
     * @param theme - the {@link THEME_OPTION} to use for styling this pop-up.
     * @param title - a String title, if desired. Can be <code>null</code>.
     * @param state - the {@link PiPWindowState} to use for checking states.
     * @see {@link #StatedSelectionPopup(THEME_OPTION, PiPWindowState)} for a
     *      shorter constructor call if no title is to be used.
     */
    public StatedSelectionPopup(THEME_OPTION theme, String title, PiPWindowState state) {
        super(theme, title);
        
        this.state = state;
    }
}
