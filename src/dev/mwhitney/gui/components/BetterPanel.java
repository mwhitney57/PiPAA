package dev.mwhitney.gui.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * An incredibly-basic extension upon JPanel that changes a few default settings/behaviors.
 * 
 * @author mwhitney57
 */
public class BetterPanel extends JPanel {
    /** A randomly-generated, unique serial ID for BetterPanels. */
    private static final long serialVersionUID = -1101549190177346722L;
    
    /** The width of the drop shadow on each side surrounding the panel. */
    private static int SHADOW = 5;
    
    /** Whether or not to paint and use the panel drop shadow. */
    private boolean useDropShadow;
    /** The boolean determining whether or not to paint a background and border at all. */
    private boolean glassMode;
    /** The Color to paint the border with. */
    private Color borderColor;
    /** The arc for the rounded corners. */
    private int roundedArc;
    /** The stroke used by the Graphics2D object when painting this component. */
    private BasicStroke g2dStroke;
    
    /**
     * Creates a BetterPanel with the passed label and text font.
     * 
     * @param layout - a LayoutManager with the layout to use for the panel.
     */
    public BetterPanel(LayoutManager layout) {
        this(layout, false);
    }
    
    /**
     * Creates a BetterPanel with the passed label and text font.
     * 
     * @param layout - a LayoutManager with the layout to use for the panel.
     */
    public BetterPanel(LayoutManager layout, boolean glass) {
        super(layout);
        
        this.glassMode = glass;
        this.roundedArc = 60;
        this.g2dStroke = new BasicStroke(10.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f);
        
        if (glassMode)
            setBorder(null);
        else
            setBorder(BorderFactory.createEmptyBorder(18, 15, 20, 15));
        setOpaque(false);
    }
    
    /**
     * Sets the color of the panel's built-in border.
     * 
     * @param c - the Color for the border.
     */
    public void setBorderColor(Color c) {
        this.borderColor = c;
    }
    
    /**
     * Sets the rounded arc amount for the panel's corners.
     * 
     * @param arc - an int with the amount.
     */
    public void setRoundedArc(int arc) {
        this.roundedArc = arc;
    }
    
    /**
     * Enables the use of the panel's drop shadow.
     * 
     * @return this BetterPanel instance.
     */
    public BetterPanel useDropShadow() {
        this.useDropShadow = true;
        return this;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Do not paint background when in glass mode.
        if (glassMode)
            return;
        
        // G2D and Rendering Hints
        final Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw the Rounded Drop Shadow
        final int shdwOff = (useDropShadow ? SHADOW : 0);
        if (useDropShadow) {
            for (int i = 0; i < SHADOW; i++) {
                g2d.setColor(new Color(0, 0, 0, (int) ((10.0 / SHADOW) * i)));
                g2d.fillRoundRect(i, i, this.getWidth() - (i * 2), this.getHeight() - (i * 2), roundedArc*2, roundedArc*2);
            }
        }
        
        // Draw rounded panel.
        g2d.setColor(getBackground());
        g2d.fillRoundRect(getX() + shdwOff + 10, getY() + shdwOff + 10, getWidth() - (shdwOff*2) - 20, getHeight() - (shdwOff*2) - 20, roundedArc, roundedArc);
        
        // Draw rounded panel's outline/border then G2D dispose.
        g2d.setColor(this.borderColor);
        g2d.setStroke(g2dStroke);
        g2d.drawRoundRect(getX() + shdwOff + 6, getY() + shdwOff + 6, getWidth() - (shdwOff*2) - 12, getHeight() - (shdwOff*2) - 12, roundedArc + 10, roundedArc + 10);
        g2d.dispose();
    }
}
