package dev.mwhitney.gui.binds;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A tracker whose sole purpose is to track what the last individual input was
 * in order to test for consecutive hits.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public abstract class BindHitsTracker implements BindsFetcher {
    /**
     * The {@link ScheduledExecutorService} responsible for scheduling asynchronous
     * jobs which indicate if a delay has passed and a hit is no longer consecutive.
     */
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    /**
     * The latest timer, represented as a {@link ScheduledFuture}. We know the delay
     * has elapsed if {@link ScheduledFuture#isDone()} is <code>true</code>.
     */
    private volatile ScheduledFuture<?> timer;
    /** The last {@link BindInput} tracked by this tracker. */
    private volatile BindInput lastInput;
    /** The current consecutive hit counter. Does not measure total hits, just consecutive hits. */
    private final AtomicInteger hits = new AtomicInteger(0);

    /**
     * Checks if the last internal timer is still non-<code>null</code> and valid.
     * 
     * @return <code>true</code> if valid; <code>false</code> otherwise.
     */
    private boolean isTimerValid() {
        return this.timer != null;
    }

    /**
     * Checks if the last internal timer is done, also implying that it is valid.
     * <p>
     * If a timer is done, then the delay has passed, so the next hit should not be
     * considered consecutive.
     * 
     * @return <code>true</code> if the timer is valid and done; <code>false</code>
     *         otherwise.
     */
    private boolean isTimerDone() {
        return (isTimerValid() && this.timer.isDone());
    }

    /**
     * Checks if the passed {@link BindInput} matches the input received in the last
     * hit.
     * <p>
     * This method will return <code>false</code> if either the last input or passed
     * input are <code>null</code>.
     * 
     * @param input - the {@link BindInput} to compare.
     * @return <code>true</code> if the two inputs match via
     *         {@link BindInput#matches(BindInput)}; <code>false</code> otherwise.
     */
    public boolean wasLastHit(BindInput input) {
        if (lastInput == null || input == null) return false;
        return this.lastInput.matches(input);
    }

    /**
     * Gets the current consecutive hit counter number.
     * 
     * @return an int with the current number of consecutive hits.
     */
    public int getHits() {
        return this.hits.get();
    }

    /**
     * Resets the consecutive hit counter back to one.
     */
    public void resetHits() {
        this.hits.set(1);
    }
    
    /**
     * Clears the current tracking data, resetting the consecutive hit counter back
     * to one and canceling the delay timer if it's active. This method does
     * <b>not</b> clear what the last input was, but since the timer cancels, the
     * next hit should be treated as non-consecutive regardless.
     */
    public void clear() {
        // Cancel timer, preventing consecutive hits even if the next is the same input.
        if (isTimerValid() && !isTimerDone()) timer.cancel(false);
        // Reset the hits back to one.
        resetHits();
    }

    /**
     * Checks if a keyboard bind with the same input, but a higher hits amount,
     * exists.
     * <p>
     * This is an important check, as it indicates whether or not the tracker should
     * continue counting hits instead of resetting the count. Furthermore,
     * retrieving the first match allows the timer to use the custom delay for that
     * bind.
     * 
     * @param input - the {@link KeyInput} to check under. Multiple identical inputs
     *              may exist, but with separate hit requirements.
     * @return a {@link BindDetails} instance containing an existing bind with a
     *         higher hits requirement, or <code>null</code> if none is found.
     */
    public BindDetails<KeyInput> higherKeyHitsExists(KeyInput input) {
        final ConcurrentHashMap<KeyInput, ConcurrentSkipListMap<Integer, BindDetails<KeyInput>>> binds = getKeyBinds();
        if (binds == null) return null;
        else {
            final ConcurrentSkipListMap<Integer, BindDetails<KeyInput>> inputBinds = binds.get(input);
            if (inputBinds == null) return null;
            // Get an Entry pair for the first bind with hits above the current hit count.
            final Entry<Integer, BindDetails<KeyInput>> higherDetails = inputBinds.ceilingEntry(input.hits() + 1);
            // The entire Entry object will be null if no higher hits binding exists.
            // In that case, return null. Otherwise, get and return bind details.
            return (higherDetails != null ? higherDetails.getValue() : null);
        }
    }

    /**
     * Checks for a mouse bind with the same input, but a higher hits amount,
     * exists.
     * <p>
     * This is an important check, as it indicates whether or not the tracker should
     * continue counting hits instead of resetting the count. Furthermore,
     * retrieving the first match allows the timer to use the custom delay for that
     * bind.
     * 
     * @param input - the {@link MouseInput} to check under. Multiple identical
     *              inputs may exist, but with separate hit requirements.
     * @return a {@link BindDetails} instance containing an existing bind with a
     *         higher hits requirement, or <code>null</code> if none is found.
     */
    public BindDetails<MouseInput> higherMouseHitsExists(MouseInput input) {
        final ConcurrentHashMap<MouseInput, ConcurrentSkipListMap<Integer, BindDetails<MouseInput>>> binds = getMouseBinds();
        if (binds == null) return null;
        else {
            final ConcurrentSkipListMap<Integer, BindDetails<MouseInput>> inputBinds = binds.get(input);
            if (inputBinds == null) return null;
            // Get an Entry pair for the first bind with hits above the current hit count.
            final Entry<Integer, BindDetails<MouseInput>> higherDetails = inputBinds.ceilingEntry(input.hits() + 1);
            // The entire Entry object will be null if no higher hits binding exists.
            // In that case, return null. Otherwise, get and return bind details.
            return (higherDetails != null ? higherDetails.getValue() : null);
        }
    }

    /**
     * Restarts the internal consecutive hits timer with the passed delay in
     * milliseconds, but with no code to run after the timer is done.
     * 
     * @param delay - an int with the hit delay in milliseconds.
     */
    private void restartTimer(int delay) {
        restartTimer(delay, null);
    }

    /**
     * Restarts the internal consecutive hits timer with the passed delay and code
     * to execute after the timer is done.
     * 
     * @param delay - an int with the hit delay in milliseconds.
     * @param run   - a {@link Runnable} with code to execute once the timer is
     *              done.
     */
    public void restartTimer(int delay, Runnable run) {
        if (delay < 1) delay = BindOptions.DEFAULT_CONSECUTIVE_HIT_DELAY;
        this.timer = scheduler.schedule((run == null ? () -> {
            /*
             * Do Nothing -- This section would be useful if executing an action after
             * delay. Ex: Double-click to fullscreen a window. First click sets up this
             * timer, which would also usually play/pause. If the timer expires, then
             * play/pause. If the timer gets cancelled by a second click (and no other click
             * shortcuts >2 clicks), then execute the fullscreen shortcut instead and don't
             * put anything in here.
             */
        } : run), delay, TimeUnit.MILLISECONDS);
    }

    /**
     * Gets the current consecutive hits number, <b>but only if</b> the passed input
     * matches the last hit. If they do not match, a value of <code>1</code> is
     * returned.
     * 
     * @param input - the {@link BindInput} to get the hits number under.
     * @return the current consecutive hits number, or <code>1</code> if the passed
     *         input was not the last to be called via {@link #hit(BindInput)}.
     */
    public int get(BindInput input) {
        return (wasLastHit(input) ? getHits() : 1);
    }

    /**
     * Tracks a hit under the passed {@link BindInput}, returning the new hit number
     * afterwards.
     * <p>
     * This method does more than simply increment the internal consecutive hits
     * counter. It starts the tracking procedures for consecutive hits, meaning that
     * if another identical call is made to this method within the delay, then the
     * hit will be considered consecutive.
     * <p>
     * If instead the passed input differs, or the delay time has elapsed, then the
     * hit will still be tracked but will not be consecutive, resetting the hits
     * number to <code>1</code>.
     * 
     * @param input - the {@link BindInput} to track a hit under.
     * @return the current consecutive hits number.
     */
    public int hit(BindInput input) {
        // Debug
//        System.out.println("> Hit Registered by Hits Tracker under input: " + input);

        // Input differs from last OR delay timer has elapsed. Reset.
        if (!wasLastHit(input) || isTimerDone()) {
            hits.set(1);
            lastInput = input;
            if (isTimerValid() && !timer.isDone()) timer.cancel(false);
        }
        // Same key pressed within timeout. Cancel timer and increment hits.
        else if (isTimerValid() && !timer.isDone()) {
            timer.cancel(false);
            hits.incrementAndGet();
        }

        // Update input instance to contain its hit count.
        input.setHits(hits.get());
        
        // Check if there is any binding with more hits than this. Schedule a timer if there is.
        BindDetails<?> higherHit = null;
        if (input instanceof KeyInput keyInput)
            higherHit = higherKeyHitsExists(keyInput);
        else if (input instanceof MouseInput mouseInput)
            higherHit = higherMouseHitsExists(mouseInput);
        else System.err.println("<!> ERROR: Got a non-Key or Mouse Input instance in BindHitsTracker.");
        
        // A higher hit bind exists. Schedule the timer with that bind's delay.
        if (higherHit != null) restartTimer(higherHit.options().delay());
        // If no higher hit exists, the reset will happen in hitUp method.
        
        // Return adjusted hits value.
        return input.hits();
    }
    
    /**
     * Tracks a release (up) operation on a hit under the passed {@link BindInput},
     * returning what the hit number was prior to calling this method.
     * <p>
     * This method is a "finalizer" of sorts, resetting the consecutive hit counter
     * if there is no bind under the passed input with a higher hit requirement.
     * This functionality used to exist within {@link #hit(BindInput)}, but
     * resetting the count there meant that binds which activated on release would
     * never work. The hits counter would reset after {@link #hit(BindInput)}, then
     * any {@link #get(BindInput)} operation after an input release would return
     * <code>1</code>.
     * <p>
     * Instead, the call to this method not only retrieves the hit count, but will
     * handle the reset and finalize the down/up cycle for an input.
     * <p>
     * This method will return what the hits number was <b>before it was called</b>.
     * Therefore, even if the hit counter is reset here, the count from the last
     * {@link #hit(BindInput)} will return. u
     * 
     * @param input - the {@link BindInput} which was released.
     * @return the current consecutive hits number, or <code>1</code> if the passed
     *         input was <b>not</b> the last hit.
     */
    public int hitUp(BindInput input) {
        // If input was not last hit, return default of 1 hit.
        if (!wasLastHit(input)) return 1;
        
        // Update input instance to contain its hit count.
        final int hitsBeforeReset = hits.get();
        input.setHits(hitsBeforeReset);
        
        // Check if there is any binding with more hits than this. If none exists, reset hits.
        final BindDetails<?> higherHit;
        if (input instanceof KeyInput keyInput)
            higherHit = higherKeyHitsExists(keyInput);
        else if (input instanceof MouseInput mouseInput)
            higherHit = higherMouseHitsExists(mouseInput);
        else return hitsBeforeReset;  // Only handle KeyInput/MouseInput instances.
        
        // No higher hit exists. Reset hits.
        if (higherHit == null) resetHits();
        
        return hitsBeforeReset;
    }
}
