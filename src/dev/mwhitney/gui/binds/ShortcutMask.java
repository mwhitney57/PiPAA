package dev.mwhitney.gui.binds;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Custom modifier masks for shortcuts.
 * <p>
 * Java provides a number of bitwise masks for typical modifiers, such as
 * {@link InputEvent#CTRL_DOWN_MASK} or {@link InputEvent#SHIFT_DOWN_MASK}, but
 * there is a finite number of modifiers, and it becomes limiting. Certain keys,
 * especially the arrow keys, can reasonably have many functions, but modifier
 * scarcity becomes limiting.
 * <p>
 * This class provides five custom modifier slots, as well as default bindings
 * similar to how {@link Shortcut} operates. The bitwise masks range in value
 * from {@code 1 << 26} to {@code 1 << 30}, which means they should not conflict
 * with Java's built-in keyboard/mouse masks, unless the user has many mouse
 * buttons.
 * <p>
 * See {@link InputEvent#getMaskForButton(int)}. Java's input system allows for
 * buttons to fill up the rest of the usable integer space, up to
 * {@code 1 << 30}. The custom masks defined here limit this slightly, but the
 * application should still allow up to 15 mouse buttons (up to {@code 1 << 25})
 * to be used without collisions.
 * <h2>Important Developer Note</h2>
 * <p>
 * Mouse buttons can, in theory, be used with the custom shortcut slots, but
 * they <b>must not</b> be representable by Java's system and any modifier masks
 * it generates. For example, with the static reference to Mouse Button 5
 * ({@link #BUTTON5_DOWN_MASK}) uses {@link InputEvent#getMaskForButton(int)}.
 * That mask should not match the values provided here for the custom masks,
 * since there is a gap between the highest expected mouse button mask and the
 * lowest custom mask. This is intentional and good, but if
 * {@link ShortcutCode#BUTTON5} were as the code value for a custom mask,
 * problems would arise. Java's system would likely recognize Mouse Button 5,
 * and use value {@code 1 << 15} ({@link #BUTTON5_DOWN_MASK} as the modifiers
 * value.
 * <p>
 * This would cause confusion, with the binds system using the built-in
 * modifiers value while simultaneously trying to use the mask value from one of
 * the custom mask slots. Shortcuts would simply not activate, and both masks
 * would be applied despite the fact that they were intended to reference the
 * same physical button. <b>If a user is customizing the custom masks, the
 * developer must ensure that there are no duplicates of this fashion.</b>
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public enum ShortcutMask {
    /** Mask Value: 1 << 26 */
    CUSTOM_MASK_1(
        1 << 26,
        KeyEvent.VK_SLASH
    ),
    /** Mask Value: 1 << 27 */
    CUSTOM_MASK_2(
        1 << 27
        // No default input.
    ),
    /** Mask Value: 1 << 28 */
    CUSTOM_MASK_3(
        1 << 28
        // No default input.
    ),
    /** Mask Value: 1 << 29 */
    CUSTOM_MASK_4(
        1 << 29
        // No default input.
    ),
    /** Mask Value: 1 << 30 */
    CUSTOM_MASK_5(
        1 << 30
        // No default input.
    );
    
    /** The integer with the unique bitwise mask that represents the custom value. */
    private final int mask;
    /** The integer with the input code that is connected to the custom mask by default. */
    private final int code;
    
    /**
     * Creates a custom shortcut mask that has no input code bound to it by default.
     * 
     * @param mask - an int with the unique bitwise mask representing this value.
     */
    private ShortcutMask(int mask) {
        this(mask, -1);
    }
    
    /**
     * Creates a custom shortcut mask that has the passed input bound to it by
     * default.
     * 
     * @param mask - an int with the unique bitwise mask representing this value.
     * @param code - an int with the input code to be bound to the mask by default.
     */
    private ShortcutMask(int mask, int code) {
        this.mask = mask;
        this.code = code;
    }
    
    /**
     * Gets the unique bitwise mask for this custom modifier.
     * 
     * @return an int with the mask value.
     */
    public int mask() {
        return this.mask;
    }
    
    /**
     * Gets the default input code bound to this custom modifier.
     * 
     * @return an int with the input code.
     */
    public int code() {
        return this.code;
    }
    
    /**
     * Checks if this custom modifier mask has an input bound to it by default.
     * 
     * @return {@code true} if there exists a default input binding; {@code false}
     *         otherwise.
     */
    public boolean hasDefault() {
        return this.code != -1;
    }
    
    /** A constant mask for Mouse Button 4 being pressed down, defined here due to lack of presence in {@link InputEvent}. */
    public static final int BUTTON4_DOWN_MASK  = InputEvent.getMaskForButton(ShortcutCode.BUTTON4);
    /** A constant mask for Mouse Button 5 being pressed down, defined here due to lack of presence in {@link InputEvent}. */
    public static final int BUTTON5_DOWN_MASK  = InputEvent.getMaskForButton(ShortcutCode.BUTTON5);
    /** A constant mask for Mouse Button 6 being pressed down, defined here due to lack of presence in {@link InputEvent}. */
    public static final int BUTTON6_DOWN_MASK  = InputEvent.getMaskForButton(ShortcutCode.BUTTON6);
    /** A constant mask for Mouse Button 7 being pressed down, defined here due to lack of presence in {@link InputEvent}. */
    public static final int BUTTON7_DOWN_MASK  = InputEvent.getMaskForButton(ShortcutCode.BUTTON7);
    /** A constant mask for Mouse Button 8 being pressed down, defined here due to lack of presence in {@link InputEvent}. */
    public static final int BUTTON8_DOWN_MASK  = InputEvent.getMaskForButton(ShortcutCode.BUTTON8);
    /** A constant mask for Mouse Button 9 being pressed down, defined here due to lack of presence in {@link InputEvent}. */
    public static final int BUTTON9_DOWN_MASK  = InputEvent.getMaskForButton(ShortcutCode.BUTTON9);
    /** A constant mask for Mouse Button 10 being pressed down, defined here due to lack of presence in {@link InputEvent}. */
    public static final int BUTTON10_DOWN_MASK = InputEvent.getMaskForButton(ShortcutCode.BUTTON10);
    /** A constant mask for Mouse Button 11 being pressed down, defined here due to lack of presence in {@link InputEvent}. */
    public static final int BUTTON11_DOWN_MASK = InputEvent.getMaskForButton(ShortcutCode.BUTTON11);
    /** A constant mask for Mouse Button 12 being pressed down, defined here due to lack of presence in {@link InputEvent}. */
    public static final int BUTTON12_DOWN_MASK = InputEvent.getMaskForButton(ShortcutCode.BUTTON12);
    /** A constant mask for Mouse Button 13 being pressed down, defined here due to lack of presence in {@link InputEvent}. */
    public static final int BUTTON13_DOWN_MASK = InputEvent.getMaskForButton(ShortcutCode.BUTTON13);
    /** A constant mask for Mouse Button 14 being pressed down, defined here due to lack of presence in {@link InputEvent}. */
    public static final int BUTTON14_DOWN_MASK = InputEvent.getMaskForButton(ShortcutCode.BUTTON14);
    /** A constant mask for Mouse Button 15 being pressed down, defined here due to lack of presence in {@link InputEvent}. */
    public static final int BUTTON15_DOWN_MASK = InputEvent.getMaskForButton(ShortcutCode.BUTTON15);
    /** A constant combining all extra mouse button masks defined in this class. */
    public static final int EXTRA_BUTTONS_MASK = BUTTON4_DOWN_MASK | BUTTON5_DOWN_MASK | BUTTON6_DOWN_MASK
            | BUTTON7_DOWN_MASK  | BUTTON8_DOWN_MASK  | BUTTON9_DOWN_MASK  | BUTTON10_DOWN_MASK | BUTTON11_DOWN_MASK
            | BUTTON12_DOWN_MASK | BUTTON13_DOWN_MASK | BUTTON14_DOWN_MASK | BUTTON15_DOWN_MASK;
}
