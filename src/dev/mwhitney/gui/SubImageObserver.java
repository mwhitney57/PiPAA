package dev.mwhitney.gui;

import java.awt.image.ImageObserver;

/**
 * An implementation of {@link ImageObserver} which allows for reference of a
 * "parent" or "super" observer instance.
 * <p>
 * While an ImageObserver may be replaced or overridden, this requires
 * replacement or re-creation of the original observer's implementation.
 * However, there may be cases where a simple add-on is desired, not a full
 * replacement. This is what inspired the creation of this class.
 * <p>
 * It allows any observer to set a "parent" with
 * {@link #setParentObserver(ImageObserver)}, then reference it by calling
 * {@link #getParentObserver()}. This enables super-like functionality – any
 * replacing observer can call back to its parent to perform its
 * {@link #imageUpdate(java.awt.Image, int, int, int, int, int)} code before or
 * after executing its own.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public abstract class SubImageObserver implements ImageObserver {
    /** The parent {@link ImageObserver} to this observer. */
    private ImageObserver parentObserver;
    
    /**
     * Gets the parent {@link ImageObserver}, or <code>null</code> if one is not
     * set.
     * 
     * @return the parent observer.
     * @see {@link #setParentObserver(ImageObserver)} to set the parent.
     */
    public ImageObserver getParentObserver() {
        return this.parentObserver;
    }
    
    /**
     * Checks if there is a parent {@link ImageObserver} set.
     * 
     * @return <code>true</code> if a parent is set; <code>false</code> otherwise.
     */
    public boolean hasParentObserver() {
        return this.parentObserver != null;
    }

    /**
     * Sets the parent {@link ImageObserver}.
     * 
     * @param observer - the {@link ImageObserver} parent.
     * @see {@link #getParentObserver(ImageObserver)} to get the parent.
     */
    public void setParentObserver(ImageObserver observer) {
        this.parentObserver = observer;
    }
}
