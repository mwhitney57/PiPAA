package dev.mwhitney.gui.binds;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * A listener or hook that acts as a middle-man between a component and the usual
 * keyboard and mouse listeners. This allows it to indicate when an input
 * matches a {@link Bind} configured for a {@link Shortcut}.
 * <p>
 * Every BindHook implements functionality from {@link KeyListener},
 * {@link MouseListener}, {@link MouseWheelListener}, and
 * {@link MouseMotionListener}. This allows it to fill in as any of those
 * listeners, capturing, converting, and then comparing events against the
 * application's {@link Bind} configuration for each {@link Shortcut}.
 * <p>
 * The user loses very little by using this class instead of a regular listener.
 * It offers multiple methods which mimic the usual listener behavior and are
 * called after the bind logic executes. For example, if you wanted to use
 * {@link #keyPressed(KeyEvent)}, you would instead override
 * {@link #onKey(KeyInput)}.
 * <p>
 * A few methods from {@link MouseMotionListener} are simply passed on and can
 * be overridden as usual, though it's not required.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public abstract class BindHook implements BindedKeyListener, BindedMouseListener, MouseWheelListener, MouseMotionListener {
    /** The {@link BindController} instance used for bind checks and validation. */
    private final BindController controller;
    
    /**
     * Creates a new {@link BindHook}.
     * <p>
     * See class documentation for more information, including the purpose of this
     * class.
     * 
     * @param controller - the {@link BindController} instance required for checking
     *                   the current bind configuration and tracking keyboard and
     *                   mouse inputs.
     */
    public BindHook(BindController controller) {
        this.controller = Objects.requireNonNull(controller, "The BindController connected to a BindHook must not be null.");
    }
    
    /**
     * Gets the {@link BindController} instance that was passed to this
     * {@link BindHook} during its construction.
     * 
     * @return the {@link BindController} instance.
     */
    public BindController controller() {
        return this.controller;
    }
    
    // Essential built-in methods. Marked as final to prevent overriding.
    @Override
    public final void keyPressed(KeyEvent e)      {
        // If key code matches a modifier, use zero (no modifiers), otherwise combine event modifiers with active custom key modifiers.
        final int mods = this.controller.isModifier(e.getKeyCode()) ? 0 : e.getModifiersEx() | this.controller.getCustomKeyModifiers();
        // Create KeyInput from KeyEvent and tell controller about the press.
        final KeyInput input = new KeyInput(e.getKeyCode(), mods);
        final List<BindDetails<KeyInput>> details = this.controller.keyDown(input);
        // At least one shortcut was found under that input.
        if (details != null && details.size() > 0) {
            details.forEach(shortcut -> {
                if (shortcut.options().isOnPress()) onKeyBind(shortcut);
            });
        }
        // Call regardless of success in finding a matching Bind and Shortcut.
        onKey(input);
    }
    @Override
    public final void keyReleased(KeyEvent e)     {
        // If key code matches a modifier, use zero (no modifiers), otherwise combine event modifiers with active custom key modifiers.
        final int mods = this.controller.isModifier(e.getKeyCode()) ? 0 : e.getModifiersEx() | this.controller.getCustomKeyModifiers();
        // Create KeyInput from KeyEvent and tell controller about the release.
        final KeyInput input = new KeyInput(e.getKeyCode(), mods);
        final List<BindDetails<KeyInput>> details = this.controller.keyUp(input);
        // At least one shortcut was found under that input.
        if (details != null && details.size() > 0) {
            details.forEach(shortcut -> {
                if (shortcut.options().isOnRelease()) onKeyBindUp(shortcut);
            });
        }
        // Call regardless of success in finding a matching Bind and Shortcut.
        onKeyUp(input);
    }
    @Override
    public final void mousePressed(MouseEvent e)  {
        // Create MouseInput from MouseEvent. Filter weird modifiers behavior, then include any active custom modifiers.
        final MouseInput input = MouseInput.newWithCombinedModifiers(
                this.controller.filterUnpressedKeyModifiers(MouseInput.fromEvent(e)),
                this.controller.getCustomModifiers());
        final List<BindDetails<MouseInput>> details = this.controller.mouseDown(input);
        // At least one shortcut was found under that input.
        if (details != null && details.size() > 0) {
            details.forEach(shortcut -> {
                if (shortcut.options().isOnPress()) onMouseBind(shortcut);
            });
        }
        // Call regardless of success in finding a matching Bind and Shortcut.
        onMouse(input);
    }
    @Override
    public final void mouseReleased(MouseEvent e) {
        // Create MouseInput from MouseEvent. Filter weird modifiers behavior, then include any active custom modifiers.
        final MouseInput input = MouseInput.newWithCombinedModifiers(
                this.controller.filterUnpressedKeyModifiers(MouseInput.fromEvent(e)),
                this.controller.getCustomModifiers());
        final List<BindDetails<MouseInput>> details = this.controller.mouseUp(input);
        // At least one shortcut was found under that input.
        if (details != null && details.size() > 0) {
            details.forEach(shortcut -> {
                if (shortcut.options().isOnRelease()) onMouseBindUp(shortcut);
            });
        }
        // Call regardless of success in finding a matching Bind and Shortcut.
        onMouseUp(input);
    }
    @Override
    public final void mouseWheelMoved(MouseWheelEvent e) {
        // Create MouseInput from MouseEvent, including active custom modifiers.
        final MouseInput input = MouseInput.newWithCombinedModifiers(MouseInput.fromEvent(e), this.controller.getCustomModifiers());
        // Check for matching bind. Wheel movement is one-off and not tracked by the controller.
        final ConcurrentSkipListMap<Integer, BindDetails<MouseInput>> binds = this.controller.getMouseBind(input);
        final BindDetails<MouseInput> details = (binds != null ? binds.get(input.hits()) : null);
        // Scroll bind match found.
        if (details != null) onScrollBind(new BindDetails<MouseInput>(details.shortcut(), details.input(), details.options()));
        // Call regardless of success in finding a matching Bind and Shortcut.
        onScroll(input);
    }
    
    // No Default Implementation -- User overrides to receive event, bind, and/or input data.
    @Override
    public void mouseDragged(MouseEvent e) {}
    @Override
    public void mouseMoved(MouseEvent e) {}
    // Methods Not Declared Abstract -- User can pick which methods are needed for each hook.
    /**
     * Fired when a bind has been activated from one or more keys being
     * <b>pressed</b>.
     * <p>
     * This method should only fire when a bind is found that matches key inputs
     * <b>and</b> that bind activates on key presses, not releases.
     * 
     * @param bind - a {@link BindDetails} instance relating to the bind.
     * @see {@link #onKeyBindUp(BindDetails)} for receiving bind activations on key
     *      <b>release</b>.
     */
    public void onKeyBind(BindDetails<KeyInput> bind) {}
    /**
     * Fired when a bind has been activated from one or more keys being
     * <b>released</b>.
     * <p>
     * This method should only fire when a bind is found that matches key inputs
     * <b>and</b> that bind activates on key releases, not presses.
     * 
     * @param bind - a {@link BindDetails} instance relating to the bind.
     * @see {@link #onKeyBind(BindDetails)} for receiving bind activations on key
     *      <b>press</b>.
     */
    public void onKeyBindUp(BindDetails<KeyInput> bind) {}
    /**
     * Fired when a bind has been activated from one or more mouse buttons being
     * <b>pressed</b>.
     * <p>
     * This method should only fire when a bind is found that matches mouse inputs
     * <b>and</b> that bind activates on mouse button presses, not releases.
     * 
     * @param bind - a {@link BindDetails} instance relating to the bind.
     * @see {@link #onMouseBindUp(BindDetails)} for receiving bind activations on
     *      mouse <b>release</b>.
     */
    public void onMouseBind(BindDetails<MouseInput> bind) {}
    /**
     * Fired when a bind has been activated from one or more mouse buttons being
     * <b>released</b>.
     * <p>
     * This method should only fire when a bind is found that matches mouse inputs
     * <b>and</b> that bind activates on mouse button releases, not presses.
     * 
     * @param bind - a {@link BindDetails} instance relating to the bind.
     * @see {@link #onMouseBind(BindDetails)} for receiving bind activations on mouse
     *      <b>press</b>.
     */
    public void onMouseBindUp(BindDetails<MouseInput> bind) {}
    /**
     * Fired when a bind has been activated from the mouse wheel being scrolled.
     * 
     * @param bind - a {@link BindDetails} instance relating to the bind.
     */
    public void onScrollBind(BindDetails<MouseInput> bind) {}
    /**
     * Fired when a key is <b>pressed</b>. A simple extension of
     * {@link KeyListener#keyPressed(KeyEvent)} which is called after performing
     * bind check logic. Called regardless of whether or not a bind was found which
     * matched the input.
     * 
     * @param input - a {@link KeyInput} with the input information.
     */
    public void onKey(KeyInput input) {}
    /**
     * Fired when a key is <b>released</b>. A simple extension of
     * {@link KeyListener#keyReleased(KeyEvent)} which is called after performing
     * bind check logic. Called regardless of whether or not a bind was found which
     * matched the input.
     * 
     * @param input - a {@link KeyInput} with the input information.
     */
    public void onKeyUp(KeyInput input) {}
    /**
     * Fired when a mouse button is <b>pressed</b>. A simple extension of
     * {@link MouseListener#mousePressed(MouseEvent)} which is called after
     * performing bind check logic. Called regardless of whether or not a bind was
     * found which matched the input.
     * 
     * @param input - a {@link MouseInput} with the input information.
     */
    public void onMouse(MouseInput input) {}
    /**
     * Fired when a mouse button is <b>released</b>. A simple extension of
     * {@link MouseListener#mouseReleased(MouseEvent)} which is called after
     * performing bind check logic. Called regardless of whether or not a bind was
     * found which matched the input.
     * 
     * @param input - a {@link MouseInput} with the input information.
     */
    public void onMouseUp(MouseInput input) {}
    /**
     * Fired when a mouse wheel is <b>scrolled</b>. A simple extension of
     * {@link MouseWheelListener#mouseWheelMoved(MouseWheelEvent)} which is called
     * after performing bind check logic. Called regardless of whether or not a bind
     * was found which matched the input.
     * 
     * @param input - a {@link MouseInput} with the input information.
     */
    public void onScroll(MouseInput input) {}
}
