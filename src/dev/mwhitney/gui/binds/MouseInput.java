package dev.mwhitney.gui.binds;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Objects;

/**
 * An extension of {@link BindInput} specifically for mouse inputs. Optionally
 * stores additional information such as scrolling, local and on-screen x and y
 * coordinates of the input.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public final class MouseInput extends BindInput {
    /** The LMB, or Left Mouse Button. Equal to {@link MouseEvent#BUTTON1}. */
    public static final int LMB = MouseEvent.BUTTON1;
    /** The MMB, or Middle Mouse Button (Scroll Wheel Press). Equal to {@link MouseEvent#BUTTON2}. */
    public static final int MMB = MouseEvent.BUTTON2;
    /** The RMB, or Right Mouse Button. Equal to {@link MouseEvent#BUTTON3}. */
    public static final int RMB = MouseEvent.BUTTON3;
    
    /** An int representing inputs which do <b>not</b> use the mouse wheel (scrolling). */
    public static final int NO_WHEEL = 0;
    
    /** An int for the scroll amount and direction, if used. */
    private final int wheelRotation;
    /** Local coordinates for where the input originated within a window. */
    private final int x, y;
    /** On-screen coordinates for where the input originated within the display. */
    private final int onScreenX, onScreenY;
    
    /**
     * Creates a new mouse input with the passed code, modifiers, and hits (clicks)
     * integers.
     * <p>
     * Using this constructor indicates that the mouse input did not use the mouse
     * wheel, nor is the origin location needed. If you need to use that data,
     * consider using {@link #MouseInput(int, int, int, int, int, int, int, int)},
     * {@link #fromEvent(MouseEvent)}, or {@link #fromEvent(MouseWheelEvent)}.
     * 
     * @param button    - the input code.
     * @param modifiers - the input modifiers mask.
     * @param clicks    - the number of consecutive hits of the input.
     */
    public MouseInput(int code, int modifiers, int clicks) {
        this(code, modifiers, clicks, NO_WHEEL, -1, -1, -1 ,-1);
    }
    
    /**
     * Creates a new mouse input with the passed integer information.
     * <p>
     * For a simpler alternative to this constructor, consider using
     * {@link #fromEvent(MouseEvent)} or {@link #fromEvent(MouseWheelEvent)} if
     * possible.
     * 
     * @param code          - the input code.
     * @param modifiers     - the input modifiers mask.
     * @param clicks        - the number of consecutive hits of the input.
     * @param wheelRotation - the mouse wheel (scroll wheel) rotation, which can be
     *                      positive or negative depending on direction.
     * @param x             - the local x-coordinate where the input originated.
     * @param y             - the local y-coordinate where the input originated.
     * @param xScr          - the on-screen x-coordinate where the input originated.
     * @param yScr          - the on-screen y-coordinate where the input originated.
     */
    private MouseInput(int code, int modifiers, int clicks, int wheelRotation, int x, int y, int xScr, int yScr) {
        // Does not clean mask of key modifiers, since they may be used alongside mouse inputs.
        super(code, modifiers, clicks);
        this.wheelRotation = wheelRotation;
        this.x = x;
        this.y = y;
        this.onScreenX = xScr;
        this.onScreenY = yScr;
    }
    
    /**
     * Creates a new input instance with a different modifiers mask.
     * 
     * @param input     - the {@link MouseInput} instance to base the new instance
     *                  on, keeping everything except for the modifiers mask.
     * @param modifiers - an int with the new modifiers mask.
     * @return the new input instance.
     */
    public static MouseInput newWithModifiers(MouseInput input, int modifiers) {
        Objects.requireNonNull(input, "Cannot create new MouseInput instance with modifiers using null.");
        return new MouseInput(input.code(), modifiers, input.hits(), input.getWheelRotation(), input.getX(), input.getY(), input.getOnScreenX(), input.getOnScreenY());
    }
    
    /**
     * Creates a new {@link MouseInput} instance using data from the passed
     * {@link MouseEvent}.
     * 
     * @param e - the {@link MouseEvent} containing the input data.
     * @return the new {@link MouseInput} instance.
     * @throws NullPointerException if the passed {@link MouseEvent} is
     *                              <code>null</code>.
     * @see {@link #fromEvent(MouseWheelEvent)} for creating an input instance which
     *      includes mouse wheel data.
     */
    public static MouseInput fromEvent(MouseEvent e) {
        Objects.requireNonNull(e, "Cannot create new MouseInput instance from null MouseEvent.");
        return new MouseInput(e.getButton(), e.getModifiersEx(), e.getClickCount(), 0, e.getX(), e.getY(), e.getXOnScreen(), e.getYOnScreen());
    }

    /**
     * Creates a new {@link MouseInput} instance using data from the passed
     * {@link MouseWheelEvent}.
     * 
     * @param e - the {@link MouseWheelEvent} containing the input data.
     * @return the new {@link MouseInput} instance.
     * @throws NullPointerException if the passed {@link MouseWheelEvent} is
     *                              <code>null</code>.
     * @see {@link #fromEvent(MouseEvent)} for creating an input instance without
     *      mouse wheel data.
     */
    public static MouseInput fromEvent(MouseWheelEvent e) {
        Objects.requireNonNull(e, "Cannot create new MouseInput instance from null MouseWheelEvent.");
        // Apply custom codes depending on scroll direction to differentiate input.
        final int eventCode = (e.getButton() != MouseEvent.NOBUTTON ? e.getButton() :
            switch (Integer.compare(0, e.getWheelRotation())) {
            case  1 -> ShortcutCode.CODE_SCROLL_UP;
            case -1 -> ShortcutCode.CODE_SCROLL_DOWN;
            default -> ShortcutCode.CODE_SCROLL;
        });
        return new MouseInput(eventCode, e.getModifiersEx(), 1, e.getWheelRotation(), e.getX(), e.getY(), e.getXOnScreen(), e.getYOnScreen());
    }

    /**
     * A simple method which is checks if <code>hits == 2</code>.
     * 
     * @return <code>true</code> if this input is a double-click; <code>false</code>
     *         otherwise.
     */
    public boolean isDoubleClick() {
        return this.hits == 2;
    }
    
    /**
     * A simple method which is checks if <code>hits == 3</code>.
     * 
     * @return <code>true</code> if this input is a triple-click; <code>false</code>
     *         otherwise.
     */
    public boolean isTripleClick() {
        return this.hits == 3;
    }
    
    /**
     * Gets the wheel rotation, which can be positive or negative depending on the
     * scroll direction.
     * 
     * @return an int with the wheel rotation.
     */
    public int getWheelRotation() {
        return this.wheelRotation;
    }
    
    /**
     * Checks if this input likely originates from a {@link MouseWheelEvent}, given
     * its data.
     * 
     * @return <code>true</code> if this input contains mouse wheel data;
     *         <code>false</code> otherwise.
     */
    public boolean isWheelEvent() {
        return this.wheelRotation != 0;
    }
    
    /**
     * Gets the local x-coordinate where this input originated.
     * 
     * @return an int with the x-coordinate.
     * @see {@link #getOnScreenX()} for retrieving the on-screen x-coordinate.
     */
    public int getX() {
        return this.x;
    }
    
    /**
     * Gets the local y-coordinate where this input originated.
     * 
     * @return an int with the y-coordinate.
     * @see {@link #getOnScreenY()} for retrieving the on-screen y-coordinate.
     */
    public int getY() {
        return this.y;
    }
    
    /**
     * Gets the on-screen x-coordinate where this input originated.
     * 
     * @return an int with the x-coordinate.
     * @see {@link #getX()} for retrieving the local x-coordinate.
     */
    public int getOnScreenX() {
        return this.onScreenX;
    }
    
    /**
     * Gets the on-screen y-coordinate where this input originated.
     * 
     * @return an int with the y-coordinate.
     * @see {@link #getY()} for retrieving the local y-coordinate.
     */
    public int getOnScreenY() {
        return this.onScreenY;
    }
    
    @Override
    public boolean equals(Object o) {
        // If same object in memory, quick return true.
        if (this == o) return true;
        // Ensure types are consistent, then cast and compare data if clear.
        if (o != null && o instanceof MouseInput input) return matches(input);
        // Not equal. Passed object is null or type mismatch.
        return false;
    }

    @Override
    public String toString() {
        return String.format("MouseInput == Code:%s | Modifiers:%s | Hits:%s | Wheel Rotation:%s | X:%s | Y:%s | ScrX:%s | ScrY:%s",
                this.code, this.modifiers, this.hits, this.wheelRotation, this.x, this.y, this.onScreenX, this.onScreenY);
    }
}
