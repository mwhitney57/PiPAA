package dev.mwhitney.gui.binds;

import java.util.Objects;

/**
 * An input bind typically connected to a {@link Shortcut}. This class is
 * intended to be extended upon, such as with {@link KeyBind} and
 * {@link MouseBind}. These classes represent different types of inputs used to
 * trigger shortcuts.
 * 
 * @param <I> - an extension of {@link BindInput} associated with the extension
 *            upon this class.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public abstract sealed class Bind<I extends BindInput> permits KeyBind, MouseBind {
    /** The set of {@link BindDetails} pertaining to this bind instance. */
    protected volatile BindDetails<I> details;
    
    /**
     * Creates a bind with the passed {@link BindDetails}.
     * 
     * @param details - the {@link BindDetails} instance.
     */
    protected Bind(BindDetails<I> details) {
        setDetails(details);
    }
    
    /**
     * Checks if this bind is a {@link KeyBind} instance.
     * 
     * @return <code>true</code> if this is a {@link KeyBind} instance;
     *         <code>false</code> otherwise.
     */
    public boolean isKeyBind() {
        return (this instanceof KeyBind);
    }
    
    /**
     * Checks if this bind is a {@link MouseBind} instance.
     * 
     * @return <code>true</code> if this is a {@link MouseBind} instance;
     *         <code>false</code> otherwise.
     */
    public boolean isMouseBind() {
        return (this instanceof MouseBind);
    }
    
    /**
     * Gets the set of details relating to this bind.
     * 
     * @return the {@link BindDetails} instance.
     */
    public BindDetails<I> details() {
        return this.details;
    }
    
    /**
     * Sets the details of this bind.
     * 
     * @param details - the {@link BindDetails} instance to set.
     */
    public void setDetails(BindDetails<I> details) {
        this.details = Objects.requireNonNull(details, "Bind cannot have null details!");
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o != null && o instanceof Bind bind) {
            // Vital equals information is the details matching. See that class' equals implementation for more.
            return this.details.equals(bind.details());
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.details);
    }
}
