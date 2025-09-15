package dev.mwhitney.gui.binds;

import java.awt.event.KeyEvent;

/**
 * An extension of {@link BindInput} specifically for keyboard inputs.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public final class KeyInput extends BindInput {
    /**
     * Creates a new keyboard input with the passed code and modifiers. Defaults to
     * a {@link #hits()} value of <code>1</code>.
     * <p>
     * Automatically performs modifier mask cleaning via
     * {@link #cleanMouseModifiers(int)}.
     * 
     * @param code      - the input code.
     * @param modifiers - the input modifiers mask.
     */
    public KeyInput(int code, int modifiers) {
        super(code, cleanMouseModifiers(modifiers));
    }

    /**
     * Creates a new keyboard input with the passed code, modifiers, and hits
     * integers.
     * <p>
     * Automatically performs modifier mask cleaning via
     * {@link #cleanMouseModifiers(int)}.
     * 
     * @param code      - the input code.
     * @param modifiers - the input modifiers mask.
     * @param hits      - the number of consecutive hits of the input.
     */
    public KeyInput(int code, int modifiers, int hits) {
        super(code, cleanMouseModifiers(modifiers), hits);
    }
    
    @Override
    public boolean equals(Object o) {
        // If same object in memory, quick return true.
        if (this == o) return true;
        // Ensure types are consistent, then cast and compare data if clear.
        if (o != null && o instanceof KeyInput input) return matches(input);
        // Not equal. Passed object is null or type mismatch.
        return false;
    }
    
    @Override
    public String toString() {
        return String.format("KeyInput [%s] == Code:%s | Modifiers:%s | Hits:%s",
                KeyEvent.getKeyText(this.code()),
                this.code, this.modifiers, this.hits);
    }
}