package dev.mwhitney.gui.decor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.util.Objects;

import javax.swing.Timer;
import javax.swing.border.LineBorder;

import dev.mwhitney.gui.interfaces.AdaptiveOpacity;
import dev.mwhitney.listeners.PaintRequester;
import dev.mwhitney.resources.AppRes;
import dev.mwhitney.util.PiPAAUtils;

/**
 * A simple extension upon the {@link LineBorder} class with the capability to
 * fade its {@link Color} into transparency upon request.
 * 
 * @author mwhitney57
 */
public class FadingLineBorder extends LineBorder implements PaintRequester, AdaptiveOpacity {
    /** The randomly-generated serial UID. */
    private static final long serialVersionUID = -960971844424714747L;

    /** The default {@link Color} for the line border. */
    private static final Color COLOR_DEFAULT = new Color(255, 0, 0, 200);
    
    /** The repeating {@link javax.swing.Timer} used for the border fading process which stops once the border has faded. */
    private final Timer fadeTimer = new Timer(10, this::handleFadeTimerEvent);
    /** The {@link Color} when the border is completely normal and not faded. */
    private final Color colorNormal;
    /**
     * A boolean which indicates whether or not the faded color should be fully
     * transparent. If the user needs to be able to interact with the border, it
     * should be technically visible, with an alpha value greater than zero.
     */
    private boolean fullTransparency = false;
    /** The opacity of the window in which this border is displayed. */
    private float windowOpacity = 1.0f;
    
    /**
     * Creates a new {@link FadingLineBorder} which is a simple extension upon
     * {@link LineBorder} with the capability to fade its Color into transparency
     * upon request.
     * <p>
     * This border has rounded corners by default. It also paints using
     * anti-aliasing for a cleaner look.
     * 
     * @param color     - the initial/normal {@link Color} of the border when it is
     *                  fully visible and fading has not begun.
     * @param thickness - an int with the thickness of the border in pixels.
     */
    public FadingLineBorder(Color color, int thickness) {
        super(color, thickness, true);
        
        // Save normal color and its faded counterpart, then set color to faded.
        this.colorNormal = Objects.requireNonNullElse(color, COLOR_DEFAULT);
        this.lineColor   = fadedColor();
    }
    
    /**
     * Enables or disables the usage of full transparency when the border is faded.
     * <p>
     * If the user needs to be able to interact with the border, then full
     * transparency should be disabled. Full transparency with an alpha value of
     * zero could lead to input being ignored by the window and handled by whatever
     * is displaying beneath it.
     * 
     * @param t - a boolean for whether or not full transparency should be used.
     * @return this {@link FadingLineBorder} instance.
     * @since 0.9.5
     */
    public FadingLineBorder useFullTransparency(boolean t) {
        // Check if border was faded prior to changing mode.
        final boolean faded = isFaded();
        
        this.fullTransparency = t;
        
        // Refresh the line color to be faded to the right amount.
        if (faded) refreshFadedLineColor();
        
        return this;
    }
    
    /*
     * Important Graphics Painting Note
     * 
     * When alt-tabbing out of a fullscreen application and then resizing a PiPWindow,
     * all other PiPWindows' borders would become white. Potential issues were investigated,
     * and DirectDraw was ultimately found to be a potential cause, so it was disabled.
     * Reference "jvm-arguments.txt" for more info and resources.
     * 
     * If disabling it causes issues, look into alternatives.
     * A potential alternative to look into would be:
     * Keep track if ANY window has focus. If none do, then one
     * gains focus, repaint all windows to ensure they are displaying
     * properly.
     */
    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        // Paint the rounded border using anti-aliasing. Use then safely dispose of Graphics copy.
        final Graphics2D g2d = (Graphics2D) g.create();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            super.paintBorder(c, g2d, x, y, width, height);
        } finally {
            g2d.dispose();
        }
    }
    
    /**
     * Handles an event firing from the Swing {@link #fadeTimer}. Each call of this
     * method works to fade the border further, until its alpha value is
     * sufficiently low. At that point, the border is fully faded and transparent,
     * so the timer will be stopped.
     * 
     * @param e - the {@link ActionEvent} supplied by the timer.
     */
    private void handleFadeTimerEvent(ActionEvent e) {
        // Apply Default Color if null
        if (this.lineColor == null)
            this.lineColor = COLOR_DEFAULT;
        // Stop Timer if Alpha has Reached Faded Value
        if (isFaded()) {
            fadeTimer.stop();
            refreshFadedLineColor();
            return;
        }
        
        // Adjust the border line color's transparency value and request a repaint.
        final int r = lineColor.getRed(), g = lineColor.getGreen(), b = lineColor.getBlue(), a = lineColor.getAlpha();
        this.lineColor = new Color(r, g, b, Math.max(fullTransparency ? 0 : PiPAAUtils.getMinimumInteractableAlpha(windowOpacity), a - 2));
        requestPaint();
    }
    
    /**
     * Checks if this {@link FadingLineBorder} is faded, meaning its alpha value is
     * either fully transparent or near-transparent, depending on the value of
     * {@link fullTransparency}.
     * 
     * @return <code>true</code> if the border is completely faded;
     *         <code>false</code> otherwise.
     */
    public boolean isFaded() {
        return this.lineColor.getAlpha() <= (fullTransparency ? 0 : PiPAAUtils.getMinimumInteractableAlpha(windowOpacity));
    }
    
    /**
     * Fades this {@link FadingLineBorder}, starting the process from its initial
     * base color and fading it over time. The fading process is complete and stops
     * once the border is fully transparent.
     */
    public void fade() {
        fade(null);
    }
    
    /**
     * Fades this {@link FadingLineBorder}, starting the process from the passed
     * Color and fading it over time. The fading process is complete and stops once
     * the border is fully transparent.
     * <p>
     * Since the fading process is constant over time, passing a more or less
     * transparent {@link Color} will impact the amount of time it takes to fade.
     * 
     * @param c - the base {@link Color} to start with before starting the fade.
     */
    public void fade(final Color c) {
        this.lineColor = (c != null ? c : colorNormal);
        fadeTimer.start();
    }
    
    /**
     * Gets a faded {@link Color}. If full transparency mode is active, the method
     * will simply return {@link AppRes#COLOR_TRANSPARENT}. Otherwise, a color will
     * be retrieved using {@link PiPAAUtils#getMinimumInteractableColor(float)}.
     * <p>
     * In that case, the faded {@link Color} is intended to be imperceptible, but
     * not invisible. The border is actually much more visible with higher RGB
     * values. Therefore, a full black is used with the lowest interactable alpha
     * value given the current {@link #windowOpacity}.
     * 
     * @return a fully faded {@link Color}.
     */
    private Color fadedColor() {
        return fullTransparency ? AppRes.COLOR_TRANSPARENT : PiPAAUtils.getMinimumInteractableColor(windowOpacity);
    }
    
    /**
     * Cancels any ongoing fading process. This method then automatically sets the
     * border to a faded state, restoring its transparency.
     */
    public void cancelFade() {
        fadeTimer.stop();
        
        refreshFadedLineColor();
    }
    
    /**
     * Updates the line color to a faded state and immediately requests a paint.
     * 
     * @since 0.9.5
     */
    private void refreshFadedLineColor() {
        this.lineColor = fadedColor();
        PiPAAUtils.invokeNowOrLater(this::requestPaint);
    }
    
    @Override
    public void requestPaint() {}

    @Override
    public void adaptToOpacity(float opacity) {
        // Check if border was faded prior to changing window opacity.
        final boolean faded = isFaded();
        
        this.windowOpacity = Math.clamp(opacity, 0.0f, 1.0f);
        
        // Refresh the line color to be faded to the right amount.
        if (faded) refreshFadedLineColor();
    }
}
