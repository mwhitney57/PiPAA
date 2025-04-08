package dev.mwhitney.gui.binds;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Objects;

/**
 * An abstract superclass for different forms of user input. Inputs could
 * ultimately match binds which trigger application shortcuts.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public abstract class BindInput {
    /** An int value for no modifiers being used. */
    public static final int NO_MODIFIERS = 0;
    
    /** A bitwise mask for all recognized, possible keyboard modifiers. */
    private static final int KEY_MODIFIERS   = InputEvent.CTRL_DOWN_MASK    | InputEvent.SHIFT_DOWN_MASK   | InputEvent.ALT_DOWN_MASK | InputEvent.META_DOWN_MASK | InputEvent.ALT_GRAPH_DOWN_MASK;
    /** A bitwise mask for all recognized, possible mouse modifiers. */
    private static final int MOUSE_MODIFIERS = InputEvent.BUTTON1_DOWN_MASK | InputEvent.BUTTON2_DOWN_MASK | InputEvent.BUTTON3_DOWN_MASK;
    
    /**
     * An int with the input code. For keyboards, the code is usually equal to
     * {@link KeyEvent#getKeyCode()}. For mice, {@link MouseEvent#getButton()}. In
     * some cases, it will be a custom code defined within {@link ShortcutCode}.
     */
    protected final int code;
    /**
     * An int with the bitwise modifiers mask. Retrieved from an event via
     * {@link InputEvent#getModifiersEx()}. Can contain multiple individual masks,
     * such as {@link InputEvent#CTRL_DOWN_MASK} or
     * {@link InputEvent#BUTTON1_DOWN_MASK}.
     */
    protected final int modifiers;
    /**
     * The number of consecutive hits (presses or clicks). For example, if this is
     * the 2nd consecutive press of the input represented by {@link #code()} and
     * {@link #modifiers()}, this value will be equal to <code>2</code>.
     * <p>
     * <b>Developer Note:</b> Used instead of AtomicInteger for being slightly
     * lighter and faster given this setup, with only one thread modifying the hits
     * at a time. If that changes, or if issues arise, consider switching for a very
     * minor performance difference.
     */
    protected volatile int hits;

    /**
     * Creates a new input with the passed code, modifiers, and hits integers.
     * <p>
     * This constructor automatically applies the logic from
     * {@link #remSelfModifiers(int, int)} to the passed modifiers mask.
     * 
     * @param code      - the input code.
     * @param modifiers - the input modifiers mask.
     * @param hits      - the number of consecutive hits of the input.
     */
    public BindInput(int code, int modifiers, int hits) {
        this.code = code;
        this.modifiers = remSelfModifiers(code, modifiers);
        this.hits = hits;
    }

    /**
     * Creates a new input with the passed code and modifiers. Defaults to a
     * {@link #hits()} value of <code>1</code>.
     * 
     * @param code      - the input code.
     * @param modifiers - the input modifiers mask.
     */
    public BindInput(int code, int modifiers) {
        this(code, modifiers, 1);
    }
    
    /**
     * Gets the code which uniquely identifies a key, button, or other input.
     * <p>
     * <b>Code Examples:</b>
     * <ul>
     * <li>{@link KeyEvent#VK_SPACE}</li>
     * <li>{@link KeyEvent#VK_A}</li>
     * <li>{@link MouseEvent#BUTTON1}</li>
     * <li>{@link MouseEvent#BUTTON3}</li>
     * <li>{@link ShortcutCode#CODE_SCROLL_DOWN}</li>
     * </ul>
     * 
     * @return an int with the code.
     */
    public int code() {
        return this.code;
    }
    
    /**
     * Gets the modifiers mask.
     * <p>
     * <b>Modifiers Mask Examples:</b>
     * <ul>
     * <li>{@link InputEvent#CTRL_DOWN_MASK}</li>
     * <li>{@link InputEvent#BUTTON1_DOWN_MASK}</li>
     * <li>{@link InputEvent#SHIFT_DOWN_MASK} | {@link InputEvent#CTRL_DOWN_MASK}</li>
     * </ul>
     * 
     * @return an int with the modifiers mask.
     */
    public int modifiers() {
        return this.modifiers;
    }
    
    /**
     * Checks if this input has any modifiers at all.
     * 
     * @return <code>true</code> if this input contains modifiers;
     *         <code>false</code> otherwise.
     */
    public boolean hasModifiers() {
        return this.modifiers > NO_MODIFIERS;
    }
    
    /**
     * Checks if the passed modifiers mask is down, meaning it is a part of this
     * input's {@link #modifiers()} mask.
     * <p>
     * For example, if this input had modifiers {@link InputEvent#SHIFT_DOWN_MASK}
     * and {@link InputEvent#CTRL_DOWN_MASK}, passing
     * {@link InputEvent#SHIFT_DOWN_MASK} to this method would return
     * <code>true</code>. Passing {@link InputEvent#ALT_DOWN_MASK} would return
     * <code>false</code>.
     * 
     * @param mask - an int with the modifiers mask to check for.
     * @return <code>true</code> if the passed mask is down; <code>false</code>
     *         otherwise.
     */
    public boolean maskDown(int mask) {
        return (this.modifiers & mask) != 0;
    }
    
    /**
     * Removes or subtracts an input's own modifiers from its mask, returning the
     * result.
     * <p>
     * Certain keys or buttons will have modifiers when pressed, if they themselves
     * are modifiers. In some cases, we may want to remove those modifiers and only
     * account for <i>additional</i> ones pressed alongside that key or button.
     * <p>
     * This method determines the modifier input based on the code and subtracts the
     * appropriate modifiers amount from the passed int. If the code did not match a
     * modifier key or button, then the passed modifiers int will be returned
     * unchanged.
     * 
     * @param code      - an int with the input code.
     * @param modifiers - an int with the input modifiers mask.
     * @return an int with the new modifiers mask, or the unchanged modifiers mask
     *         if the code did not match.
     */
    public static int remSelfModifiers(int code, int modifiers) {
        final int mask = inputCodeAsMask(code);
        return (modifiers & ~(mask == -1 ? 0 : mask));
    }

    /**
     * Converts the passed code to its equivalent modifiers mask, if it has one.
     * <p>
     * The passed code should correspond to an input which is also recognized as a
     * modifier. For example, passing {@link KeyEvent#VK_SHIFT} would return
     * {@link InputEvent#SHIFT_DOWN_MASK}.
     * 
     * @param code - the int code to convert.
     * @return the equivalent modifiers mask, or <code>-1</code> if no equivalent
     *         mask exists for the passed int.
     */
    public static int inputCodeAsMask(int code) {
        return switch(code) {
        // Key Modifiers
        case KeyEvent.VK_CONTROL   -> KeyEvent.CTRL_DOWN_MASK;
        case KeyEvent.VK_ALT       -> KeyEvent.ALT_DOWN_MASK;
        case KeyEvent.VK_SHIFT     -> KeyEvent.SHIFT_DOWN_MASK;
        case KeyEvent.VK_META      -> KeyEvent.META_DOWN_MASK;
        case KeyEvent.VK_ALT_GRAPH -> KeyEvent.ALT_GRAPH_DOWN_MASK;
        
        // Mouse Button Modifiers
        case MouseEvent.BUTTON1 -> InputEvent.BUTTON1_DOWN_MASK;
        case MouseEvent.BUTTON2 -> InputEvent.BUTTON2_DOWN_MASK;
        case MouseEvent.BUTTON3 -> InputEvent.BUTTON3_DOWN_MASK;
        default -> -1;
        };
    }
    
    /**
     * Cleans the passed modifiers mask of any key modifiers, removing them so that
     * only mouse modifiers may remain.
     * <p>
     * May be used less or not at all compared to {@link #cleanMouseModifiers(int)},
     * as keyboard modifiers may be used alongside mouse inputs.
     * 
     * @param modifiers - an int with the modifiers mask.
     * @return an int with the cleaned modifiers mask.
     */
    public static int cleanKeyModifiers(int modifiers) {
        return (modifiers & ~KEY_MODIFIERS);
    }
    
    /**
     * Cleans the passed modifiers mask of any mouse modifiers, removing them so
     * that only keyboard modifiers may remain.
     * 
     * @param modifiers - an int with the modifiers mask.
     * @return an int with the cleaned modifiers mask.
     */
    public static int cleanMouseModifiers(int modifiers) {
        return (modifiers & ~MOUSE_MODIFIERS);
    }
    
    /**
     * Gets this input's consecutive hit count.
     * 
     * @return an int with the hit count.
     */
    public int hits() {
        return this.hits;
    }
    
    /**
     * Adds a single hit to this input instance. Equivalent to calling
     * {@link #addHits(int)} and passing <code>1</code>.
     * 
     * @return an int with the new hits amount.
     */
    public int addHit() {
        return addHits(1);
    }
    
    /**
     * Adds the passed number of hits to this input instance.
     * 
     * @param hits - an int with amount of hits to add.
     * @return an int with the new hits amount.
     */
    public int addHits(int hits) {
        return setHits(this.hits + hits);
    }
    
    /**
     * Sets the number of hits to the passed int. The amount is bounded to be a
     * minimum of <code>1</code>. Only in cases where the passed int is less
     * than that bound will the returned value differ from what was passed.
     * 
     * @param hits - an int with the new hits amount.
     * @return an int with the new hits amount.
     */
    public int setHits(int hits) {
        this.hits = Math.max(1, hits);
        return this.hits;
    }
    
    /**
     * Checks if the passed input matches this instance by comparing the
     * {@link #code()} and {@link #modifiers()}. If each instance shares the same
     * values, then they are considered matching.
     * <p>
     * This method <b>does not</b> perform instance/class comparisons like
     * {@link #equals(Object)} would. Use that method if such logic is required.
     * 
     * @param input - the input to compare against.
     * @return <code>true</code> if the two inputs match; <code>false</code>
     *         otherwise.
     */
    protected boolean matches(BindInput input) {
        return (this.code == input.code() && this.modifiers == input.modifiers());
    }
    
    // Make equals abstract. Each child class implements its own.
    @Override
    public abstract boolean equals(Object o);
    
    @Override
    public int hashCode() {
        return Objects.hash(this.code, this.modifiers);
    }
    
    @Override
    public String toString() {
        return String.format("BindInput == Code:%s | Modifiers:%s | Hits:%s",
                this.code, this.modifiers, this.hits);
    }
}
