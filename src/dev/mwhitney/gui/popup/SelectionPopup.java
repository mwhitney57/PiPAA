package dev.mwhitney.gui.popup;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.JDialog;
import javax.swing.JRootPane;

import dev.mwhitney.gui.PiPWindowState;
import dev.mwhitney.gui.components.BetterButton;
import dev.mwhitney.gui.components.BetterLabel;
import dev.mwhitney.gui.components.BetterPanel;
import dev.mwhitney.listeners.simplified.KeyPressListener;
import dev.mwhitney.listeners.simplified.MouseClickListener;
import dev.mwhitney.listeners.simplified.WindowFocusLostListener;
import dev.mwhitney.main.PiPProperty.THEME_OPTION;
import dev.mwhitney.main.PiPProperty.THEME_OPTION.COLOR;
import net.miginfocom.swing.MigLayout;

/**
 * A simple pop-up for displaying the user with multiple selections.
 * This class is meant to be extended with specific functionality.
 * 
 * @author mwhitney57
 * @since 0.9.4
 * @see {@link StatedSelectionPopup} for utilizing {@link PiPWindowState} in pop-ups.
 * @see {@link LockSelectionPopup} for an example of how to utilize this class and its extensions.
 */
public abstract class SelectionPopup extends JDialog {
    /** The randomly-generated serial ID for SelectionPopup. */
    private static final long serialVersionUID = 8397925600523455895L;
    
    /** The default {@link Font} for button text in pop-ups. */
    protected static final Font FONT_BTN   = new Font(Font.DIALOG, Font.BOLD, 16);
    /** The default {@link Font} for title text in pop-ups. */
    protected static final Font FONT_TITLE = new Font(Font.DIALOG, Font.BOLD, 18);

    /** The {@link CountDownLatch} used for blocking threads while waiting for a selection. */
    private final CountDownLatch latch = new CountDownLatch(1);
    
    /** A String with the layout constraints for the pop-up. */
    protected final String LAYOUT_CONSTRAINTS = "fill, insets 5 15 5 15";
    /** A boolean for whether or not the pop-up has been closed. Once <code>true</code> this should never return to <code>false</code>. */
    protected boolean hasClosed = false;
    /** The {@link THEME_OPTION} used for styling this pop-up. */
    protected THEME_OPTION theme;
    /** An array of {@link BetterButton} buttons within the pop-up, each with a selection. */
    protected BetterButton[] buttons;
    /** A String with the title shown within this pop-up, if there is to be one at all. */
    protected String title;
    
    /** A {@link SelectionReceiver} which is called when a selection is received. */
    protected SelectionReceiver receiver;
    
    /**
     * Creates a new SelectionPopup.
     * 
     * @param theme - the {@link THEME_OPTION} to use for styling this pop-up.
     * @see {@link #SelectionPopup(THEME_OPTION, String)} to use a title within the pop-up.
     */
    public SelectionPopup(THEME_OPTION theme) {
        this(theme, null);
    }
    
    /**
     * Creates a new SelectionPopup, including a title within its contents.
     * 
     * @param theme - the {@link THEME_OPTION} to use for styling this pop-up.
     * @param title - a String title, if desired. Can be <code>null</code>.
     * @see {@link #SelectionPopup(THEME_OPTION)} for a shorter constructor call if no title is to be used.
     */
    public SelectionPopup(THEME_OPTION theme, String title) {
        this.theme = theme;
        this.title = title;
        
        setup();
    }
    
    /**
     * Performs core setup for the selection pop-up.
     * Adds a title if one was specified at construction.
     */
    protected void setup() {
        // Frame Setup
        this.setAlwaysOnTop(true);
        this.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
        this.setUndecorated(true);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setResizable(false);
        this.setBackground(new Color(0, 0, 0, 0));
        
        // Panel Setup
        final BetterPanel panel = new BetterPanel(new MigLayout(getLayoutConstraints())).useDropShadow();
        panel.setBackground(theme.color(COLOR.BG));
        panel.setBorderColor(theme.color(COLOR.BG_ACCENT));
        panel.setRoundedArc(30);
        this.setContentPane(panel);
        
        // Title Setup
        if (!Objects.toString(this.title, "").isEmpty()) {
            final BetterLabel label = new BetterLabel(this.title, FONT_TITLE);
            label.setFocusable(false);
            label.setForeground(theme.color(COLOR.TXT));
            panel.add(label, "alignx center, span, wrap");
        }
        
        // Final Setup Calls
        setupListeners();
        setupButtons();
        this.pack();
    }
    
    /**
     * Sets up the listeners, including a key listener and focus listener. Called
     * during {@link #setup()}. Override this method to change its listener
     * functionality, but be sure to mimic some of its functionality where
     * necessary.
     */
    protected void setupListeners() {
        this.getContentPane().addMouseListener((MouseClickListener) (e) -> {
            // Close pop-up on RMB click.
            if (e.getButton() == MouseEvent.BUTTON3) close();
        });
        this.addWindowFocusListener((WindowFocusLostListener) (e) -> close());
        this.addKeyListener((KeyPressListener) (e) -> close());
    }
    
    /**
     * Sets up the buttons. Define in each class that extends
     * {@link SelectionPopup}. Set the buttons by accessing the {@link #buttons}
     * array. Button listeners are expected to be set up here as well.
     */
    protected abstract void setupButtons();
    
    /**
     * Gets the default layout constraints for the pop-up.
     * 
     * @return a String with the layout constraints.
     */
    protected String getLayoutConstraints() {
        return this.LAYOUT_CONSTRAINTS;
    }
    
    /**
     * Displays this pop-up.
     */
    public SelectionPopup display() {
        if (!this.hasClosed) this.setVisible(true);
        return this;
    }
    
    /**
     * Blocks the current thread with this pop-up, awaiting the firing of its
     * internal receiver.
     * <p>
     * This method differs from {@link #block(int)}, as it blocks indefinitely as
     * opposed to having a timeout.
     * 
     * @return <code>true</code> if the block was stopped from a selection being
     *         picked; <code>false</code> if the block timed out or was interrupted.
     * @see {@link #block(int)} to block with a set timeout in {@link TimeUnit#SECONDS}.
     */
    public boolean block() {
        return block(-1);
    }
    
    /**
     * Blocks the current thread with this pop-up, awaiting the firing of its
     * internal receiver or the elapsing of the passed timeout in
     * {@link TimeUnit#SECONDS}.
     * 
     * @param timeout - an int in {@link TimeUnit#SECONDS} for the maximum time
     *                before the block times out.
     * @return <code>true</code> if the block was stopped from a selection being
     *         picked; <code>false</code> if the block timed out or was interrupted.
     * @see {@link #block()} to block without a set timeout.
     */
    public boolean block(int timeout) {
        try {
            if (timeout <= 0) latch.await();
            else       return latch.await(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.err.println("Pop-up block was interrupted");
            return false;
        }
        
        return true;
    }
    
    /**
     * Closes this pop-up. The pop-up will be disposed of and become unusable after
     * this call.
     */
    protected void close() {
        if (hasClosed) return;
        
        this.hasClosed = true;
        this.latch.countDown();
        this.setVisible(false);
        this.dispose();
    }
    
    /**
     * Moves the pop-up relative to the passed component.
     * Automatically centers the pop-up based on the passed {@link Component}'s location.
     * This method is effectively shorthand for manually calling {@link #setLocationRelativeTo(Component)}.
     * 
     * @param c - the {@link Component} to move relative to.
     * @return this pop-up instance.
     */
    public SelectionPopup moveRelTo(Component c) {
        if (c != null && !this.hasClosed) this.setLocationRelativeTo(c);
        return this;
    }
    
    /**
     * Checks if this pop-up has a {@link SelectionReceiver} set.
     * 
     * @return <code>true</code> if a receiver is set; <code>false</code> otherwise.
     * @see {@link #setReceiver(SelectionReceiver)} to set a receiver.
     */
    public boolean hasReceiver() {
        return (this.receiver != null);
    }
    
    /**
     * Sets the {@link SelectionReceiver} used by this pop-up.
     * The receiver will be called when a selection is made within the pop-up.
     * 
     * @param receiver - the {@link SelectionReceiver} to use.
     * @return this pop-up instance.
     */
    public SelectionPopup setReceiver(SelectionReceiver receiver) {
        this.receiver = receiver;
        return this;
    }
}
