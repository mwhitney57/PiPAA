package dev.mwhitney.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

/**
 * The state of a PiPWindow, which contains numerous fields relating to its status.
 * 
 * @author mwhitney57
 */
public class PiPWindowState {
    /**
     * Properties or fields relating to a window's state.
     */
    public enum StateProp {
        /**
         * Whether the window's player is not set.
         * The internal player choice property only changes when a player is set to be on.
         */
        PLAYER_NONE,
        /**
         * Whether the window's player is set as VLC.
         * The internal player choice property only changes when a player is set to be on.
         */
        PLAYER_VLC,
        /**
         * Whether the window's player is set as Swing's JLabel for images.
         * The internal player choice property only changes when a player is set to be on.
         */
        PLAYER_SWING,
        /**
         * Whether the window's player is set as a combination of the VLC and SWING players.
         * The internal player choice property only changes when a player is set to be on.
         */
        PLAYER_COMBO,
        /**
         * Whether the window is loading media.
         */
        LOADING,
        /**
         * Whether the window is saving/caching its current media.
         */
        SAVING_MEDIA,
        /**
         * Whether the media in the window is manually paused.
         */
        MANUALLY_PAUSED,
        /**
         * Whether the media in the window was manually stopped.
         */
        MANUALLY_STOPPED,
        /**
         * Whether the media in the window is muted locally.
         */
        LOCALLY_MUTED,
        /**
         * Whether the window has begun closing at any point.
         * Cannot be turned OFF (false) once set to ON (true).
         */
        CLOSING,
        /**
         * Whether the window is closed.
         * Cannot be turned OFF (false) once set to ON (true).
         */
        CLOSED;
    }

    /** A window media player value that represents which player is in use. */
    private static final int PLAYER_NONE = 0, PLAYER_VLC = 1, PLAYER_SWING = 2, PLAYER_COMBO = 3;
    
    /**
     * An int with a value that represents the current media player set in the window.
     */
    private int player = 0;
    /**
     * A boolean for whether or not the window is loading media.
     */
    private boolean loading;
    /**
     * A boolean for whether or not the media in the window is being saved/cached.
     * This is tracked to prevent concurrent save attempts from taking place, which
     * could cause issues and break the saved media.
     */
    private boolean savingMedia;
    /**
     * A boolean for whether or not the media has been paused <b>manually</b>. This
     * will be false if the media is paused but it was done by the application and
     * not the user.
     */
    private boolean manuallyPaused;
    /**
     * A boolean for whether or not the media has been stopped <b>manually</b>. This
     * will be false if the media is stopped but it was done by the application and
     * not the user.
     */
    private boolean manuallyStopped;
    /**
     * A boolean for whether or not the media has been muted <b>locally</b>.
     * This is important to track so that each window's muted state remains
     * after a <b>global</b> mute has been activated/deactivated.
     */
    private boolean locallyMuted;
    
    /**
     * A boolean for whether to not the window is closing, which will
     * only and permanently read as true when it has happened once.
     * In other words, this should never be reset to false;
     */
    private boolean closing;
    /**
     * A boolean for whether or not the window has been closed, which is permanent.
     */
    private boolean closed;
    
    /**
     * A map of StateProps which each correspond to a map of added hooks tied to
     * either a <code>true</code> or <code>false</code> value. When a StateProp is
     * set to one of the two boolean values, that set of hooks is executed.
     * 
     * @see #hook(StateProp, boolean, Runnable)
     * @see #runHooks(StateProp, boolean)
     */
    private HashMap<StateProp, HashMap<Boolean, ArrayList<Runnable>>> hooks;
    
    /**
     * Creates a new PiPWindowState instance.
     * <p>
     * This constructor performs basic setup on the internal hooks feature so that
     * it may be used immediately.
     * 
     * @see #hook(StateProp, boolean, Runnable)
     */
    public PiPWindowState() {
        this.hooks = new HashMap<StateProp, HashMap<Boolean, ArrayList<Runnable>>>();
        for (final StateProp prop : StateProp.values()) {
            final HashMap<Boolean, ArrayList<Runnable>> onOffMap = new HashMap<Boolean, ArrayList<Runnable>>();
            onOffMap.put(Boolean.TRUE, new ArrayList<Runnable>());
            onOffMap.put(Boolean.FALSE, new ArrayList<Runnable>());
            hooks.put(prop, onOffMap);
        }
    }
    
    /**
     * Sets the passed StateProp to <b>ON</b> (enabled).
     * 
     * @param prop - the StateProp to enable.
     * @return this PiPWindowState instance.
     */
    public PiPWindowState on(StateProp prop) {
        set(prop, true);
        return this;
    }
    
    /**
     * Sets the passed StateProps to <b>ON</b> (enabled).
     * 
     * @param props - the StateProps to enable.
     * @return this PiPWindowState instance.
     */
    public PiPWindowState on(StateProp... props) {
        for (final StateProp prop : props) {
            set(prop, true);
        }
        return this;
    }
    
    /**
     * Sets the passed StateProp to <b>OFF</b> (disabled).
     * 
     * @param prop - the StateProp to disable.
     * @return this PiPWindowState instance.
     */
    public PiPWindowState off(StateProp prop) {
        set(prop, false);
        return this;
    }
    
    /**
     * Sets the passed StateProps to <b>OFF</b> (disabled).
     * 
     * @param props - the StateProps to disable.
     * @return this PiPWindowState instance.
     */
    public PiPWindowState off(StateProp... props) {
        for (final StateProp prop : props) {
            set(prop, false);
        }
        return this;
    }
    
    /**
     * Sets the passed StateProp to the passed boolean value.
     * 
     * @param prop - the StateProp to change.
     * @param val - the new value of the StateProp.
     */
    public void set(StateProp prop, boolean val) {
        switch(prop) {
        case PLAYER_NONE      -> this.player = (val ? PLAYER_NONE  : this.player);
        case PLAYER_VLC       -> this.player = (val ? PLAYER_VLC   : this.player);
        case PLAYER_SWING     -> this.player = (val ? PLAYER_SWING : this.player);
        case PLAYER_COMBO     -> this.player = (val ? PLAYER_COMBO : this.player);
        case LOADING          -> this.loading         = val;
        case SAVING_MEDIA     -> this.savingMedia     = val;
        case MANUALLY_PAUSED  -> this.manuallyPaused  = val;
        case MANUALLY_STOPPED -> this.manuallyStopped = val;
        case LOCALLY_MUTED    -> this.locallyMuted    = val;
        // Permanent State Property Changes
        case CLOSING          -> this.closing = (val ? true : this.closing);
        case CLOSED           -> this.closed  = (val ? true : this.closed);
        }
        CompletableFuture.runAsync(() -> runHooks(prop, val));
    }
    
    /**
     * Checks if the passed StateProp is on/enabled/true.
     * 
     * @param prop - the StateProp to check.
     * @return <code>true</code> if the property is on; <code>false</code> otherwise.
     */
    public boolean is(StateProp prop) {
        return switch(prop) {
        case PLAYER_NONE      -> (this.player == PLAYER_NONE);
        case PLAYER_VLC       -> (this.player == PLAYER_VLC);
        case PLAYER_SWING     -> (this.player == PLAYER_SWING);
        case PLAYER_COMBO     -> (this.player == PLAYER_COMBO);
        case LOADING          ->  this.loading;
        case SAVING_MEDIA     ->  this.savingMedia;
        case MANUALLY_PAUSED  ->  this.manuallyPaused;
        case MANUALLY_STOPPED ->  this.manuallyStopped;
        case LOCALLY_MUTED    ->  this.locallyMuted;
        case CLOSING          ->  this.closing;
        case CLOSED           ->  this.closed;
        default               ->  false;
        };
    }
    
    /**
     * Checks if the passed StateProp(s) are <b>all</b> on/enabled/true.
     * 
     * @param props - one or more StateProps to check.
     * @return <code>true</code> if all properties are on; <code>false</code> otherwise.
     */
    public boolean is(StateProp... props) {
        for (final StateProp prop : props) {
            if (not(prop))
                return false;
        }
        return true;
    }
    
    /**
     * Checks if the passed StateProp is off/disabled/false.
     * 
     * @param prop - the StateProp to check.
     * @return <code>true</code> if the property is off; <code>false</code> otherwise.
     */
    public boolean not(StateProp prop) {
        return !is(prop);
    }
    
    /**
     * Checks if the passed StateProp(s) are <b>all</b> off/disabled/false.
     * 
     * @param props - one or more StateProps to check.
     * @return <code>true</code> if all properties are off; <code>false</code> otherwise.
     */
    public boolean not(StateProp... props) {
        for (final StateProp prop : props) {
            if (is(prop))
                return false;
        }
        return true;
    }
    
    /**
     * Gets the current, raw value of the passed StateProp. Since the type of this
     * raw value differs by StateProp, the return type of this method is
     * {@link Object}.
     * 
     * @param prop - the StateProp to retrieve the raw value of.
     * @return an Object with the raw value.
     */
    public Object get(StateProp prop) {
        return switch(prop) {
        case PLAYER_NONE, PLAYER_VLC, PLAYER_SWING, PLAYER_COMBO -> this.player;
        default                                                  -> is(prop);
        };
    }
    
    /**
     * Runs any and all hooks into the passed {@link StateProp} with the passed
     * boolean value.
     * 
     * @param prop - the {@link StateProp} that should have its hooks run.
     * @param val  - the corresponding boolean value of the {@link StateProp}'s
     *             hooks.
     */
    private void runHooks(final StateProp prop, final boolean val) {
        final ArrayList<Runnable> runs = this.hooks.get(prop).get(Boolean.valueOf(val));
        if (runs.size() > 0) {
            final Iterator<Runnable> runsIter = runs.iterator();
            while (runsIter.hasNext()) {
                runsIter.next().run();
                runsIter.remove();
            }
        }
    }
    
    /**
     * Creates a hook into the window state and execute the passed Runnable when the
     * passed {@link StateProp} is set to be equal to the passed boolean.
     * 
     * @param prop - the {@link StateProp} to hook into.
     * @param val  - the boolean value of the {@link StateProp} when the hook should
     *             activate.
     * @param run  - a {@link Runnable} with the code to execute when the hook
     *             condition is met.
     */
    public void hook(final StateProp prop, final boolean val, final Runnable run) {
        this.hooks.get(prop).get(Boolean.valueOf(val)).add(run);
    }
    
    /**
     * Removes every hook attached to the passed {@link StateProp} and boolean value.
     * 
     * @param prop - the {@link StateProp} to unhook.
     * @param val  - the corresponding boolean value of the {@link StateProp}'s
     *             hooks.
     */
    public void unhook(final StateProp prop, final boolean val) {
        this.hooks.get(prop).get(Boolean.valueOf(val)).clear();
    }
    
    /**
     * Removes every hook attached to <b>every</b> {@link StateProp} within this window state.
     */
    public void unhookAll() {
        for (final StateProp prop : StateProp.values()) {
            unhook(prop, true);
            unhook(prop, false);
        }
    }
    
    /**
     * Disables hooks within this PiPWindowState instance by removing and clearing
     * them.
     * <p>
     * <b>This method should only be executed once, and hooks become unusable after
     * calling it.</b> Therefore, it should only be called when the PiPWindowState
     * is to be disposed of or hooks will certainly no longer be used.
     */
    public void disableHooks() {
        unhookAll();
        this.hooks.clear();
        this.hooks = null;
    }
}
