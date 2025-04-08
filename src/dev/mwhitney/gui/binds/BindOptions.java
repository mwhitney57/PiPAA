package dev.mwhitney.gui.binds;

import java.util.Objects;

/**
 * A set of options for a {@link Bind}. Allows for more conditions surrounding
 * when a bind should activate.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public class BindOptions {
    /** The default set of {@link BindOptions}, which includes one hit, the {@link #DEFAULT_CONSECUTIVE_HIT_DELAY}, and {@link #ON_PRESS} activation. */
    public static final BindOptions DEFAULT = new BindOptions();
    
    /** An int with the default delay for hits to be considered consecutive. */
    public static final int DEFAULT_CONSECUTIVE_HIT_DELAY = 600;
    /** Indicates that the shortcut should activate on <b>press</b> of the bind.*/
    public static final boolean ON_PRESS = true;
    /** Indicates that the shortcut should activate on <b>release</b> of the bind. */
    public static final boolean ON_RELEASE = false;
    
    /** The number of times a bind's codes must be fired before the bind matches. Ex: 2 = Double Press */
    private int hits;
    /** The maximum amount of milliseconds that can pass between hits for them to be considered "consecutive." */
    private int delay;
    /** If the bind activates {@link #ON_PRESS} or {@link #ON_RELEASE}. Default is {@link #ON_PRESS} (true). */
    private boolean onPress;
    
    /**
     * Creates a set of bind options with all of the defaults.
     */
    public BindOptions() {
        this(1, -1);
    }
    
    /**
     * Creates a set of bind options with a custom number of hits. This constructor uses
     * the {@link #DEFAULT_CONSECUTIVE_HIT_DELAY} and {@link #ON_PRESS} activation.
     * 
     * @param hits - an int with the number of hits.
     */
    public BindOptions(int hits) {
        this(hits, -1);
    }
    
    /**
     * Creates a set of bind options with a custom number of consecutive hits and a
     * custom delay between those hits. This constructor uses {@link #ON_PRESS}
     * activation.
     * 
     * @param hits  - an int with the number of hits.
     * @param delay - an int with the hit delay in milliseconds.
     */
    public BindOptions(int hits, int delay) {
        this(hits, delay, ON_PRESS);
    }
    
    /**
     * Creates a set of bind options with a custom number of consecutive hits, delay
     * between those hits, and whether the bind should activate {@link #ON_PRESS} or
     * {@link #ON_RELEASE}.
     * 
     * @param hits    - an int with the number of hits.
     * @param delay   - an int with the hit delay in milliseconds.
     * @param onPress - a boolean for whether the bind should activate
     *                {@link #ON_PRESS} or {@link #ON_RELEASE}.
     */
    public BindOptions(int hits, int delay, boolean onPress) {
        this.hits = Math.max(1, hits);      // Minimum of 1 Hit
        this.delay = (delay <= 0 ? DEFAULT_CONSECUTIVE_HIT_DELAY : delay);
        this.onPress = onPress;
    }
    
    // Builder Methods
    /**
     * Builder initiation method. Used to construct a set of bind options in a more
     * verbose way.
     * <p>
     * Builder instance starts with as the {@link #DEFAULT}, changing with each
     * sequential build method call. Typical builder methods include:
     * <ul>
     * <li>{@link #onHit(int)}</li>
     * <li>{@link #useDelay(int)}</li>
     * <li>{@link #onPress()}</li>
     * <li>{@link #onRelease()}</li>
     * </ul>
     * 
     * @return the new {@link BindOptions} instance to build with.
     */
    public static BindOptions build() {
        return new BindOptions();
    }

    /**
     * Builder method: Sets the hits amount required for the bind to trigger.
     * 
     * @param hits - an int with the number of hits.
     * @return this {@link BindOptions} instance.
     */
    public BindOptions onHit(int hits) {
        this.hits = hits;
        return this;
    }

    /**
     * Builder method: Sets the delay in milliseconds between hits for them to be
     * considered "consecutive."
     * 
     * @param delay - an int with the hit delay in milliseconds.
     * @return this {@link BindOptions} instance.
     */
    public BindOptions useDelay(int delay) {
        this.delay = delay;
        return this;
    }

    /**
     * Builder method: Sets that the bind should trigger on a <b>press</b> action.
     * 
     * @return this {@link BindOptions} instance.
     */
    public BindOptions onPress() {
        this.onPress = ON_PRESS;
        return this;
    }

    /**
     * Builder method: Sets that the bind should trigger on a <b>release</b> action.
     * 
     * @return this {@link BindOptions} instance.
     */
    public BindOptions onRelease() {
        this.onPress = ON_RELEASE;
        return this;
    }
    
    // Standard Methods
    /**
     * Gets the number of consecutive hits required for the bind to trigger.
     * 
     * @return an int with the hits amount.
     */
    public int hits() {
        return this.hits;
    }
    
    /**
     * Adds a single hit to hits amount. Equivalent to calling {@link #addHits(int)}
     * and passing <code>1</code>.
     * 
     * @return an int with the new hits amount.
     */
    public int addHit() {
        return addHits(1);
    }
    
    /**
     * Adds the passed number of hits to the hits amount.
     * 
     * @param hits - an int with amount of hits to add.
     * @return an int with the new hits amount.
     */
    public int addHits(int hits) {
        return setHits(this.hits + hits);
    }
    
    /**
     * Sets the number of hits to the passed int. The amount is bounded to be a
     * minimum of <code>1</code>. Only in cases where the passed int is less than
     * that bound will the returned value differ from what was passed.
     * 
     * @param hits - an int with the new hits amount.
     * @return an int with the new hits amount.
     */
    public int setHits(int hits) {
        this.hits = Math.max(1, hits);
        return this.hits;
    }
    
    /**
     * Checks if the bind should activate on a <b>single</b> hit.
     * 
     * @return <code>true</code> if the hits amount is 1; <code>false</code>
     *         otherwise.
     */
    public boolean onSingleHit() {
        return (this.hits == 1);
    }

    /**
     * Checks if the bind should activate on a <b>double</b> hit.
     * 
     * @return <code>true</code> if the hits amount is 2; <code>false</code>
     *         otherwise.
     */
    public boolean onDoubleHit() {
        return (this.hits == 2);
    }

    /**
     * Checks if the bind should activate on a <b>triple</b> hit.
     * 
     * @return <code>true</code> if the hits amount is 3; <code>false</code>
     *         otherwise.
     */
    public boolean onTripleHit() {
        return (this.hits == 3);
    }

    /**
     * Gets the consecutive hit delay for the bind, in milliseconds.
     * 
     * @return an int with the hit delay in milliseconds.
     */
    public int delay() {
        return this.delay;
    }
    
    /**
     * Checks if the consecutive hit delay is equal to the default at
     * {@link #DEFAULT_CONSECUTIVE_HIT_DELAY}.
     * 
     * @return <code>true</code> if the delay is default; <code>false</code>
     *         otherwise.
     */
    public boolean hasDefaultDelay() {
        return this.delay == DEFAULT_CONSECUTIVE_HIT_DELAY;
    }
    
    /**
     * Checks if the bind should activate {@link #ON_PRESS}.
     * 
     * @return <code>true</code> if the bind should activate on press;
     *         <code>false</code> otherwise.
     */
    public boolean isOnPress() {
        return this.onPress;
    }
    
    /**
     * Checks if the bind should activate {@link #ON_RELEASE}.
     * 
     * @return <code>true</code> if the bind should activate on release;
     *         <code>false</code> otherwise.
     */
    public boolean isOnRelease() {
        return !this.onPress;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o != null && o instanceof BindOptions options) {
            // Vital equals information is the number of hits required and if the Bind would activate on Press or Release.
            return this.hits == options.hits() && this.onPress == options.isOnPress();
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.hits, this.onPress);
    }
    
    @Override
    public String toString() {
        return String.format("BindOptions == Hits:%s | Delay:%s | On Press:%s", this.hits, this.delay, this.onPress);
    }
}
