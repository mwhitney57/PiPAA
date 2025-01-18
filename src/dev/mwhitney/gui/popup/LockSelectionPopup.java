package dev.mwhitney.gui.popup;

import static dev.mwhitney.gui.PiPWindowState.StateProp.LOCKED_FULLSCREEN;
import static dev.mwhitney.gui.PiPWindowState.StateProp.LOCKED_MEDIA;
import static dev.mwhitney.gui.PiPWindowState.StateProp.LOCKED_POSITION;
import static dev.mwhitney.gui.PiPWindowState.StateProp.LOCKED_SIZE;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import dev.mwhitney.gui.PiPWindowState;
import dev.mwhitney.gui.components.BetterButton;
import dev.mwhitney.listeners.simplified.KeyPressListener;
import dev.mwhitney.listeners.simplified.MouseClickListener;
import dev.mwhitney.listeners.simplified.WindowFocusLostListener;
import dev.mwhitney.main.PiPProperty.THEME_OPTION;

/**
 * A simple, stated, pop-up for displaying the user with multiple <b>lock</b>
 * selections. This pop-up is for enabling/disabling "lock" options. See the
 * following properties for more information on these locks:
 * <ul>
 * <li>{@link PiPWindowState.StateProp#LOCKED_SIZE}</li>
 * <li>{@link PiPWindowState.StateProp#LOCKED_POSITION}</li>
 * <li>{@link PiPWindowState.StateProp#LOCKED_FULLSCREEN}</li>
 * <li>{@link PiPWindowState.StateProp#LOCKED_MEDIA}</li>
 * </ul>
 * 
 * @author mwhitney57
 * @since 0.9.4
 * @see {@link SelectionPopup} - superclass for selection pop-ups.
 * @see {@link StatedSelectionPopup} - superclass for stated selection pop-ups.
 */
public class LockSelectionPopup extends StatedSelectionPopup {
    /** The randomly-generated serial ID for LockSelectionPopup. */
    private static final long serialVersionUID = 1116662298994186121L;
    
    /** A String with the layout constraints for the pop-up. Hides {@link SelectionPopup#LAYOUT_CONSTRAINTS}. */
    protected final String LAYOUT_CONSTRAINTS = "fill, insets 5 15 5 15, wrap 2";

    /**
     * Creates a new LockSelectionPopup.
     * <p>
     * This pop-up is for enabling/disabling "lock" options. See the following
     * properties for more information on these locks:
     * <ul>
     * <li>{@link PiPWindowState.StateProp#LOCKED_SIZE}</li>
     * <li>{@link PiPWindowState.StateProp#LOCKED_POSITION}</li>
     * <li>{@link PiPWindowState.StateProp#LOCKED_FULLSCREEN}</li>
     * <li>{@link PiPWindowState.StateProp#LOCKED_MEDIA}</li>
     * </ul>
     * 
     * @param theme - the {@link THEME_OPTION} to use for styling this pop-up.
     * @param state - the {@link PiPWindowState} to use for checking states.
     */
    public LockSelectionPopup(THEME_OPTION theme, PiPWindowState state) {
        super(theme, "Toggle Lock on:", state);
        
        // Adjust each button, refreshing its color based on state, then add it.
        for (int i = 0; i < this.buttons.length; i++) {
            final BetterButton btn = this.buttons[i];
            btn.getActionListeners()[0].actionPerformed(null);  // ONLY refreshes colors of buttons based on state, since null.
            btn.setForeground(new Color(240, 240, 240));
            btn.setPreferredSize(null);
            btn.setRoundedArc(20);
            btn.setBorderRoundedArc(10);
            
            this.getContentPane().add(btn, "sg buttons, grow, h 25:40:60");
        }
        
        this.pack();
    }
    
    @Override
    protected void setupListeners() {
        this.getContentPane().addMouseListener((MouseClickListener) (e) -> {
            // Close pop-up on RMB click.
            if (e.getButton() == MouseEvent.BUTTON3) close();
        });
        this.addWindowFocusListener((WindowFocusLostListener) (e) -> close());
        this.addKeyListener((KeyPressListener) (e) -> {
            // Determine which button the key press corresponds to.
            final int button = switch (e.getKeyCode()) {
            case KeyEvent.VK_1, KeyEvent.VK_S -> 0;
            case KeyEvent.VK_2, KeyEvent.VK_P -> 1;
            case KeyEvent.VK_3, KeyEvent.VK_F -> 2;
            case KeyEvent.VK_4, KeyEvent.VK_M -> 3;
            // Ignore other numbers -- More likely to be hit by mistake while interacting; it shouldn't close the window.
            case KeyEvent.VK_5, KeyEvent.VK_6, KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9, KeyEvent.VK_0 -> -2;
            default -> -1;
            };
            
            // Should get called from the button, unless key doesn't correspond to a button.
            if (button == -1) close();
            else if (button >= 0) {
                // Fire action listeners. ActionEvent cannot be null here due to listener logic.
                for (final ActionListener al : buttons[button].getActionListeners())
                    al.actionPerformed(new ActionEvent(buttons[button], ActionEvent.ACTION_PERFORMED, null));
            }
        });
    }
    
    @Override
    protected void setupButtons() {
        this.buttons = new BetterButton[] {
            new BetterButton("Size",       FONT_BTN),
            new BetterButton("Position",   FONT_BTN),
            new BetterButton("Fullscreen", FONT_BTN),
            new BetterButton("Media",      FONT_BTN)
        };
        
        // Add standard (press) action listeners to buttons.
        this.buttons[0].addActionListener(a -> {
            // Passed ActionEvent should only be null when passed during construction.
            if (a != null)
                state.toggle(LOCKED_SIZE);
            
            if (state.is(LOCKED_SIZE))
                this.buttons[0].setColors(ENABLED, ENABLED_PRESSED, ENABLED_BORDER);
            else
                this.buttons[0].setColors(DISABLED, DISABLED_PRESSED, DISABLED_BORDER);
        });
        this.buttons[1].addActionListener(a -> {
            // Passed ActionEvent should only be null when passed during construction.
            if (a != null)
                state.toggle(LOCKED_POSITION);
            
            if (state.is(LOCKED_POSITION))
                this.buttons[1].setColors(ENABLED, ENABLED_PRESSED, ENABLED_BORDER);
            else
                this.buttons[1].setColors(DISABLED, DISABLED_PRESSED, DISABLED_BORDER);
        });
        this.buttons[2].addActionListener(a -> {
            // Passed ActionEvent should only be null when passed during construction.
            if (a != null)
                state.toggle(LOCKED_FULLSCREEN);
            
            if (state.is(LOCKED_FULLSCREEN))
                this.buttons[2].setColors(ENABLED, ENABLED_PRESSED, ENABLED_BORDER);
            else
                this.buttons[2].setColors(DISABLED, DISABLED_PRESSED, DISABLED_BORDER);
        });
        this.buttons[3].addActionListener(a -> {
            // Passed ActionEvent should only be null when passed during construction.
            if (a != null)
                state.toggle(LOCKED_MEDIA);
            
            if (state.is(LOCKED_MEDIA))
                this.buttons[3].setColors(ENABLED, ENABLED_PRESSED, ENABLED_BORDER);
            else
                this.buttons[3].setColors(DISABLED, DISABLED_PRESSED, DISABLED_BORDER);
        });
    }

    @Override
    protected String getLayoutConstraints() {
        return this.LAYOUT_CONSTRAINTS;
    }
}
