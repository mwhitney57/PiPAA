package dev.mwhitney.gui.components;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JLabel;

/**
 * A simple extension upon {@link JLabel} that allows for partial background
 * transparency without paint issues. Using this class may be necessary if the
 * label is not opaque and the background must be painted, even if it has
 * transparency. Under the regular {@link JLabel} logic, paints to the
 * background may be ignored entirely when the component is not opaque.
 * <p>
 * The paint method is overridden to force the painting of the background first,
 * using the {@link Color} provided by {@link #getBackground()}.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public class TransparentLabel extends JLabel {
    /** The randomly-generated serial ID. */
    private static final long serialVersionUID = -7992228942336528683L;
    
    @Override
    protected void paintComponent(Graphics g) {
        // Force-paint the background.
        final Graphics2D g2d = (Graphics2D) g.create();
        try {
            g2d.setColor(getBackground());
            g2d.fillRect(0, 0, getWidth(), getHeight());
        } finally {
            g2d.dispose();
        }
        super.paintComponent(g);
    }
}
