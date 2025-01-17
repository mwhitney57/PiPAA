package dev.mwhitney.gui.popup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import dev.mwhitney.gui.components.BetterButton;
import dev.mwhitney.main.PiPProperty.THEME_OPTION;
import dev.mwhitney.main.PiPProperty.THEME_OPTION.COLOR;

/**
 * A simple pop-up for displaying the user with two options when <b>an image is
 * dropped onto an audio media window</b>. The user can select between replacing
 * the artwork for that audio media, or loading the image itself.
 * 
 * @author mwhitney57
 * @since 0.9.4
 * @see {@link SelectionPopup} - superclass for selection pop-ups.
 */
public class ArtSelectionPopup extends SelectionPopup {
    /** The randomly-generated serial ID for ArtSelectionPopup. */
    private static final long serialVersionUID = 2023645680306139465L;

    /**
     * Creates a new ArtSelectionPopup.
     * <p>
     * This pop-up is for either replacing the artwork of audio media in a window,
     * or loading the image in that window instead.
     * 
     * @param theme - the {@link THEME_OPTION} to use for styling this pop-up.
     */
    public ArtSelectionPopup(THEME_OPTION theme) {
        super(theme);
    }
    
    @Override
    protected void setupListeners() {
        this.getContentPane().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Close pop-up on RMB click.
                if (e.getButton() == MouseEvent.BUTTON3) close();
            }
        });
        this.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent e) { close(); }
        });
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e)         {
                // Determine which button the key press corresponds to.
                final int button = switch (e.getKeyCode()) {
                case KeyEvent.VK_1, KeyEvent.VK_R -> 0;
                case KeyEvent.VK_2, KeyEvent.VK_O -> 1;
                // Ignore other numbers -- More likely to be hit by mistake while interacting; it shouldn't close the window.
                case KeyEvent.VK_3, KeyEvent.VK_4, KeyEvent.VK_5, KeyEvent.VK_6, KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9, KeyEvent.VK_0 -> -2;
                default -> -1;
                };
                
                // Should get called from the button, unless key doesn't correspond to a button.
                if (button == -1) close();
                else if (button >= 0) {
                    // Fire action listeners. ActionEvent cannot be null here due to listener logic.
                    for (final ActionListener al : buttons[button].getActionListeners())
                        al.actionPerformed(new ActionEvent(buttons[button], ActionEvent.ACTION_PERFORMED, null));
                }
            }
        });
    }

    @Override
    protected void setupButtons() {
        this.buttons = new BetterButton[] {
            new BetterButton("Replace Artwork", FONT_BTN),
            new BetterButton("Open Image",      FONT_BTN),
        };
        
        // Add standard (press) action listeners to buttons.
        this.buttons[0].addActionListener(a -> {
            if (hasReceiver()) this.receiver.selected(0);
            close();
        });
        this.buttons[1].addActionListener(a -> {
            if (hasReceiver()) this.receiver.selected(1);
            close();
        });
        
        // Adjust each button, refreshing its color based on state, then add it.
        for (int i = 0; i < this.buttons.length; i++) {
            final BetterButton btn = this.buttons[i];
            btn.setColors(theme.color(COLOR.BTN), theme.color(COLOR.BTN_PRESSED), theme.color(COLOR.BTN_BORDER));
            btn.setForeground(theme.color(COLOR.BTN_TXT));
            btn.setPreferredSize(null);
            btn.setRoundedArc(20);
            btn.setBorderRoundedArc(10);
            
            this.getContentPane().add(btn, "sg buttons, grow, h 25:40:60");
        }
    }
    
    @Override
    public boolean block(int timeout) {
        // Override to automatically close pop-up after block has completed.
        final boolean result = super.block(timeout);
        close();
        return result;
    }
}
