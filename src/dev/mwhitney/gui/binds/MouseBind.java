package dev.mwhitney.gui.binds;

/**
 * An extension of {@link Bind} specifically for mouse binds. Represents a
 * combination of buttons and conditions which must be met before triggering a
 * connected {@link Shortcut}.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public class MouseBind extends Bind<MouseInput> {
    /**
     * Creates a new mouse bind under the passed code with no modifiers.
     * 
     * @param mouseCode - an int with the mouse code.
     */
    public MouseBind(int mouseCode) {
        this(mouseCode, BindInput.NO_MODIFIERS);
    }
    
    /**
     * Creates a new mouse bind under the passed code with the passed modifiers.
     * 
     * @param mouseCode - an int with the mouse code.
     * @param modifiers - an int with the modifiers mask.
     */
    public MouseBind(int mouseCode, int modifiers) {
        this(BindOptions.DEFAULT, mouseCode, modifiers);
    }
    
    /**
     * Creates a new mouse bind under the passed code with the specified set of
     * {@link BindOptions}.
     * 
     * @param options   - the {@link BindOptions} to go along with the mouse bind.
     * @param mouseCode - an int with the mouse code.
     */
    public MouseBind(BindOptions options, int mouseCode) {
        this(options, mouseCode, BindInput.NO_MODIFIERS);
    }
    
    /**
     * Creates a new mouse bind under the passed code with the passed modifiers and
     * the specified set of {@link BindOptions}.
     * 
     * @param options   - the {@link BindOptions} to go along with the mouse bind.
     * @param mouseCode - an int with the mouse code.
     * @param modifiers - an int with the modifiers mask.
     */
    public MouseBind(BindOptions options, int mouseCode, int modifiers) {
        super(new BindDetails<>(null, new MouseInput(mouseCode, modifiers, options.hits()), options));
    }
}
