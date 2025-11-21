package dev.mwhitney.gui.binds;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * A class that manages all of the keyboard and mouse bindings throughout the
 * entire application.
 * <p>
 * Furthermore, it handles all of the tracking of keyboard and mouse inputs,
 * allowing for easy addition, subtraction, or comparison of said inputs from
 * internal maps. This simplifies the bindings process. The class acts as a
 * central hub. Only one instance should be run across the entire application.
 * <p>
 * If {@link Shortcut} bindings are updated or changed, the changes should be
 * updated here, as other classes which interface with the controller will ask
 * it for the current bindings when checking if a binding has been satisfied.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public class BindController {
    /**
     * A {@link ConcurrentHashMap} with the {@link KeyInput} as the <b>key</b> and
     * another map as the <b>value</b>.
     * <p>
     * The secondary map is indexed by an integer for the amount of <b>hits</b>
     * (a.k.a. presses) required to activate a binded {@link Shortcut} in its value
     * field. Each of these shortcuts is represented by a {@link BindDetails}
     * instance which fully encapsulates information necessary for the bind to
     * activate, including any additional {@link BindOptions}.
     */
    private final ConcurrentHashMap<KeyInput, ConcurrentSkipListMap<Integer, BindDetails<KeyInput>>> keyBinds = new ConcurrentHashMap<>();
    /**
     * A {@link ConcurrentHashMap} with the {@link MouseInput} as the <b>key</b> and
     * another map as the <b>value</b>.
     * <p>
     * The secondary map is indexed by an integer for the amount of <b>hits</b>
     * (a.k.a. clicks) required to activate a binded {@link Shortcut} in its value
     * field. Each of these shortcuts is represented by a {@link BindDetails}
     * instance which fully encapsulates information necessary for the bind to
     * activate, including any additional {@link BindOptions}.
     */
    private final ConcurrentHashMap<MouseInput, ConcurrentSkipListMap<Integer, BindDetails<MouseInput>>> mouseBinds = new ConcurrentHashMap<>();
    /**
     * The {@link CustomModifierManager} which handles all custom modifiers.
     * <p>
     * Java provides a number of bitwise masks for typical modifiers, such as
     * {@link InputEvent#CTRL_DOWN_MASK} or {@link InputEvent#SHIFT_DOWN_MASK}, but
     * there is a finite number of modifiers, and it becomes limiting. Certain keys,
     * especially the arrow keys, can reasonably have many functions, but
     * limitations arise given this short list of modifiers.
     * <p>
     * This manager uses the additional five custom modifier slots provided by
     * {@link ShortcutMask} and provides the ability for their default bindings to
     * be overridden, just like with user-configured keyboard and mouse binds.
     */
    private final CustomModifierManager customModsManager = new CustomModifierManager();

    /** The {@link ConcurrentHashMap} set containing all active (currently-pressed) keyboard inputs. */
    private final Set<KeyInput>   activeKeyInputs   = ConcurrentHashMap.newKeySet();
    /** The {@link ConcurrentHashMap} set containing all active (currently-pressed) mouse inputs. */
    private final Set<MouseInput> activeMouseInputs = ConcurrentHashMap.newKeySet();
    
    /** The {@link BindHitsTracker} for all keyboard inputs. */
    private final BindHitsTracker keyHitsTracker = new BindHitsTracker() {
        @Override
        public ConcurrentHashMap<KeyInput, ConcurrentSkipListMap<Integer, BindDetails<KeyInput>>> getKeyBinds() { return BindController.this.keyBinds; }
        @Override
        public ConcurrentHashMap<MouseInput, ConcurrentSkipListMap<Integer, BindDetails<MouseInput>>> getMouseBinds() { return BindController.this.mouseBinds; }
    };
    /** The {@link BindHitsTracker} for all mouse inputs. */
    private final BindHitsTracker mouseHitsTracker = new BindHitsTracker() {
        @Override
        public ConcurrentHashMap<KeyInput, ConcurrentSkipListMap<Integer, BindDetails<KeyInput>>> getKeyBinds() { return BindController.this.keyBinds; }
        @Override
        public ConcurrentHashMap<MouseInput, ConcurrentSkipListMap<Integer, BindDetails<MouseInput>>> getMouseBinds() { return BindController.this.mouseBinds; }
    };
    
    /** The input for hitting Control (Ctrl) Key with no additional modifiers. */
    private static final KeyInput CTRL_KEY      = new KeyInput(KeyEvent.VK_CONTROL, 0);
    /** The input for hitting Shift Key with no additional modifiers. */
    private static final KeyInput SHIFT_KEY     = new KeyInput(KeyEvent.VK_SHIFT, 0);
    /** The input for hitting Alt Key with no additional modifiers. */
    private static final KeyInput ALT_KEY       = new KeyInput(KeyEvent.VK_ALT, 0);
    /** The input for hitting Meta (Cmd⌘ on macOS) Key with no additional modifiers. */
    private static final KeyInput META_KEY      = new KeyInput(KeyEvent.VK_META, 0);     // Triggers META_DOWN_MASK on macOS.
    /** The input for hitting Windows Key with no additional modifiers. */
//    private static final KeyInput WIN_KEY       = new KeyInput(KeyEvent.VK_WINDOWS, 0);  // Does not trigger META_DOWN_MASK on Windows. Unused.
    /** The input for hitting Alt Graph (AltGr) Key with no additional modifiers. */
    private static final KeyInput ALT_GRAPH_KEY = new KeyInput(KeyEvent.VK_ALT_GRAPH, 0);
    
    /**
     * Creates a BindController using the default bindings for each shortcut,
     * specified by each {@link Shortcut} value's constructor.
     * <p>
     * The defaults can be retrieved via {@link #getDefaults()}.
     */
    public BindController() { this(null); }
    
    /**
     * Creates a BindController using the passed {@link Map} containing each
     * {@link Shortcut} and a {@link Bind} array with any binds to register to that
     * shortcut.
     * 
     * @param binds - the {@link Map} of shortcuts and binds.
     */
    public BindController(Map<Shortcut, Bind<?>[]> binds) {
        if (binds != null) setBinds(binds);
        else               setBinds(getDefaults());
    }
    
    /**
     * Loads any valid custom modifiers provided in the passed {@link Map}.
     * 
     * @param map - a {@link Map} with custom modifier masks and an input code bound
     *            to each.
     * @return this {@link BindController} instance.
     */
    public BindController loadCustomModifiers(Map<ShortcutMask, Integer> map) {
        this.customModsManager.load(map);
        return this;
    }
    
    /**
     * Gets keyboard binds identified by the passed {@link KeyInput}, if any exist.
     * <p>
     * Some binds are only registered to fire after being pressed a certain amount
     * of times consecutively (hits). Therefore, more than one bind may be found
     * using the passed input.
     * 
     * @param input - the {@link KeyInput} used to identify the keyboard bind.
     * @return a {@link ConcurrentSkipListMap} with the binds found under the passed
     *         input, or <code>null</code> if no binds were found at all. Can also
     *         return <code>null</code> if the passed input is <code>null</code>.
     */
    public ConcurrentSkipListMap<Integer, BindDetails<KeyInput>> getKeyBind(KeyInput input) {
        return (input == null ? null : this.keyBinds.get(input));
    }

    /**
     * Gets mouse binds identified by the passed {@link MouseInput}, if any exist.
     * <p>
     * Some binds are only registered to fire after being pressed a certain amount
     * of times consecutively (hits). Therefore, more than one bind may be found
     * using the passed input.
     * 
     * @param input - the {@link MouseInput} used to identify the mouse bind.
     * @return a {@link ConcurrentSkipListMap} with the binds found under the passed
     *         input, or <code>null</code> if no binds were found at all. Can also
     *         return <code>null</code> if the passed input is <code>null</code>.
     */
    public ConcurrentSkipListMap<Integer, BindDetails<MouseInput>> getMouseBind(MouseInput input) {
        return (input == null ? null : this.mouseBinds.get(input));
    }
    
    /**
     * Finds any duplicate binds, including multiples within a single
     * {@link Shortcut} or duplicates across multiple shortcuts. This can be
     * especially helpful to check if the user shortcut configuration is fully
     * valid, containing only unique binds for each {@link Shortcut}.
     * 
     * @param bindings - the {@link Map} containing each {@link Shortcut} and its
     *                 {@link Bind} array containing zero or more binds.
     * @return a {@link Map} containing any duplicate {@link Bind}s
     *         alongside a {@link List} of {@link Shortcut}s containing it.
     */
    public Map<Bind<?>, List<Shortcut>> findDuplicateBinds(Map<Shortcut, Bind<?>[]> bindings) {
        // Create Map which tracks every Shortcut which houses a particular Bind.
        final Map<Bind<?>, List<Shortcut>> bindsToShortcuts = new HashMap<>();
        
        // Build the mapping of Binds to their Shortcuts.
        bindings.forEach((shortcut, binds) -> {
            // For each Bind in the Shortcut...
            for (final Bind<?> bind : binds) {
                // Put the Bind in the map, adding the Shortcut to the associated List. Create the List if it's not present.
                bindsToShortcuts.computeIfAbsent(bind, k -> new ArrayList<>()).add(shortcut);
            }
        });
        
        // Create a Map which will contain only the Binds with multiple Shortcuts, indicating duplicates.
        final Map<Bind<?>, List<Shortcut>> duplicates = new HashMap<>();
        bindsToShortcuts.forEach((bind, shortcutList) -> {
            // If a Bind's Shortcut List contains multiple elements, we know we have duplicates.
            if (shortcutList.size() > 1) duplicates.put(bind, shortcutList);
        });
        
        return duplicates;
    }
    
    /**
     * A debug method which prints out any found duplicate bindings.
     * <p>
     * If you need to get the duplicates, use {@link #findDuplicateBinds(Map)}.
     * 
     * @param binds - the {@link Map} containing each {@link Shortcut} and its
     *              {@link Bind} array containing zero or more binds.
     */
    public void warnOnDuplicateBinds(Map<Shortcut, Bind<?>[]> binds) {
        findDuplicateBinds(binds).forEach((bind, shortcutList) ->
            System.err.println("<Shortcut Warning> Duplicate bind, in shortcuts " + shortcutList + ": " + bind.details().input())
        );
    }
    
    /**
     * Checks if any keyboard or mouse binds are set.
     * 
     * @return <code>true</code> if <b>any</b> keyboard or mouse binds are set;
     *         <code>false</code> otherwise.
     */
    public boolean hasBinds() {
        return (!this.keyBinds.isEmpty() || !this.mouseBinds.isEmpty());
    }
    
    /**
     * Sets the current binds using the passed {@link Map}. The <b>key</b> for the
     * map is each {@link Shortcut}. The <b>value</b> for the map is a {@link Bind}
     * array containing zero or more binds.
     * 
     * @param binds - the {@link Map} with the binds to set.
     * @return this BindController instance.
     */
    public BindController setBinds(Map<Shortcut, Bind<?>[]> binds) {
        // Clear current binds if any are present.
        if (hasBinds()) clearBinds();
        // Issue a printed warning if there are duplicate binds across the map.
        warnOnDuplicateBinds(binds);
        // For each Shortcut and its Binds...
        binds.forEach((action, bindArr) -> {
            // For each Bind in the Shortcut's Binds
            for (final Bind<?> bind : bindArr) {
                // If either the input or options is missing, ignore the bind.
                if (!bind.details().hasInput()) continue;
                
                // Cast the Bind depending on its instance, then add the bind to its appropriate map.
                if (bind instanceof KeyBind keyBind) {
                    final BindDetails<KeyInput> details = keyBind.details();
                    keyBinds.computeIfAbsent(details.input(), b -> new ConcurrentSkipListMap<>())
                        .put(details.input().hits(), new BindDetails<KeyInput>(action, details.input(), details.options()));
                }
                else if (bind instanceof MouseBind mouseBind) {
                    final BindDetails<MouseInput> details = mouseBind.details();
                    mouseBinds.computeIfAbsent(details.input(), b -> new ConcurrentSkipListMap<>())
                        .put(details.input().hits(), new BindDetails<MouseInput>(action, details.input(), details.options()));
                }
                else continue;  // Ignore otherwise. Only handling Key/Mouse binds.
            }
        });
        return this;
    }

    /**
     * Clears all registered keyboard <b>and</b> mouse binds.
     */
    private void clearBinds() {
        clearKeyBinds();
        clearMouseBinds();
    }

    /**
     * Clears all registered keyboard binds.
     */
    private void clearKeyBinds() {
        this.keyBinds.clear();
    }

    /**
     * Clears all registered mouse binds.
     */
    private void clearMouseBinds() {
        this.mouseBinds.clear();
    }
        
    /**
     * Checks if the passed input hits int is a multiple of the passed bind hits
     * int.
     * <p>
     * More clearly, this method performs <b>two</b> simple operations. First, it
     * ensures that the second integer is greater than or equal to the first.
     * Second, it ensures that the second integer is a multiple of the first via the
     * modulo operator (%).
     * <p>
     * The purpose of this method is to verify when an input should trigger a
     * particular bind.
     * 
     * @param bindHits  - the hits integer which is being compared against.
     * @param inputHits - the hits integer stemming from a recent input which is
     *                  being checked to ensure that it is a multiple.
     * @return <code>true</code> if the second integer is a greater multiple of the
     *         first; <code>false</code> otherwise.
     */
    public boolean hitsMultOfBind(int bindHits, int inputHits) {
        return (inputHits >= bindHits && inputHits % bindHits == 0);
    }

    /**
     * Removes all active key inputs that have a key code matching the passed
     * integer.
     * <p>
     * This method is preferred when a key has been released, since the modifiers
     * become partially irrelevant. What modifiers were pressed makes no difference
     * as to whether or not an input should be considered released. The key was
     * released, end of story.
     * <p>
     * We still keep and consider the modifiers in order to trigger release-specific
     * binds, but we must remove all instances of the press.
     * 
     * @param code - the key code of the active inputs to remove.
     */
    private void removeAllKeyPressesOf(int code) {
        this.activeKeyInputs.removeIf(i -> i.code() == code);
    }
    
    /**
     * Removes all active mouse inputs that have a mouse code matching the passed
     * integer.
     * <p>
     * This method is preferred when a button has been released, since the modifiers
     * become partially irrelevant. What modifiers were pressed makes no difference
     * as to whether or not an input should be considered released. The button was
     * released, end of story.
     * <p>
     * We still keep and consider the modifiers in order to trigger release-specific
     * binds, but we must remove all instances of the press.
     * 
     * @param code - the mouse button code of the active inputs to remove.
     */
    private void removeAllMousePressesOf(int code) {
        this.activeMouseInputs.removeIf(i -> i.code() == code);
    }
    
    /**
     * Checks if the passed input code integer is a <b>modifier</b>. This method
     * checks for matches against all <b>custom modifiers</b> as well as Java's
     * built-in options like {@link InputEvent#CTRL_DOWN_MASK}.
     * 
     * @param code - an integer with the input code to check.
     * @return {@code true} if the input code matches a modifier key; {@code false}
     *         otherwise.
     */
    public boolean isModifier(int code) {
        return this.customModsManager.isCustomModifier(code) || BindInput.inputCodeAsMask(code) != -1;
    }

    /**
     * Gets all of the actively pressed <b>custom</b> modifiers as a single bitwise
     * mask.
     * 
     * @return an int with the combined bitwise mask of all pressed custom
     *         modifiers. If none are pressed, the value will be {@code 0}.
     */
    public int getCustomModifiers() {
        return getCustomKeyModifiers() | getCustomMouseModifiers();
    }

    /**
     * Gets all of the actively pressed <b>custom</b> key modifiers as a single
     * bitwise mask.
     * 
     * @return an int with the combined bitwise mask of all pressed custom
     *         modifiers. If none are pressed, the value will be {@code 0}.
     */
    public int getCustomKeyModifiers() {
        return activeKeyInputs.stream()
                .map(KeyInput::code)
                .map(customModsManager::getCustomModifier)
                .filter(Objects::nonNull)
                .map(ShortcutMask::mask)
                .reduce(0, (mask, i) -> mask | i);
    }

    /**
     * Gets all of the actively pressed <b>custom</b> mouse modifiers as a single
     * bitwise mask.
     * 
     * @return an int with the combined bitwise mask of all pressed custom
     *         modifiers. If none are pressed, the value will be {@code 0}.
     */
    public int getCustomMouseModifiers() {
        return activeMouseInputs.stream()
                .map(MouseInput::code)
                .map(customModsManager::getCustomModifier)
                .filter(Objects::nonNull)
                .map(ShortcutMask::mask)
                .reduce(0, (mask, i) -> mask | i);
    }

    /**
     * Registers a key down under the passed {@link KeyInput}. If the key is already
     * down, nothing will happen and <code>null</code> will be returned. Otherwise,
     * the input will be marked as down until {@link #keyUp(KeyInput)} is called
     * with the same input.
     * <p>
     * In normal cases, the returned {@link List} will have one or more
     * {@link BindDetails}, each representing a bind which matches the passed
     * input's code and modifiers values. However, the list can have more than one
     * bind. The controller tracks consecutive hits via {@link BindHitsTracker}
     * instances. The passed input may have been pressed multiple times
     * consecutively within its delay window. If it was, the passed input has its
     * hits integer set. Binds will only be added to the list if the input then meets the
     * hits requirement, or a multiple of it. After that logic, the list is
     * returned. Therefore, the list can only be non-<code>null</code> and empty if
     * the input matches at least one bind, but the hit requirement has not been met
     * for any of them.
     * <p>
     * If no binds match the passed input at all, this method will still return
     * <code>null</code>.
     * 
     * @param input - the {@link KeyInput} which was pressed.
     * @return a {@link List} of registered binds which the input meets the
     *         conditions for.
     */
    public List<BindDetails<KeyInput>> keyDown(KeyInput input) {
        // Key input was already down. Return null early.
        if (isKeyDown(input)) return null;
        
        // Add to active inputs map.
        activeKeyInputs.add(input);
        
        // Get the map of all binds under the passed input.
        final ConcurrentSkipListMap<Integer, BindDetails<KeyInput>> binds = keyBinds.get(input);
        // No binds under the passed input. Return null early.
        if (binds == null) return null;
        // Set the hits using the tracker.
        input.setHits(keyHitsTracker.hit(input));
        
        // Iterate over every bind, adding those which have a compatible hits requirement.
        final List<BindDetails<KeyInput>> shortcuts = new ArrayList<>(binds.size());
        binds.forEach((i, d) -> {
            // Recent input's hits value meets or is a multiple of the bind's, so add it to the list.
            if (hitsMultOfBind(d.options().hits(), input.hits()))
                shortcuts.add(d);
        });
        return shortcuts;

    }

    /**
     * Removes the registered key under the passed {@link KeyInput}. If the key was not
     * down, nothing will happen and <code>null</code> will be returned. Otherwise,
     * the input will be unmarked as down.
     * <p>
     * In normal cases, the returned {@link List} will have one or more
     * {@link BindDetails}, each representing a bind which matches the passed
     * input's code and modifiers values. However, the list can have more than one
     * bind. The controller tracks consecutive hits via {@link BindHitsTracker}
     * instances. The passed input may have been pressed multiple times
     * consecutively within its delay window. If it was, the passed input has its
     * hits integer set. Binds will only be added to the list if the input then meets the
     * hits requirement, or a multiple of it. After that logic, the list is
     * returned. Therefore, the list can only be non-<code>null</code> and empty if
     * the input matches at least one bind, but the hit requirement has not been met
     * for any of them.
     * <p>
     * If no binds match the passed input at all, this method will still return
     * <code>null</code>.
     * 
     * @param input - the {@link KeyInput} which was pressed.
     * @return a {@link List} of registered binds which the input meets the
     *         conditions for.
     */
    public List<BindDetails<KeyInput>> keyUp(KeyInput input) {
        // Key was already up. Return null early.
        if (!isKeyDownAny(input.code())) return null;
        
        // Remove every press instance, even if modifiers are different, since the key is now up.
        removeAllKeyPressesOf(input.code());
        
        // Get the map of all binds under the passed input.
        final ConcurrentSkipListMap<Integer, BindDetails<KeyInput>> binds = keyBinds.get(input);
        // No binds under the passed input. Return null early.
        if (binds == null) return null;
        // Set the hits using the tracker.
        input.setHits(keyHitsTracker.hitUp(input));
        
        // Iterate over every bind, adding those which have a compatible hits requirement.
        final List<BindDetails<KeyInput>> shortcuts = new ArrayList<>(binds.size());
        binds.forEach((i, d) -> {
            if (hitsMultOfBind(d.options().hits(), input.hits())) {
                // Recent input's hits value meets or is a multiple of the bind's, so add it to the list.
                shortcuts.add(d);
            }
        });
        return shortcuts;
    }

    /**
     * Checks if the passed {@link KeyInput} is registered as down (pressed).
     * 
     * @param input - the {@link KeyInput} to check.
     * @return <code>true</code> if it is down; <code>false</code> otherwise.
     */
    public boolean isKeyDown(KeyInput input) {
        return activeKeyInputs.stream().anyMatch((i) -> i.matches(input));
    }

    /**
     * Checks if a key is registered as down under the passed key code integer,
     * disregarding any modifiers the input may have.
     * <p>
     * This method is not a true or fully accurate verification that a specific
     * input is down. Rather, it is only intended to check if a particular key is
     * pressed, in any fashion (with or without modifiers). The check is only
     * concerned about the key that is down, not the entire input, which would
     * include its modifiers mask in the comparison.
     * <p>
     * For example, if you just want to see if the <b><code>SPACE</code></b> key is down,
     * and you do not care about any modifiers it may have been pressed with.
     * <p>
     * If a fully accurate check is necessary, consider using
     * {@link #isKeyDown(KeyInput)}.
     * 
     * @param code - an integer with the key code to check for.
     * @return <code>true</code> if it is down; <code>false</code> otherwise.
     */
    public boolean isKeyDownAny(int code) {
        return activeKeyInputs.stream().anyMatch((m) -> (m.code() == code));
    }
    
    /**
     * Clears all of the active key inputs.
     * <p>
     * <b>Not to be confused with {@link #clearKeyBinds()}.</b> This method clears
     * active or down key inputs, not the internal map of registered key binds.
     * 
     * @return this BindController instance.
     */
    public BindController clearKeyInputs()  {
        activeKeyInputs.clear();
        return this;
    }
    
    /**
     * Filters out any key modifiers from the passed {@link MouseInput} that are
     * <b>not</b> currently pressed.
     * <p>
     * While this method may seem unnecessary, it is not. {@link MouseEvent} objects
     * from {@link MouseListener}'s methods seem to be unreliable or inconsistent
     * when it comes to the {@link InputEvent#getModifiersEx()} integer mask. For
     * example, on {@link MouseListener#mouseReleased(MouseEvent)} events, if the
     * button was {@link MouseEvent#BUTTON3} (RMB), the
     * {@link InputEvent#META_DOWN_MASK} value of <code>256</code> is sometimes
     * present even when no keys are pressed. For {@link MouseEvent#BUTTON2} (MMB),
     * a similar issue may arise, getting {@link InputEvent#ALT_DOWN_MASK} value of
     * <code>512</code> even when not pressing keys.
     * <p>
     * This behavior is odd, and newer versions of Java do not seem to fix the
     * issue. Therefore, it could be a bug stemming from another factor, such as
     * development/environment setup or an operating system/software issue.
     * Regardless, this method should not hurt even in cases where the
     * aforementioned behavior does not occur. It simply provides safeguards.
     * 
     * @param input - the {@link MouseInput} to run through the filter.
     * @return a new {@link MouseInput} instance with identical data, <b>except</b>
     *         for the new filtered modifiers. Even if the filtration changes
     *         nothing about the input, a new instance will still be created and
     *         returned.
     */
    public MouseInput filterUnpressedKeyModifiers(MouseInput input) {
        // Filter out key modifiers which are NOT currently pressed. See method doc. as to why.
        int filteredMods = input.modifiers();
        if (!activeKeyInputs.contains(CTRL_KEY))
            filteredMods = filteredMods & ~InputEvent.CTRL_DOWN_MASK;
        if (!activeKeyInputs.contains(SHIFT_KEY))
            filteredMods = filteredMods & ~InputEvent.SHIFT_DOWN_MASK;
        if (!activeKeyInputs.contains(ALT_KEY))
            filteredMods = filteredMods & ~InputEvent.ALT_DOWN_MASK;
        if (!activeKeyInputs.contains(META_KEY))
            filteredMods = filteredMods & ~InputEvent.META_DOWN_MASK;
        if (!activeKeyInputs.contains(ALT_GRAPH_KEY))
            filteredMods = filteredMods & ~InputEvent.ALT_GRAPH_DOWN_MASK;
        return MouseInput.newWithModifiers(input, filteredMods);
    }
    
    /**
     * Registers a mouse input down under the passed {@link MouseInput}. If the
     * input is already down, nothing will happen and <code>null</code> will be
     * returned. Otherwise, the input will be marked as down until
     * {@link #mouseUp(MouseInput)} is called with the same input.
     * <p>
     * In normal cases, the returned {@link List} will have one or more
     * {@link BindDetails}, each representing a bind which matches the passed
     * input's code and modifiers values. However, the list can have more than one
     * bind. The controller tracks consecutive hits via {@link BindHitsTracker}
     * instances. The passed input may have been pressed multiple times
     * consecutively within its delay window. If it was, the passed input has its
     * hits integer set. Binds will only be added to the list if the input then
     * meets the hits requirement, or a multiple of it. After that logic, the list
     * is returned. Therefore, the list can only be non-<code>null</code> and empty
     * if the input matches at least one bind, but the hit requirement has not been
     * met for any of them.
     * <p>
     * If no binds match the passed input at all, this method will still return
     * <code>null</code>.
     * 
     * @param input - the {@link MouseInput} which was pressed.
     * @return a {@link List} of binds ({@link BindDetails}) which the passed input
     *         can activate.
     */
    public List<BindDetails<MouseInput>> mouseDown(MouseInput input) {
        // Mouse input was already down. Return null early.
        if (isMouseDown(input)) return null;
        
        // Add to active inputs map.
        activeMouseInputs.add(input);
        
        // Get all binds under input.
        final ConcurrentSkipListMap<Integer, BindDetails<MouseInput>> binds = this.mouseBinds.get(input);
        // No binds under the passed input. Return null early.
        if (binds == null) return null;
        // Set the hits using the tracker.
        input.setHits(mouseHitsTracker.hit(input));
        
        // Iterate over every bind, adding those which have a compatible hits requirement.
        final List<BindDetails<MouseInput>> shortcuts = new ArrayList<>(binds.size());
        binds.forEach((i, d) -> {
            if (hitsMultOfBind(d.options().hits(), input.hits()))
                shortcuts.add(d);
        });
        return shortcuts;
    }

    /**
     * Removes the registered mouse input under the passed {@link MouseInput}. If
     * the input was not down, nothing will happen and <code>null</code> will be
     * returned. Otherwise, the input will be unmarked as down.
     * <p>
     * In normal cases, the returned {@link List} will have one or more
     * {@link BindDetails}, each representing a bind which matches the passed
     * input's code and modifiers values. However, the list can have more than one
     * bind. The controller tracks consecutive hits via {@link BindHitsTracker}
     * instances. The passed input may have been pressed multiple times
     * consecutively within its delay window. If it was, the passed input has its
     * hits integer set. Binds will only be added to the list if the input then
     * meets the hits requirement, or a multiple of it. After that logic, the list
     * is returned. Therefore, the list can only be non-<code>null</code> and empty
     * if the input matches at least one bind, but the hit requirement has not been
     * met for any of them.
     * <p>
     * If no binds match the passed input at all, this method will still return
     * <code>null</code>.
     * 
     * @param input - the {@link MouseInput} which was pressed.
     * @return a {@link List} of binds ({@link BindDetails}) which the passed input
     *         can activate.
     */
    public List<BindDetails<MouseInput>> mouseUp(MouseInput input) {
        // Mouse button was already up. Return null early.
        if (!isMouseDownAny(input.code())) return null;
        
        // Remove every press instance, even if modifiers are different, since the button is now up.
        removeAllMousePressesOf(input.code());
        
        // Get all binds under input.
        final ConcurrentSkipListMap<Integer, BindDetails<MouseInput>> binds = mouseBinds.get(input);
        // No binds under the passed input. Return null early.
        if (binds == null) return null;
        // Set the hits using the tracker.
        input.setHits(mouseHitsTracker.hitUp(input));
        
        // Iterate over every bind, adding those which have a compatible hits requirement.
        final List<BindDetails<MouseInput>> shortcuts = new ArrayList<>(binds.size());
        binds.forEach((i, d) -> {
            if (hitsMultOfBind(d.options().hits(), input.hits()))
                shortcuts.add(d);
        });
        return shortcuts;
    }

    /**
     * Checks if the passed {@link MouseInput} is registered as down (pressed).
     * 
     * @param input - the {@link MouseInput} to check.
     * @return <code>true</code> if it is down; <code>false</code> otherwise.
     */
    public boolean isMouseDown(MouseInput input) {
        return activeMouseInputs.stream().anyMatch((i) -> i.matches(input));
    }

    /**
     * Checks if a mouse input is registered as down under the passed code integer,
     * disregarding any modifiers the input may have.
     * <p>
     * This method is not a true or fully accurate verification that a specific
     * input is down. Rather, it is only intended to check if one is pressed, in any
     * fashion (with or without modifiers). The check is only concerned about the
     * button that is down, not the entire input, which would include its modifiers
     * mask in the comparison.
     * <p>
     * For example, if you just want to see if {@link MouseEvent#BUTTON1} (LMB) is
     * down, and you do not care about any modifiers it may have been pressed with.
     * <p>
     * If a fully accurate check is necessary, consider using
     * {@link #isMouseDown(MouseInput)}.
     * 
     * @param code - an integer with the code to check for.
     * @return <code>true</code> if it is down; <code>false</code> otherwise.
     */
    public boolean isMouseDownAny(int code) {
        return activeMouseInputs.stream().anyMatch((m) -> (m.code() == code));
    }

    /**
     * Clears all of the active mouse inputs.
     * <p>
     * <b>Not to be confused with {@link #clearMouseBinds()}.</b> This method clears
     * active or down mouse inputs, not the internal map of registered mouse binds.
     * 
     * @return this BindController instance.
     */
    public BindController clearMouseInputs() {
        activeMouseInputs.clear();
        return this;
    }
    
    /**
     * Clears all of the active keyboard and mouse inputs.
     * <p>
     * <b>Not to be confused with {@link #clearBinds()}.</b> This method clears
     * active or down keyboard and mouse inputs, not the internal maps of registered
     * binds.
     * 
     * @return this BindController instance.
     */
    public BindController clearAllInputs() {
        clearKeyInputs();
        clearMouseInputs();
        return this;
    }
    
    /**
     * Clears all of the data from the keyboard and mouse trackers, resetting their
     * counts and canceling consecutive hit checks.
     * <p>
     * This method is especially useful in preventing inputs from one window
     * carrying over to another. For example, clicking on a window to
     * <code>PLAY/PAUSE</code>, then clicking on another to do the same, only for
     * the application to initiate <code>FULLSCREEN</code> because it tracked both
     * hits, even though the window focus switched.
     * 
     * @return this BindController instance.
     */
    public BindController clearTrackers() {
        this.keyHitsTracker.clear();
        this.mouseHitsTracker.clear();
        return this;
    }

    /**
     * Gets all of the default bindings from {@link Shortcut} and each of its
     * values. Each shortcut can have zero or more binds associated with it.
     * 
     * @return a {@link Map} with the default {@link Shortcut} and {@link Bind}
     *         values.
     */
    public static Map<Shortcut, Bind<?>[]> getDefaults() {
        final HashMap<Shortcut, Bind<?>[]> map = new HashMap<>();
        for (final Shortcut action : Shortcut.values()) {
            map.put(action, action.binds());
        }
        return map;
    }
    
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder("-------------------------------\nBindController\n\nActive Keys: \n");
        this.activeKeyInputs.forEach(k -> builder.append("> ").append(k.toString()).append("\n"));
        builder.append("Active Mouse Buttons: \n");
        this.activeMouseInputs.forEach(m -> builder.append("> ").append(m.toString()).append("\n"));
        return builder.toString();
    }
}
