package dev.mwhitney.gui.components.better;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.SwingConstants;

/**
 * An incredibly-basic extension upon JButton that changes a few default settings/behaviors.
 * 
 * @author mwhitney57
 */
public class BetterButton extends JButton {
    /** A randomly-generated, unique serial ID for BetterButtons. */
    private static final long serialVersionUID = -5531816749938062883L;
    
    /** The Color of the button's background. */
    protected Color colorBG;
    /** The Color of the button's background (when pressed.) */
    protected Color colorBGPressed;
    /** The Color of the button's border. */
    protected Color colorBorder;
    
    /** The arc for the rounded corners. */
    protected int roundedArc       = -1;
    /** The arc for the rounded border corners. */
    protected int roundedArcBorder = -1;
    
    /**
     * Creates a BetterButton with the passed label and label font.
     * 
     * @param label - a String with the label text.
     * @param f - the Font to use for the label text.
     */
    public BetterButton(String label, Font f) {
        super(label);
        
        setFont(f);
        setFocusable(false);
        setContentAreaFilled(false);
        setVerticalTextPosition(SwingConstants.BOTTOM);
        setHorizontalTextPosition(SwingConstants.CENTER);
        setFocusPainted(false);
        setPreferredSize(new Dimension(80, 40));
        
        // Set Colors to Defaults
        colorBG = Color.LIGHT_GRAY;
        colorBGPressed = colorBG.darker();
        colorBorder = Color.GRAY;
    }
    
    /**
     * Creates a BetterButton with the passed label and font, then adds the passed
     * listener.
     * 
     * @param label - a String with the label text.
     * @param f     - the Font to use for the label text.
     * @param al    - the {@link ActionListener} to add after construction.
     */
    public BetterButton(String label, Font f, ActionListener al) {
        this(label, f);
        this.addActionListener(al);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        // Create Graphics and Rendering Hints
        final Graphics2D g2d = (Graphics2D) g.create();
        try {
            // Select the Color Based on Pressed State
            if(getModel().isPressed())
                g2d.setColor(colorBGPressed);
            else
                g2d.setColor(colorBG);
            g2d.fillRoundRect(1, 1, getWidth()-2, getHeight()-2,
                    (roundedArc != -1 ? roundedArc : 40),
                    (roundedArc != -1 ? roundedArc : 40));
        } finally {
            // Dispose of the Graphics2D Object
            g2d.dispose();
        }
        
        super.paintComponent(g);
    }
    
    @Override
    protected void paintBorder(Graphics g) {
        // Create Graphics and Rendering Hints
        final Graphics2D g2d = (Graphics2D) g.create();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draws the Rounded Border
            g2d.setColor(colorBorder);
            g2d.setStroke(new BasicStroke(4.0f));
            g2d.drawRoundRect(2, 2, getWidth()-5, getHeight()-5,
                    (roundedArcBorder != -1 ? Math.max(0, roundedArcBorder) : 25),
                    (roundedArcBorder != -1 ? Math.max(0, roundedArcBorder) : 25));
        } finally {
            // Dispose of the Graphics2D Object
            g2d.dispose();
        }
    }
    
    /**
     * Sets the colors for the BetterButton, including the background color, its
     * pressed variant, and the border color.
     * 
     * @param bg        - the Color of the background.
     * @param bgPressed - the Color of the background when the button is pressed.
     * @param border    - the Color of the border.
     */
    public void setColors(Color bg, Color bgPressed, Color border) {
        this.colorBG = bg;
        this.colorBGPressed = bgPressed;
        this.colorBorder = border;
        this.repaint(); // Repaint to ensure new colors are being displayed.
    }
    
    /**
     * Sets the rounded arc amount for the button's corners.
     * Any value <code>>=0</code> is acceptable.
     * 
     * @param arc - an int with the amount.
     */
    public void setRoundedArc(int arc) {
        this.roundedArc = arc;
    }
    
    /**
     * Sets the rounded arc amount for the corners on the button's border.
     * Any value <code>>=0</code> is acceptable.
     * 
     * @param arc - an int with the amount.
     */
    public void setBorderRoundedArc(int arc) {
        this.roundedArcBorder = arc;
    }
}
