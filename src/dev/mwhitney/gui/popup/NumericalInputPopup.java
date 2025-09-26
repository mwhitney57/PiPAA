package dev.mwhitney.gui.popup;

import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import dev.mwhitney.gui.components.NumberWheelButton;
import dev.mwhitney.listeners.simplified.KeyPressListener;
import dev.mwhitney.listeners.simplified.MouseClickListener;
import dev.mwhitney.listeners.simplified.WindowFocusLostListener;
import dev.mwhitney.main.PiPProperty.THEME_OPTION;
import dev.mwhitney.util.InputSequence;
import dev.mwhitney.util.PiPAAUtils;

/**
 * A pop-up for retrieving solely numerical input from the user. One or more
 * number wheels or dials are displayed and can be immediately controlled
 * through keyboard and/or mouse input.
 * <p>
 * Though other approaches, like a slider, may seem simpler at face value, this
 * approach ultimately provides further simplification with superior controls.
 * It provides granular control with a faster control scheme. It streamlines the
 * development side of things as well by ensuring the result is always an
 * {@link Integer} without the need for any additional validation.
 * 
 * @author mwhitney57
 * @since 0.9.5
 * @see {@link SelectionPopup} - superclass for selection pop-ups.
 */
public class NumericalInputPopup extends SelectionPopup {
    /** The randomly-generated serial ID. */
    private static final long serialVersionUID = 1591799421333167931L;
    
    /** An array of {@link NumberWheelButton} buttons within the pop-up, each with a selection. Hides {@link SelectionPopup#buttons}. */
    protected NumberWheelButton[] buttons;
    
    /** The maximum length of the numerical input, which also determines how many {@link NumberWheelButton} instances are needed. */
    private int length;
    /** The {@link ValueReceiver} which is called when the input is finalized and received from this pop-up. */
    private ValueReceiver<Integer> receiver;
    /**
     * The internal {@link InputSequence} which manages the flow of digit inputs.
     * This is one of the most crucial parts of this class. Not only does it allow
     * for inputs to flow in as the user enters them, and out as the length limit is
     * reached, but values can also be set and retrieved directly.
     * <p>
     * Ensure that the sequence is always in alignment with the data presented by
     * the {@link #buttons} displayed to the user.
     */
    private InputSequence<Integer> sequence; 

    /**
     * Creates a new {@link NumericalInputPopup}.
     * <p>
     * This pop-up streamlines the process for inputting numbers. It offers quick
     * and easy keyboard/mouse controls, and it conveniently ensures that only an
     * integer can be received via its {@link ValueReceiver}.
     * 
     * @param theme  - the {@link THEME_OPTION} to use for styling this pop-up.
     * @param title  - the String title to present above the options, if desired.
     * @param length - an int with the maximum digit length allowed. Determines how
     *               many {@link NumberWheelButton} instances to use and display in
     *               the pop-up. Must be at least one.
     */
    public NumericalInputPopup(THEME_OPTION theme, String title, int length) {
        super(theme, title);
        
        // Length must be at least one (number wheel/dial displayed).
        this.length = Math.max(1, length);
        this.sequence = new InputSequence<>(new Integer[this.length]).fill(0);  // Create sequence and fill with zeroes.
        
        // Setup buttons after setting length.
        setupButtons();
    }
    
    /**
     * Creates a new {@link NumericalInputPopup} without a title.
     * <p>
     * This pop-up streamlines the process for inputting numbers. It offers quick
     * and easy keyboard/mouse controls, and it conveniently ensures that only an
     * integer can be received via its {@link ValueReceiver}.
     * 
     * @param theme  - the {@link THEME_OPTION} to use for styling this pop-up.
     * @param length - an int with the maximum digit length allowed. Determines how
     *               many {@link NumberWheelButton} instances to use and display in
     *               the pop-up. Must be at least one.
     */
    public NumericalInputPopup(THEME_OPTION theme, int length) {
        this(theme, null, length);
    }
    
    /**
     * Creates a new {@link NumericalInputPopup} without a title and with a default
     * length of one, only allowing a single-digit numerical input.
     * <p>
     * This pop-up streamlines the process for inputting numbers. It offers quick
     * and easy keyboard/mouse controls, and it conveniently ensures that only an
     * integer can be received via its {@link ValueReceiver}.
     * 
     * @param theme - the {@link THEME_OPTION} to use for styling this pop-up.
     */
    public NumericalInputPopup(THEME_OPTION theme) {
        this(theme, 1);
    }
    
    @Override
    protected void setupListeners() {
        this.getContentPane().addMouseListener((MouseClickListener) (e) -> {
            // Close pop-up on RMB click.
            if (e.getButton() == MouseEvent.BUTTON3) close();
        });
        this.addWindowFocusListener((WindowFocusLostListener) (e) -> close());
        this.addKeyListener((KeyPressListener) (e) -> {
            int keyIntValue = -1;
            switch(e.getKeyCode()) {
            // Confirm with the current values.
            case KeyEvent.VK_ENTER, KeyEvent.VK_SPACE -> {
                // If receiver is set, call with current value.
                if (this.hasReceiver()) {
                    this.receiver.valueSelected(PiPAAUtils.appendInts(sequence.getSequence()));
                }
                close();  // Close window regardless.
            }
            // Intentional close.
            case KeyEvent.VK_ESCAPE -> close();
            // Decrement/Increment shown value.
            case KeyEvent.VK_DOWN, KeyEvent.VK_LEFT -> keyIntValue = -3;
            case KeyEvent.VK_UP,  KeyEvent.VK_RIGHT -> keyIntValue = -2;
            // Specific digit inputs.
            case KeyEvent.VK_0, KeyEvent.VK_NUMPAD0 -> keyIntValue = 0;
            case KeyEvent.VK_1, KeyEvent.VK_NUMPAD1 -> keyIntValue = 1;
            case KeyEvent.VK_2, KeyEvent.VK_NUMPAD2 -> keyIntValue = 2;
            case KeyEvent.VK_3, KeyEvent.VK_NUMPAD3 -> keyIntValue = 3;
            case KeyEvent.VK_4, KeyEvent.VK_NUMPAD4 -> keyIntValue = 4;
            case KeyEvent.VK_5, KeyEvent.VK_NUMPAD5 -> keyIntValue = 5;
            case KeyEvent.VK_6, KeyEvent.VK_NUMPAD6 -> keyIntValue = 6;
            case KeyEvent.VK_7, KeyEvent.VK_NUMPAD7 -> keyIntValue = 7;
            case KeyEvent.VK_8, KeyEvent.VK_NUMPAD8 -> keyIntValue = 8;
            case KeyEvent.VK_9, KeyEvent.VK_NUMPAD9 -> keyIntValue = 9;
            }
            
            // Granular arrow key adjustments to value.
            if (/* Decrement */ keyIntValue == -3 || /* Increment */ keyIntValue == -2) {
                final int current  = PiPAAUtils.appendInts(sequence.getSequence());
                // Adjust current value, restricted to bounds of possible values. 
                final int number   = keyIntValue == -3 ? current - 1 : current + 1;
                // Determine max value. (10^n)-1 where n=sequence length. One Wheel: 0 to 9, Two Wheels: 0 to 99, Three Wheels: 0 to 999, ...
                final int maxValue = (int) Math.pow(10, sequence.length()) - 1;
                // New value would be out of bounds. Cancel and return.
                if (number < 0 || number > maxValue) return;
                
                // Split adjusted number back into digits to place into sequence.
                final int[] digits = PiPAAUtils.splitInt(number);
                // Index for the digits array.
                int digiIndex = 0;
                for (int i = 0; i < sequence.length(); i++) {
                    // Leading zero. Set as zero and move to next digit in sequence.
                    if (i < sequence.length() - digits.length) {
                        sequence.set(0, i);
                        continue;
                    }
                    sequence.set(digits[digiIndex], i); // Place each digit into sequence.
                    digiIndex++;                        // Increment through digits index.
                }
            }
            else if (keyIntValue == -1) return; // No valid input.
            // Valid specific input. Input value into sequence.
            else this.sequence.in(keyIntValue);
            // Refresh buttons to ensure they have the latest sequence data and are displaying it.
            refreshButtonsDisplay();
        });
    }
    
    @Override
    protected void setupButtons() {
        // Postpone or Cancel button setup until constructor determines how many buttons should be used.
        if (this.length < 1) return;
        
        // Initialize buttons array.
        this.buttons = new NumberWheelButton[this.length];
        
        // Create and setup the buttons.
        for (int i = 0; i < this.buttons.length; i++) {
            final NumberWheelButton btn = new NumberWheelButton(FONT_BTN);
            btn.theme(theme);
            btn.setRoundedArc(20);
            btn.setBorderRoundedArc(10);
            btn.addActionListener(a -> {
                // Increment wheel by one.
                btn.next();
                // Refresh sequence data to ensure it matches the button's new value.
                refreshSequence();
            });
            btn.addMouseListener(new MouseAdapter() {
                /**
                 * A boolean for whether the RMB is primed. A button is "primed" if it is
                 * pressed and was the last button pressed. Therefore, RMB may become unprimed
                 * if another button is pressed in between pressing and releasing the RMB.
                 */
                private boolean rmbPrimed = false;
                
                @Override
                public void mousePressed(MouseEvent e) {
                    // Ensure button shows pressed visuals on RMB presses as well.
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        btn.getModel().setArmed(true);
                        btn.getModel().setPressed(true);
                        rmbPrimed = true;
                    }
                    // Not primed if last press was not RMB.
                    else rmbPrimed = false;
                }
                @Override
                public void mouseReleased(MouseEvent e) {
                    // Do nothing for non-RMB buttons or if RMB isn't primed.
                    if (e.getButton() != MouseEvent.BUTTON3 || !rmbPrimed) return;
                    
                    // Step wheel backwards.
                    btn.previous();
                    // Ensure pressed visuals are off on release. Do after value change so next paint shows new value.
                    btn.getModel().setArmed(false);
                    btn.getModel().setPressed(false);
                    // Reset primed value.
                    rmbPrimed = false;
                    
                    // Refresh sequence data to ensure it matches the button's new value.
                    refreshSequence();
                }
            });
            btn.addMouseWheelListener(e -> {
                // Adjust wheel up or down depending on scroll direction. Negated to make direction proper.
                final double scroll = -e.getPreciseWheelRotation();
                if      (scroll > 0) btn.next();
                else if (scroll < 0) btn.previous();
                else return;  // Ignore if scroll is zero somehow.
                
                // Refresh sequence data to ensure it matches the button's new value.
                refreshSequence();
                // Repaint. Scroll actions don't seem to cause repaints on their own like button presses do.
                btn.repaint();
            });
            
            // Add to buttons array and content pane.
            this.buttons[i] = btn;
            this.getContentPane().add(btn, "sg buttons");
        }
        this.pack();
    }

    /**
     * Refreshes the sequence to ensure it matches what is being displayed by the
     * buttons.
     * <p>
     * <b>Overwrites current sequence values with those present in each button.</b>
     */
    private void refreshSequence() {
        for (int i = 0; i < this.buttons.length; i++) {
            this.sequence.set(this.buttons[i].getValue(), i);
        }
    }
    
    /**
     * Refreshes the values of each button in the pop-up and repaints it to ensure
     * the display matches with the internal sequence data.
     * <p>
     * <b>Overwrites button display values with those present in the sequence.</b>
     */
    private void refreshButtonsDisplay() {
        for (int i = 0; i < this.buttons.length; i++) {
            final Integer val = this.sequence.get(i);
            this.buttons[i].setValue(val == null ? 0 : val);
            this.buttons[i].repaint();
        }
    }

    // Overridden to change return type.
    @Override
    public NumericalInputPopup display() {
        super.display();
        return this;
    }
    
    /**
     * Checks if this pop-up has a {@link ValueReceiver} set.
     * 
     * @return <code>true</code> if a receiver is set; <code>false</code> otherwise.
     * @see {@link #setValueReceiver(ValueReceiver)} to set a receiver.
     */
    @Override
    public boolean hasReceiver() {
        return (this.receiver != null);
    }
    
    /**
     * Sets the {@link ValueReceiver} used by this pop-up. The receiver will be
     * called when a value selection is made within the pop-up.
     * 
     * @param receiver - the {@link ValueReceiver} to use.
     * @return this pop-up instance.
     */
    public NumericalInputPopup setValueReceiver(ValueReceiver<Integer> receiver) {
        this.receiver = receiver;
        return this;
    }
}
