package dev.mwhitney.gui.popup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Objects;

import dev.mwhitney.gui.components.better.BetterButton;
import dev.mwhitney.listeners.simplified.KeyPressListener;
import dev.mwhitney.listeners.simplified.WindowFocusLostListener;
import dev.mwhitney.properties.PiPProperty.THEME_OPTION;
import dev.mwhitney.properties.PiPProperty.THEME_OPTION.COLOR;

/**
 * A simple pop-up for displaying the user with multiple options. The options
 * are displayed as buttons, with the title of each being customizable.
 * 
 * @author mwhitney57
 * @since 0.9.5
 * @see {@link SelectionPopup} - superclass for selection pop-ups.
 */
public class OptionPopup extends SelectionPopup {
    /** The randomly-generated serial ID. */
    private static final long serialVersionUID = 1905280959552553619L;

    /** A String array of available options in this pop-up. Each of these Strings has a button created for them. */
    private final String[] options;
    
    /**
     * Creates a new {@link OptionPopup}.
     * <p>
     * This pop-up is good for any scenario where multiple options must be
     * presented, especially if those options are not hard-coded and may change.
     * 
     * @param theme   - the {@link THEME_OPTION} to use for styling this pop-up.
     * @param title   - the String title to present above the options, if desired.
     * @param options - the String array of options to display as buttons in the
     *                pop-up.
     * @throws NullPointerException if the passed String array is <code>null</code>.
     */
    public OptionPopup(THEME_OPTION theme, String title, String[] options) {
        super(theme, title);
        
        this.options = Objects.requireNonNull(options, "Cannot display an pop-up with null options!");
        setupButtons(); // Setup buttons AFTER options has been set.
    }
    
    /**
     * Creates a new {@link OptionPopup} without a title.
     * <p>
     * This pop-up is good for any scenario where multiple options must be
     * presented, especially if those options are not hard-coded and may change.
     * 
     * @param theme   - the {@link THEME_OPTION} to use for styling this pop-up.
     * @param options - the String array of options to display as buttons in the
     *                pop-up.
     * @throws NullPointerException if the passed String array is <code>null</code>.
     */
    public OptionPopup(THEME_OPTION theme, String[] options) {
        this(theme, null, options);
    }
    
    @Override
    protected void setupListeners() {
        this.addWindowFocusListener((WindowFocusLostListener) e -> close());
        this.addKeyListener((KeyPressListener) e -> {
            // Determine which button the key press corresponds to.
            final int optionIndex = switch (e.getKeyCode()) {
            // Equivalent to using 1. For programmers who see the first option as element zero.
            case KeyEvent.VK_0, KeyEvent.VK_NUMPAD0 -> 0;
            case KeyEvent.VK_1, KeyEvent.VK_NUMPAD1 -> 0;
            case KeyEvent.VK_2, KeyEvent.VK_NUMPAD2 -> 1;
            case KeyEvent.VK_3, KeyEvent.VK_NUMPAD3 -> 2;
            case KeyEvent.VK_4, KeyEvent.VK_NUMPAD4 -> 3;
            case KeyEvent.VK_5, KeyEvent.VK_NUMPAD5 -> 4;
            case KeyEvent.VK_6, KeyEvent.VK_NUMPAD6 -> 5;
            case KeyEvent.VK_7, KeyEvent.VK_NUMPAD7 -> 6;
            case KeyEvent.VK_8, KeyEvent.VK_NUMPAD8 -> 7;
            case KeyEvent.VK_9, KeyEvent.VK_NUMPAD9 -> 8;
            default -> -1;  // Non-numerical input.
            };
            
            // Close if input is non-numerical. User likely wants to close pop-up. Any numerical input may indicate attempt to select an option
            if (optionIndex == -1) close();
            // Check if input points to an option.
            else if (optionIndex >= 0 && optionIndex < this.options.length) {
                // Fire action listeners for that option. ActionEvent cannot be null here due to listener logic.
                for (final ActionListener al : buttons[optionIndex].getActionListeners())
                    al.actionPerformed(new ActionEvent(buttons[optionIndex], ActionEvent.ACTION_PERFORMED, null));
            }
        });
    }

    @Override
    protected void setupButtons() {
        // Don't attempt button setup if options has not been set or has no valid length.
        if (this.options == null || this.options.length < 1) return;
        
        // Prepare buttons array.
        this.buttons = new BetterButton[this.options.length];
        // Iterate over options array, creating a button for each one.
        for (int i = 0; i < this.options.length; i++) {
            final BetterButton btn = new BetterButton(Objects.toString(this.options[i], ""), FONT_BTN);
            // Add standard (press) action listeners to button.
            final int index = i;
            btn.addActionListener(a -> {
                if (hasReceiver()) this.receiver.selected(index);
                close();
            });
            // Adjust and theme each button.
            btn.setColors(theme.color(COLOR.BTN), theme.color(COLOR.BTN_PRESSED), theme.color(COLOR.BTN_BORDER));
            btn.setForeground(theme.color(COLOR.BTN_TXT));
            btn.setPreferredSize(null);
            btn.setRoundedArc(20);
            btn.setBorderRoundedArc(10);
            
            // Add to buttons array and content pane.
            this.buttons[i] = btn;
            this.getContentPane().add(btn, "sg optionBtns, grow, h 25:40:60, wrap");
        };
        this.pack();
    }
    
    @Override
    public boolean block(int timeout) {
        // Override to automatically close pop-up after block has completed, since that isn't default behavior.
        final boolean result = super.block(timeout);
        close();
        return result;
    }
}
