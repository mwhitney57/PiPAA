package dev.mwhitney.gui.components;

import static dev.mwhitney.gui.PiPWindow.BORDER_SIZE;
import static dev.mwhitney.gui.PiPWindowState.StateProp.FULLSCREEN;
import static dev.mwhitney.gui.PiPWindowState.StateProp.HW_ACCELERATION;
import static dev.mwhitney.gui.PiPWindowState.StateProp.PASSTHROUGH;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Objects;

import javax.swing.JComponent;
import javax.swing.JFrame;

import dev.mwhitney.gui.PiPWindow;
import dev.mwhitney.gui.PiPWindowState;
import dev.mwhitney.gui.interfaces.AdaptiveOpacity;
import dev.mwhitney.resources.AppRes;
import dev.mwhitney.util.PiPAAUtils;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

/**
 * A simple {@link JComponent} intended to be used as a glass pane for a
 * {@link JFrame}.
 * 
 * Despite the name, this component doesn't do anything special, but it is used
 * in a special way.
 * <h2>The Problem</h2>
 * <p>
 * The vlcj {@link EmbeddedMediaPlayerComponent} does not like when a
 * {@link JFrame} has a fully transparent background, at least with this app's
 * setup for each window. It will ignore mouse input and allow clicks on the
 * video surface to pass through as if nothing was there.
 * <p>
 * This becomes problematic if the frame background needs to be transparent. For
 * example, the edges of the frame need to always be transparent to allow the
 * border to do its own color work without mixing or interference. However,
 * overriding the painting of the background to only paint the content area,
 * excluding the border, ultimately failed or presented additional problems. It
 * was buggy and often lead to painting issues.
 * <h2>The Fix</h2>
 * <p>
 * Usage of a glass pane was one of the first solutions tested. However, it was
 * assumed that the near-transparent color would be visible over the entire
 * window. This is typically true, but it was discovered that the vlcj component
 * gets painted <b>above</b> the glass pane. Therefore, the glass pane's only
 * drawback was void. There shouldn't be any color skewing, <b>so long as it is
 * solely used in this context with vlcj, not with other Swing components.</b>
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public class SpecialGlassPane extends JComponent implements AdaptiveOpacity {
    /** The randomly-generated serial ID. */
    private static final long serialVersionUID = 2303414505288020519L;

    /** The {@link PiPWindow} parent which uses this glass pane. */
    private final PiPWindow parent;
    
    /**
     * Creates a glass pane linked to the passed window. The parent window 
     * <p>
     * If the glass pane must be temporarily shown or hidden, opt for using
     * {@link #reveal()}, {@link #conceal()}, or {@link #setVisible(boolean)}
     * directly, as opposed to removing it from its parent window. It is simpler and
     * should be the best solution. {@link JFrame#setGlassPane(java.awt.Component)}
     * does not accept {@code null} values which makes removal more tedious.
     * 
     * @param parent - the {@link PiPWindow} this glass pane will be used in.
     */
    public SpecialGlassPane(PiPWindow parent) {
        this.parent = Objects.requireNonNull(parent, "The provided parent frame for SpecialGlassPane must be non-null.");
        
        this.setBackground(AppRes.NEAR_TRANSPARENT);
        this.setOpaque(false);
        this.setFocusable(false);
    }
    
    /**
     * Reveals the glass pane, making it visible. Shorthand for calling
     * {@link #setVisible(boolean)} with {@code true}. Alternative for method
     * reference usage and readability.
     */
    public void reveal() {
        this.setVisible(true);
    }
    
    /**
     * Conceals the glass pane, making it invisible. Shorthand for calling
     * {@link #setVisible(boolean)} with {@code false}. Alternative for method
     * reference usage and readability.
     */
    public void conceal() {
        this.setVisible(false);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        final Graphics2D g2d = (Graphics2D) g.create();
        try {
            final PiPWindowState state = parent.state();
            final int parentW = parent.getWidth(), parentH = parent.getHeight();
            // Start with common case of only painting inner content area.
            int x = BORDER_SIZE, y = BORDER_SIZE, w = parentW - (BORDER_SIZE * 2), h = parentH - (BORDER_SIZE * 2);
            // Expand the paint area to the entire window when in fullscreen mode, since borders will disappear.
            if (state.is(FULLSCREEN)) {
                // Narrow the paint area to allow pass-through transparency in areas surrounding the media.
                final boolean narrowedArea =
                        parent.hasAttributedMedia()
                        && state.is(PASSTHROUGH)
                        // Incompatible: hardware acceleration forces opaque view → transparency becomes obstructive black void.
                        && state.not(HW_ACCELERATION);
                
                if (narrowedArea) {
                    // Only covers the media content in the window.
                    final Dimension mediaArea = parent.getMedia().getAttributes().getScaledFullscreenSize();
                    x = (parentW - mediaArea.width)  / 2;
                    y = (parentH - mediaArea.height) / 2;
                    w = mediaArea.width;
                    h = mediaArea.height;
                } else {
                    // Default: Cover the entire window.
                    x = 0;
                    y = 0;
                    w = parentW;
                    h = parentH;
                }
            }
            
//            g2d.setColor(new Color(255, 0, 0, 122));  // Red Debug Color for Visibility
            g2d.setColor(getBackground());
            g2d.fillRect(x, y, w, h);
        } finally {
            g2d.dispose();
        }
    }

    @Override
    public void adaptToOpacity(float opacity) {
        this.setBackground(PiPAAUtils.getMinimumInteractableColor(opacity));
    }
}
