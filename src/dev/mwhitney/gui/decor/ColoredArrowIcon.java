package dev.mwhitney.gui.decor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;

/**
 * A color-customizable arrow {@link Icon} which looks similar to the greater
 * than symbol or right arrow sign (>).
 * 
 * @author mwhitney57
 */
public class ColoredArrowIcon implements Icon {
    /** The size of this icon in pixels. */
    private static final int SIZE = 10; 
    /** The {@link Color} of the arrow icon. */
    private final Color color;

    /**
     * Creates a new ColoredArrowIcon. This color-customizable arrow icon is
     * supposed to look like the greater than symbol or right arrow: <code>></code>
     * 
     * @param color - the {@link Color} to use when painting this icon.
     */
    public ColoredArrowIcon(final Color color) {
        this.color = color;
    }

    @Override
    public int getIconWidth() { return SIZE; }

    @Override
    public int getIconHeight() { return SIZE; }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        final Graphics2D g2d = (Graphics2D) g;

        // Setup x and y-coordinate points for drawing: >
        final int[] xPoints = {x + 2, x + 5, x + 2};
        final int[] yPoints = {y + 2, y + 5, y + 8};

        // Enable anti-aliasing.
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Adjust drawing style.
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(2));
        // Draw each polygon line.
        g2d.drawPolyline(xPoints, yPoints, 3);
        
        // Disable anti-aliasing.
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }
}
