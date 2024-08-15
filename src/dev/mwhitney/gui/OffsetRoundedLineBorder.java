package dev.mwhitney.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.border.LineBorder;

/**
 * An offset, always-rounded expansion upon the LineBorder class.
 * 
 * @author mwhitney57
 */
public class OffsetRoundedLineBorder extends LineBorder {
    /**
     * The default, randomly-generated serial ID for OffsetRoundedLineBorders.
     */
    private static final long serialVersionUID = -6839791208603815787L;

    /**
     * Creates a new LineBorder which is offset and rounded. This ensures that any
     * component utilizing this border, which fills up the entire space of its
     * container, will not show the background color in the corners where the border
     * rounds off. As a result of this, border thickness values may be inaccurate
     * and require slight tweaking.
     * 
     * @param color     - the Color to paint the border with.
     * @param thickness - an int for the thickness of the border in pixels.
     */
    public OffsetRoundedLineBorder(Color color, int thickness) {
        super(color, thickness, true);
    }
    
    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        super.paintBorder(c, g, x-3, y-3, width+6, height+6);
    }
}
