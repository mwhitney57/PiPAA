package dev.mwhitney.gui.binds;

/**
 * An extension of {@link Bind} specifically for keyboard binds. Represents a
 * combination of keys and conditions which must be met before triggering a
 * connected {@link Shortcut}.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public final class KeyBind extends Bind<KeyInput> {
    /**
     * Creates a new keyboard bind under the passed code with no modifiers.
     * 
     * @param keyCode - an int with the key code.
     */
    public KeyBind(int keyCode) {
        this(keyCode, BindInput.NO_MODIFIERS);
    }
    
    /**
     * Creates a new keyboard bind under the passed code with the passed modifiers.
     * 
     * @param keyCode   - an int with the key code.
     * @param modifiers - an int with the modifiers mask.
     */
    public KeyBind(int keyCode, int modifiers) {
        this(BindOptions.DEFAULT, keyCode, modifiers);
    }
    
    /**
     * Creates a new keyboard bind under the passed code with the specified set of
     * {@link BindOptions}.
     * 
     * @param options - the {@link BindOptions} to go along with the key bind.
     * @param keyCode - an int with the key code.
     */
    public KeyBind(BindOptions options, int keyCode) {
        this(options, keyCode, BindInput.NO_MODIFIERS);
    }

    /**
     * Creates a new keyboard bind under the passed code with the passed modifiers
     * and the specified set of {@link BindOptions}.
     * 
     * @param options   - the {@link BindOptions} to go along with the key bind.
     * @param keyCode   - an int with the key code.
     * @param modifiers - an int with the modifiers mask.
     */
    public KeyBind(BindOptions options, int keyCode, int modifiers) {
        super(new BindDetails<>(null, new KeyInput(keyCode, modifiers, options.hits()), options));
    }
}
