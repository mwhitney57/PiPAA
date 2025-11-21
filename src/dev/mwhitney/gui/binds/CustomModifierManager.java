package dev.mwhitney.gui.binds;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.Objects;

/**
 * A class that manages a configuration of custom modifiers (bitwise masks) to
 * unlock even more intuitive or complex shortcut combinations. The class will
 * use the defaults within {@link ShortcutMask} to start, but these can be
 * easily overridden at any time for user configuration.
 * <p>
 * In simpler terms, this manager enables you to designate a standard input,
 * like the {@link KeyEvent#VK_M} key, as a <b>modifier</b>. It will occupy one
 * of the custom modifier slots. Each slot can be used as part of a shortcut
 * combination by using the unique mask associated with it.
 * <p>
 * Java provides a number of bitwise masks for typical modifiers, such as
 * {@link InputEvent#CTRL_DOWN_MASK} or {@link InputEvent#SHIFT_DOWN_MASK}, but
 * there is a finite number of modifiers, and it becomes limiting. Certain keys,
 * especially the arrow keys, can reasonably have many functions, but modifier
 * scarcity becomes limiting.
 * <p>
 * This manager uses the additional five custom modifier slots provided by
 * {@link ShortcutMask} and provides the ability for their default bindings to
 * be overridden, just like with user-configured keyboard and mouse binds.
 * <h1>Documentation Terms</h1>
 * <h3>Shortcut</h3> One or more inputs that trigger an action. In most cases,
 * shortcuts will be basic and be bound to a single input code, nothing more.
 * Sometimes, modifiers must be pressed and held before pressing the input to
 * trigger the shortcut.
 * <h3>Mask</h3> Also referred to as a bitmask, bitwise mask, or modifier mask,
 * this is a 32-bit integer, where each bit position represents a separate
 * boolean flag. Its value is represented in code like {@code 1 << 10} for
 * example. Java provides and automatically detects multiple of these, such as
 * {@link InputEvent#SHIFT_DOWN_MASK} or {@link InputEvent#BUTTON2_DOWN_MASK}.
 * However, this class handles custom modifiers, allowing most keys to
 * substitute their traditional role as a key that fires a shortcut, to a key
 * that modifies others. <b>There are up to five custom slots available for
 * use.</b>
 * <h3>Code</h3> Refers to the unique key or mouse code for an input, such as
 * {@link KeyEvent#VK_SPACE} or {@link MouseEvent#BUTTON2}. These codes help to
 * identify the specific key which should be used as a custom modifier.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public class CustomModifierManager {
    /**
     * A small, fixed-size array of integers whose length always matches
     * {@link ShortcutMask#values()}. The element at any index corresponds to the
     * same ordinal position in the {@link ShortcutMask} enum.
     * <p>
     * For example, index {@code 0} of the array should align with
     * {@link ShortcutMask#CUSTOM_MASK_1}, {@code 1} with
     * {@link ShortcutMask#CUSTOM_MASK_2}, and so on.
     */
    private volatile int[] customMaskCodes = new int[ShortcutMask.values().length];
    /** The combination of all custom modifier masks, allowing the removal of them specifically from any input's modifier integer. */
    private volatile int CUSTOM_MODIFIERS = 0;
    
    /**
     * Creates a modifier manager with the default bindings. The bindings can later
     * be changed using {@link #CustomModifierManager(Map)}, or {@link #load(Map)}.
     */
    public CustomModifierManager() {
        this(null);
    }
    
    /**
     * Creates a modifier manager with the specified custom modifier bindings. If
     * any bindings are missing for certain {@link ShortcutMask} values, nothing
     * will be used in that slot, even if there would've been something used by
     * default. If the passed {@link Map} is {@code null} then the defaults will be
     * used for every custom modifier slot.
     * 
     * @param map - a {@link Map} of each custom modifier slot and the input code
     *            that fills it.
     */
    public CustomModifierManager(Map<ShortcutMask, Integer> map) {
        load(map);
    }

    /**
     * Overwrites the saved input codes and their associated custom modifiers with
     * those provided in the passed {@link Map}.
     * <p>
     * If a binding is missing for a particular {@link ShortcutMask}, that slot will
     * be disabled ({@code code == -1}), even if a default binding exists.
     * <p>
     * If the passed {@link Map} is null, then the defaults will be used for every
     * custom modifier slot.
     * 
     * @param map - the {@link Map} of each custom modifier slot and the input code
     *            that fills it.
     */
    public void load(Map<ShortcutMask, Integer> map) {
        // Load the defaults if map is missing at least one non-null entry.
        if (map == null || map.isEmpty() || (map.size() == 1 && map.containsKey(null))) {
            loadDefaults();
            return;
        }
        
        // Iterate over custom modifier slots, using any valid entries in the map.
        for (int i = 0; i < customMaskCodes.length; i++) {
            final ShortcutMask mask = getEnumEquivalent(i);
            
            // Don't allow null values, using -1 as the default for unset/empty.
            customMaskCodes[i] = map.containsKey(mask) ? Objects.requireNonNullElse(map.get(mask), -1) : -1;
            CUSTOM_MODIFIERS = CUSTOM_MODIFIERS | mask.mask();
        }
    }
    
    /**
     * Loads the default input codes and their associated custom modifiers. If no
     * default input code exists for a particular modifier, the value {@code -1}
     * will be used in its place, indicating that there is no binding.
     */
    private void loadDefaults() {
        for (int i = 0; i < customMaskCodes.length; i++) {
            final ShortcutMask mask = getEnumEquivalent(i);
            customMaskCodes[i] = mask.hasDefault() ? mask.code() : -1;
            CUSTOM_MODIFIERS = CUSTOM_MODIFIERS | mask.mask();
        }
    }

    /**
     * Gets the enum equivalent to the passed integer index.
     * <p>
     * The enum value will one one within {@link ShortcutMask}. The mapping is
     * simple and as one would expect, with the lowest index of {@code 0} being
     * connected to the first custom mask {@link ShortcutMask#CUSTOM_MASK_1}. Every
     * subsequent index is connected to every subsequent mask.
     * 
     * @param index - the int index within the local array that stores input codes.
     * @return the {@link ShortcutMask} value equivalent to the passed index, or
     *         {@code null} if the passed int is out of bounds of the array.
     */
    private ShortcutMask getEnumEquivalent(int index) {
        return switch(index) {
        case 0  -> ShortcutMask.CUSTOM_MASK_1;
        case 1  -> ShortcutMask.CUSTOM_MASK_2;
        case 2  -> ShortcutMask.CUSTOM_MASK_3;
        case 3  -> ShortcutMask.CUSTOM_MASK_4;
        case 4  -> ShortcutMask.CUSTOM_MASK_5;
        default -> null;
        };
    }
    
    /**
     * Gets the index equivalent to the passed enum value.
     * <p>
     * The enum value will one one within {@link ShortcutMask}. The mapping is
     * simple and as one would expect, with the lowest index of {@code 0} being
     * connected to the first custom mask {@link ShortcutMask#CUSTOM_MASK_1}. Every
     * subsequent index is connected to every subsequent mask.
     * 
     * @param mask - the {@link ShortcutMask} value to get the index for.
     * @return the integer index value equivalent to the enum value.
     */
    @SuppressWarnings("unused")
    private int getIndexEquivalent(ShortcutMask mask) {
        return switch(mask) {
        case CUSTOM_MASK_1 -> 0;
        case CUSTOM_MASK_2 -> 1;
        case CUSTOM_MASK_3 -> 2;
        case CUSTOM_MASK_4 -> 3;
        case CUSTOM_MASK_5 -> 4;
        };
    }
    
    /**
     * Checks if the passed {@link Bind} matches with a custom modifier based on its
     * code. This method performs this check based on the values that are currently
     * loaded, not just the defaults.
     * 
     * @param bind - the {@link Bind} to check.
     * @return {@code true} if it is a custom modifier; {@code false} otherwise.
     */
    public boolean isCustomModifier(Bind<?> bind) {
        return isCustomModifier(bind.details().input().code());
    }
    
    /**
     * Checks if the passed input code matches with a custom modifier. This method
     * performs this check based on the values that are currently loaded, not just
     * the defaults.
     * 
     * @param code - the int input code to check.
     * @return {@code true} if it is a custom modifier; {@code false} otherwise.
     */
    public boolean isCustomModifier(int code) {
        for (final int maskCode : this.customMaskCodes) {
            if (maskCode == code) return true;
        }
        return false;
    }
    
    /**
     * Gets the custom {@link ShortcutMask} value associated with the passed input
     * code. If no match is found, a value of {@code null} will be returned.
     * 
     * @param code - the int input code used to get the bound custom mask.
     * @return the {@link ShortcutMask} value bound to the passed input code, or
     *         {@code null} if there is no custom mask connected to that code.
     */
    public ShortcutMask getCustomModifier(int code) {
        for (int i = 0; i < customMaskCodes.length; i++) {
            final ShortcutMask mask = getEnumEquivalent(i);
            if (customMaskCodes[i] == code) return mask;
        }
        return null;
    }
}
