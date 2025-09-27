package dev.mwhitney.gui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;

import dev.mwhitney.gui.interfaces.ThemedComponent;
import dev.mwhitney.main.PiPProperty.PropDefault;
import dev.mwhitney.main.PiPProperty.THEME_OPTION;
import dev.mwhitney.main.PiPProperty.THEME_OPTION.COLOR;
import dev.mwhitney.resources.AppRes;
import dev.mwhitney.util.Loop;

/**
 * A button which displays similar to a number wheel or dial. The button
 * internally tracks a {@link Loop} of digits <code>0-9</code>. The selected
 * digit is displayed by the button, with its nearest neighbors displayed above
 * and below it in the wheel.
 * <p>
 * The wheel can be scrolled through via {@link #next()} and
 * {@link #previous()}, or the value can be set directly with
 * {@link #setValue(int)}.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public class NumberWheelButton extends BetterButton implements ThemedComponent {
    /** The randomly-generated serial ID. */
    private static final long serialVersionUID = 7438789081511768660L;
    
    /** The internal loop of possible values for the number wheel. */
    private final Loop<Integer> loop = new Loop<Integer>(new Integer[]{
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9
    });
    /** The color of the highlight in the vertical center of the wheel background. */
    private Color colorHighlight = PropDefault.THEME.color(COLOR.BG_ACCENT);
    /** The color of the number text within the wheel. */
    private Color colorText = PropDefault.THEME.color(COLOR.BTN_TXT);
    /** The total display size of the wheel. */
    private static final Dimension wheelSize = new Dimension(40, 100);
    
    /**
     * Creates a NumberWheelButton with the passed {@link Font}.
     * 
     * @param f - the {@link Font} to use when displaying the digits in the wheel.
     */
    public NumberWheelButton(Font f) {
        super("0", f);
        
        this.setFont(f.deriveFont(20f));
        this.setPreferredSize(wheelSize);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        // Create Graphics and Rendering Hints
        final Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Simplify retrieval of width and height.
        final int width  = getWidth();
        final int height = getHeight();
        
        // Select the Color Based on Pressed State
        if(getModel().isPressed()) g2d.setColor(colorBGPressed);
        else                       g2d.setColor(colorBG);
        // Paint general background area of button.
        g2d.fillRoundRect(1, 1, width-2, height-2,
                (roundedArc != -1 ? roundedArc : 40),
                (roundedArc != -1 ? roundedArc : 40));
        
        // Paint Highlighted Area
        g2d.setColor(colorHighlight);
        g2d.fillRoundRect(1, 1 + height/3, width-2, height/3,
                (roundedArc != -1 ? roundedArc : 40),
                (roundedArc != -1 ? roundedArc : 40));
        
        // Setup Text Color and Font
        g2d.setColor(colorText);
        g2d.setFont(getFont().deriveFont(24f));
        
        // Text placeholder to retrieve font information.
        final String text = "0";
        // Get font metrics for text measurement.
        FontMetrics fm = g2d.getFontMetrics();
        
        // Get width and height of the text.
        int textWidth  = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        
        // Calculate the X and Y coordinates to center the text.
        int x = (width  - textWidth)  / 2;
        int y = (height + textHeight) / 2 - fm.getDescent(); // Subtract fm.getDescent() to adjust baseline to the vertical center.
        
        // Draw larger, current digit in center of highlighted area.
        g2d.drawString(""+loop.current(), x, y);
        
        // Reset Font. Prepare for similar font process for above and below digits.
        g2d.setFont(getFont());
        // Get font metrics for text measurement.
        fm = g2d.getFontMetrics();
        
        // Get width and height of the text.
        textWidth  = fm.stringWidth(text);
        textHeight = fm.getHeight();
        
        // Calculate the X and Y coordinates to center the text.
        x = (width  - textWidth)  / 2;
        y = (height + textHeight) / 2 - fm.getDescent(); // Subtract fm.getDescent() to adjust baseline to the vertical center.
        
        // Draw above and below digits based on current digit.
        if (loop.current() != 0)
            g2d.drawString(String.valueOf(loop.current()-1), x, height/3 - fm.getDescent());
        else
            g2d.drawString(String.valueOf(9), x, height/3 - fm.getDescent());
        if (loop.current() != 9)
            g2d.drawString(String.valueOf(loop.current()+1), x, height/2 + (height/3) + fm.getDescent());
        else
            g2d.drawString(String.valueOf(0), x, height/2 + (height/3) + fm.getDescent());
            
        
        // Setup gradient values for fade overlay.
        float midPoint  = height / 2f;   // Controls the size of the middle point.
        float fadeRange = height / 4f;  // Controls the height of the transparent area.

        // Transparency Color
        final Color transparency = AppRes.COLOR_TRANSPARENT;

        // Creates a gradient that goes:  BG → Transparency → BG
        final LinearGradientPaint gradient = new LinearGradientPaint(
            0, height/8, 0, height - (height/8), 
            new float[] { 0f, (midPoint - fadeRange) / height, (midPoint + fadeRange) / height, 1f },
            new Color[] { colorBGPressed, transparency, transparency, colorBGPressed }
        );

        // Set the gradient paint to the graphics context
        g2d.setPaint(gradient);

        // Paint the gradient overlay, rounded and within the border of the component.
        g2d.fillRoundRect(2, 2, width - 4, height - 4, (roundedArc != -1 ? roundedArc : 40),
                (roundedArc != -1 ? roundedArc : 40));
        
        // Dispose of the Graphics2D Object
        g2d.dispose();
    }
    
    /**
     * Gets the current value of the wheel, shown in the center area.
     * 
     * @return an int with the current digit.
     */
    public int getValue() {
        return this.loop.current();
    }
    
    /**
     * Sets the value of the wheel to be the passed digit.
     * <p>
     * The passed int is bounded within <code>0-9</code> to keep it as a single
     * digit. This method will then continue through the loop until the digit
     * matches.
     * 
     * @param value - an int with the value to set.
     */
    public void setValue(int value) {
        // Protection: Keep value within 0-9 bounds.
        value = Math.max(0, Math.min(value, 9));
        // Return early if already on the passed value.
        if (this.loop.current() == value) return;
        // Loop until the passed value is reached.
        while (this.loop.next() != value);
    }
    
    /**
     * Proceeds one step through the wheel and its digit loop.
     * 
     * @return an int with the value after going to the next digit.
     */
    public int next() {
        return this.loop.next();
    }
    
    /**
     * Proceeds one step backwards through the wheel and its digit loop.
     * 
     * @return an int with the value after going to the previous digit.
     */
    public int previous() {
        return this.loop.previous();
    }
    
    @Override
    public NumberWheelButton theme(THEME_OPTION theme) {
        this.colorBG = theme.color(COLOR.BTN);
        this.colorBGPressed = theme.color(COLOR.BTN_PRESSED);
        this.colorBorder = theme.color(COLOR.BTN_BORDER);
        this.colorHighlight = theme.color(COLOR.BTN_HOVER);
        this.colorText = theme.color(COLOR.BTN_TXT);
        this.repaint(); // Repaint to ensure new colors are being displayed.
        return this;
    }
}
