package dev.mwhitney.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Timer;
import javax.swing.border.LineBorder;

import dev.mwhitney.listeners.PaintRequester;

/**
 * A simple extension upon the {@link LineBorder} class with the capability to
 * fade its Color into transparency upon request.
 * 
 * @author mwhitney57
 */
public class FadingLineBorder extends LineBorder implements PaintRequester {
    /** The randomly-generated serial UID for the FadingLineBorder class. */
    private static final long serialVersionUID = -960971844424714747L;

    /** The repeating Timer used for the border fading process which stops once the border has faded. */
    private Timer fadeTimer;
    /** The Color when the border is completely normal and not faded. */
    private Color colorNormal;
    
    /**
     * Creates a new FadingLineBorder, which is a simple extension upon
     * {@link LineBorder} with the capability to fade its Color into transparency
     * upon request. This constructor automatically enables the rounding of the
     * border. Additionally, it enables antialiasing when drawing the border for a
     * cleaner look.
     * 
     * @param color     - the initial/normal Color of the border when it is fully
     *                  visible and fading has not begun.
     * @param thickness - an int with the thickness of the border in pixels.
     */
    public FadingLineBorder(Color color, int thickness) {
        super(color, thickness, true);
        
        // Save normal color and its faded counterpart, then set color to faded.
        this.colorNormal = color;
        this.lineColor   = fadedOf(color);
        
        fadeTimer = new Timer(10, (e) -> {
            // Apply Default Color if null
            if (this.lineColor == null)
                this.lineColor = new Color(255, 0, 0, 200);
            // Stop Timer if Alpha has Reached 0 (Faded)
            if (this.lineColor.getAlpha() == 0) {
                ((Timer) e.getSource()).stop();
                return;
            }
            
            // Adjust the border line color's transparency value and request a repaint.
            final int r = lineColor.getRed(), g = lineColor.getGreen(), b = lineColor.getBlue(), a = lineColor.getAlpha();
            this.lineColor = new Color(r, g, b, Math.max(0, a - 2));
            requestPaint();
        });
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
        // Only paint the border when not faded.
        if (!isFaded()) {
            // Enable anti-aliasing, paint the rounded border, then disable anti-aliasing.
            // If not disabled, the anti-aliasing will persist to other components and cause a massive hit to performance.
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            super.paintBorder(c, g, x, y, width, height);
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }
    }
    
    /**
     * Checks if this FadingLineBorder is faded, meaning it is fully transparent.
     * 
     * @return <code>true</code> if the border is fully transparent;
     *         <code>false</code> otherwise.
     */
    public boolean isFaded() {
        return (lineColor.getAlpha() == 0);
    }
    
    /**
     * Fades this FadingLineBorder, starting the process from its initial base color
     * and fading it over time. The fading process is complete and stops once the
     * border is fully transparent.
     */
    public void fade() {
        fade(colorNormal);
    }
    
    /**
     * Fades this FadingLineBorder, starting the process from the passed Color and
     * fading it over time. The fading process is complete and stops once the border
     * is fully transparent. Since the fading process is constant over time, passing
     * a more or less transparent Color will impact the amount of time it takes to
     * fade.
     * 
     * @param c - the base Color to start with before starting the fade.
     */
    public void fade(final Color c) {
        if (c != null) {
            this.lineColor = c;
        } else
            this.lineColor = colorNormal;
        fadeTimer.start();
    }
    
    /**
     * Calculates and returns a faded (transparent) version of the passed Color.
     * 
     * @param c - the Color to get a faded version of.
     * @return the passed Color, but faded (transparent.)
     */
    private Color fadedOf(final Color c) {
        return (new Color(c.getRed(), c.getGreen(), c.getBlue(), 0));
    }
    
    /**
     * Cancels any ongoing fading process. This method will then automatically fade
     * the border, restoring its transparency.
     */
    public void cancelFade() {
        fadeTimer.stop();
        
        this.lineColor = fadedOf(colorNormal);
    }
    
    @Override
    public void requestPaint() {}
}
