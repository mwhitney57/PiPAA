package dev.mwhitney.gui.binds;

import java.awt.Point;

/**
 * HSMBTracker stands for High-Speed Mouse Button Tracker. A simple and quick
 * class for keeping track of mouse button presses, as well as the {@link Point}
 * at which each was last pressed.
 * <p>
 * To easily ensure thread safety, only call class methods from the
 * event-dispatch thread (EDT).
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public class HSMBTracker {
    /** A simple boolean to track whether the button is currently pressed. */
    private boolean lmb = false, mmb = false, rmb = false;
    /** The {@link Point} at which the button was pressed. <code>null</code> if the button is not currently pressed. */
    private Point lmbPoint, mmbPoint, rmbPoint;
    
    /**
     * Turns on the left mouse button (LMB), indicating it is currently pressed.
     */
    public void onLMB()  { this.lmb = true; }
    /**
     * Turns on the middle mouse button (MMB), indicating it is currently pressed.
     */
    public void onMMB()  { this.mmb = true; }
    /**
     * Turns on the right mouse button (RMB), indicating it is currently pressed.
     */
    public void onRMB()  { this.rmb = true; }
    
    
    /**
     * Turns off the left mouse button (LMB), indicating it is no longer pressed.
     */
    public void offLMB() { this.lmb = false; noPointLMB(); }
    /**
     * Turns off the middle mouse button (MMB), indicating it is no longer pressed.
     */
    public void offMMB() { this.mmb = false; noPointMMB(); }
    /**
     * Turns off the right mouse button (RMB), indicating it is no longer pressed.
     */
    public void offRMB() { this.rmb = false; noPointRMB(); }
    
    
    /**
     * Is the left mouse button (LMB) currently pressed?
     * 
     * @return <code>true</code> if it is pressed; <code>false</code> otherwise.
     */
    public boolean isLMB() { return this.lmb; }
    /**
     * Is the middle mouse button (MMB) currently pressed?
     * 
     * @return <code>true</code> if it is pressed; <code>false</code> otherwise.
     */
    public boolean isMMB() { return this.mmb; }
    /**
     * Is the right mouse button (RMB) currently pressed?
     * 
     * @return <code>true</code> if it is pressed; <code>false</code> otherwise.
     */
    public boolean isRMB() { return this.rmb; }
    
    
    /**
     * Does the left mouse button (LMB) currently have a {@link Point} saved? It is
     * possible for a {@link Point} to not be saved during a button press by using
     * {@link #onLMB()} instead of {@link #usePointLMB(Point)}.
     * 
     * @return <code>true</code> if a {@link Point} exists; <code>false</code>
     *         otherwise.
     */
    public boolean hasPointLMB() { return this.lmbPoint != null; }
    /**
     * Does the middle mouse button (MMB) currently have a {@link Point} saved? It is
     * possible for a {@link Point} to not be saved during a button press by using
     * {@link #onMMB()} instead of {@link #usePointMMB(Point)}.
     * 
     * @return <code>true</code> if a {@link Point} exists; <code>false</code>
     *         otherwise.
     */
    public boolean hasPointMMB() { return this.mmbPoint != null; }
    /**
     * Does the right mouse button (RMB) currently have a {@link Point} saved? It is
     * possible for a {@link Point} to not be saved during a button press by using
     * {@link #onRMB()} instead of {@link #usePointRMB(Point)}.
     * 
     * @return <code>true</code> if a {@link Point} exists; <code>false</code>
     *         otherwise.
     */
    public boolean hasPointRMB() { return this.rmbPoint != null; }
    
    
    /**
     * Sets the {@link Point} where the last left mouse button (LMB) press occurred.
     * <p>
     * Using this method is generally not necessary. Consider using
     * {@link #usePointLMB(Point)} instead.
     * 
     * @param p - the {@link Point} where the button press occurred.
     */
    public void setPointLMB(Point p) { this.lmbPoint = p; }
    /**
     * Sets the {@link Point} where the last middle mouse button (MMB) press occurred.
     * <p>
     * Using this method is generally not necessary. Consider using
     * {@link #usePointMMB(Point)} instead.
     * 
     * @param p - the {@link Point} where the button press occurred.
     */
    public void setPointMMB(Point p) { this.mmbPoint = p; }
    /**
     * Sets the {@link Point} where the last right mouse button (RMB) press occurred.
     * <p>
     * Using this method is generally not necessary. Consider using
     * {@link #usePointRMB(Point)} instead.
     * 
     * @param p - the {@link Point} where the button press occurred.
     */
    public void setPointRMB(Point p) { this.rmbPoint = p; }
    
    
    /**
     * Shorthand for calling {@link #onLMB()} and {@link #setPointLMB(Point)} in
     * succession.
     * 
     * @param p - the {@link Point} where the button press occurred.
     */
    public void usePointLMB(Point p) {
        onLMB();
        setPointLMB(p);
    }
    /**
     * Shorthand for calling {@link #onMMB()} and {@link #setPointMMB(Point)} in
     * succession.
     * 
     * @param p - the {@link Point} where the button press occurred.
     */
    public void usePointMMB(Point p) {
        onMMB();
        setPointMMB(p);
    }
    /**
     * Shorthand for calling {@link #onRMB()} and {@link #setPointRMB(Point)} in
     * succession.
     * 
     * @param p - the {@link Point} where the button press occurred.
     */
    public void usePointRMB(Point p) {
        onRMB();
        setPointRMB(p);
    }
    
    
    /**
     * Nullifies the {@link Point} at which the left mouse button (LMB) was pressed.
     */
    public void noPointLMB() { this.lmbPoint = null; }
    /**
     * Nullifies the {@link Point} at which the middle mouse button (MMB) was pressed.
     */
    public void noPointMMB() { this.mmbPoint = null; }
    /**
     * Nullifies the {@link Point} at which the right mouse button (RMB) was pressed.
     */
    public void noPointRMB() { this.rmbPoint = null; }
    
    
    /**
     * Gets the {@link Point} at which the left mouse button (LMB) was pressed.
     * 
     * @return the {@link Point} where the press occurred.
     */
    public Point getPointLMB() { return this.lmbPoint; }
    /**
     * Gets the {@link Point} at which the middle mouse button (MMB) was pressed.
     * 
     * @return the {@link Point} where the press occurred.
     */
    public Point getPointMMB() { return this.mmbPoint; }
    /**
     * Gets the {@link Point} at which the right mouse button (RMB) was pressed.
     * 
     * @return the {@link Point} where the press occurred.
     */
    public Point getPointRMB() { return this.rmbPoint; }
}
