package dev.mwhitney.gui.binds;

import java.util.Objects;

/**
 * The details about any particular {@link KeyBind} or {@link MouseBind}. Each
 * instance contains the following fields:
 * <ul>
 * <li>{@link Shortcut}</il>
 * <li>An extension of {@link BindInput}: {@link KeyInput} or {@link MouseInput}
 * </il>
 * <li>{@link BindOptions}</il>
 * </ul>
 * 
 * The class operates similarly to a Java <code>record</code>, but the
 * {@link Shortcut} field can be adjusted, so a regular <code>class</code> type
 * was used.
 * 
 * @param <I> - an extension of {@link BindInput}, such as {@link KeyInput} or
 *            {@link MouseInput}.
 * @author mwhitney57
 * @since 0.9.5
 */
public class BindDetails <I extends BindInput> {
    /** The connected {@link Shortcut} which this instance contains details for. */
    private volatile Shortcut shortcut;
    /** The input required to activate the bind, which includes important information such as the code and modifiers. */
    private final I input;
    /** The {@link BindOptions} which define custom criteria or behavior for a particular bind. */
    private final BindOptions options;
    
    /**
     * Creates a new BindDetails instance.
     * 
     * @param action  - the {@link Shortcut} connected to the bind.
     * @param input   - the input which activates the bind.
     * @param options - the options which help differentiate each bind and alter
     *                when and how it should activate.
     * @throws NullPointerException if the passed {@link BindOptions} instance is
     *                              <code>null</code>.
     * @see {@link #BindDetails(Shortcut, BindInput)} to automatically use the
     *      default set of {@link BindOptions}.
     */
    public BindDetails(Shortcut action, I input, BindOptions options) {
        this.shortcut = action;
        this.input = input;
        this.options = Objects.requireNonNull(options, "BindOptions must not be null when constructing BindDetails.");
    }

    /**
     * Creates a new BindDetails instance with the default set of
     * {@link BindOptions}.
     * 
     * @param action - the {@link Shortcut} connected to the bind.
     * @param input  - the input which activates the bind.
     * @see {@link #BindDetails(Shortcut, BindInput, BindOptions)} to specify a
     *      custom set of {@link BindOptions}.
     */
    public BindDetails(Shortcut action, I input) {
        this(action, input, BindOptions.DEFAULT);
    }
    
    /**
     * Gets the {@link Shortcut} connected to the bind.
     * 
     * @return the connected {@link Shortcut}.
     */
    public Shortcut shortcut() {
        return this.shortcut;
    }
    
    /**
     * Sets the {@link Shortcut} connected to the bind.
     * 
     * @param shortcut - the connected {@link Shortcut}.
     * @return this BindDetails instance.
     */
    public BindDetails<I> setShortcut(Shortcut shortcut) {
        this.shortcut = shortcut;
        return this;
    }

    /**
     * Gets the input which activates the bind.
     * 
     * @return the input instance.
     */
    public I input() {
        return this.input;
    }
    
    /**
     * Checks if an input instance is included within this set of details.
     * 
     * @return <code>true</code> if an input is present; <code>false</code>
     *         otherwise.
     */
    public boolean hasInput() {
        return this.input != null;
    }
    
    /**
     * Gets the set of bind options.
     * 
     * @return the {@link BindOptions}.
     */
    public BindOptions options() {
        return this.options;
    }

    /**
     * Creates a "dummy" set of bind details, which pertains to the passed
     * {@link Shortcut}. The purpose of this method is quickly generate an instance
     * of {@link BindDetails} that reference a shortcut which does not utilize any
     * of the other information in its handling. In other words, dummy instances can
     * tell part of the code to handle a particular {@link Shortcut} without needing
     * to specify additional information such as the {@link BindInput} or
     * {@link BindOptions}, which may be unnecessary for the shortcut to execute.
     * <p>
     * For example, think about how {@link Shortcut#FULLSCREEN} works. None of the
     * input data is necessary for a window to take that shortcut call and execute
     * the fullscreen change. If the situation requires using a {@link BindDetails}
     * instance, it may be most convenient to just create a dummy.
     * 
     * 
     * @param s - the {@link Shortcut} which the dummy refers to.
     * @return the new dummy BindDetails instance.
     */
    public static BindDetails<?> createDummy(Shortcut s) {
        return new BindDetails<>(s, null);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o != null && o instanceof BindDetails<?> details) {
            // Vital equals information is the input matching and if some options data matches.
            return this.input.equals(details.input()) && this.options.equals(details.options());
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.input, this.options);
    }
    
    @Override
    public String toString() {
        return String.format("BindDetails == Shortcut:%s | Input:%s | Options:%s",
                this.shortcut.toString(), this.input.toString(), this.options.toString());
    }
}
