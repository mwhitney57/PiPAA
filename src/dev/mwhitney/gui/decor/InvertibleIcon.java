package dev.mwhitney.gui.decor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.io.Serializable;

import javax.swing.Icon;
import javax.swing.plaf.UIResource;

/**
 * An Icon which acts as a middle-man between another Icon and its owner,
 * allowing for color inversion.
 * 
 * @author mwhitney57
 */
public class InvertibleIcon implements Icon, UIResource, Serializable {
    /** A randomly-generated, unique serial ID for InvertibleIcons. */
    private static final long serialVersionUID = 7155455002351840041L;
    
    /** The Icon that should be capable of being inverted. */
    private Icon icon;
    /** The boolean for whether or not the icon is currently inverted. */
    private boolean inverted;
    
    /**
     * Creates a new InvertibleIcon, which is essentially a middle-man between the
     * passed Icon and whatever is calling its <code>paintIcon(...)</code> method.
     * 
     * @param i - the Icon that should be invertible.
     */
    public InvertibleIcon(Icon i) {
        this.icon = i;
    }
    
    /**
     * Normalizes the icon, ensuring that it is <b>not</b> inverted.
     */
    public void normalize() {
        this.inverted = false;
    }
    
    /**
     * Inverts the icon, making it paint in a somewhat-inverted color.
     */
    public void invert() {
        this.inverted = true;
    }
    
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        // Do nothing if there is no Icon to paint.
        if (this.icon == null)
            return;
        
        if (inverted)
            g.setXORMode(Color.WHITE);
        icon.paintIcon(c, g, x, y);
    }

    @Override
    public int getIconWidth() {
        return icon.getIconWidth();
    }

    @Override
    public int getIconHeight() {
        return icon.getIconHeight();
    }
}
