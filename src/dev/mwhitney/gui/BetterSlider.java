package dev.mwhitney.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

/**
 * An incredibly-basic extension upon JSlider that changes a few default settings/behaviors.
 * 
 * @author mwhitney57
 */
public class BetterSlider extends JSlider {
    /** A randomly-generated, unique serial ID for BetterLabels. */
    private static final long serialVersionUID = -5093004319704976291L;
    
    /** A set of Colors for the slider and its thumb. */
    private Color sliderColor, sliderEmptyColor, sliderThumbColor;
    
    /**
     * Creates a BetterSlider with the passed min, max, and initial values.
     * 
     * @param min - an int with the minimum allowed value.
     * @param max - an int with the maximum allowed value.
     * @param init - an int with the initial value.
     */
    public BetterSlider(int min, int max, int init) {
        super(JSlider.HORIZONTAL, min, max, init);
        
        setFocusable(false);
        setOpaque(false);
        setUI(new BasicSliderUI() {
            /*
             * Parts of the code below was originally sourced from user @weisj on StackOverflow
             * while searching for a solution to changing the color of specific JSlider parts.
             * Their implementation has been heavily modified to better suit this application.
             * 
             * https://stackoverflow.com/a/62613662
             * https://stackoverflow.com/u/12174160
             */
            private static final int TRACK_THICKNESS = 10;
            private static final int TRACK_ARC = 5;
            private static final Dimension THUMB_SIZE = new Dimension(20, 20);
            private final RoundRectangle2D.Float trackShape = new RoundRectangle2D.Float();

            /**
             * Checks if the slider is horizontally-oriented.
             * @return <code>true</code> if the slider is horizontal; <code>false</code> otherwise.
             */
            private boolean horizontal() {
                return slider.getOrientation() == JSlider.HORIZONTAL;
            }
            
            @Override
            protected void calculateTrackRect() {
                super.calculateTrackRect();
                if (horizontal()) {
                    trackRect.y = trackRect.y + (trackRect.height - TRACK_THICKNESS) / 2;
                    trackRect.height = TRACK_THICKNESS;
                } else {
                    trackRect.x = trackRect.x + (trackRect.width  - TRACK_THICKNESS)  / 2;
                    trackRect.width  = TRACK_THICKNESS;
                }
                trackShape.setRoundRect(trackRect.x, trackRect.y, trackRect.width, trackRect.height, TRACK_ARC, TRACK_ARC);
            }

            @Override
            protected void calculateThumbLocation() {
                super.calculateThumbLocation();
                // Adjust position to match adjusted track location and dimensions.
                if (horizontal()) thumbRect.y = trackRect.y + (trackRect.height - thumbRect.height) / 2;
                else              thumbRect.x = trackRect.x + (trackRect.width  - thumbRect.width ) / 2;
            }

            @Override
            protected Dimension getThumbSize() { return THUMB_SIZE; }

            @Override
            public void paint(final Graphics g, final JComponent c) {
                // Enable anti-aliasing during the entire slider painting process.
                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                super.paint(g, c);
                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            }

            @Override
            public void paintTrack(final Graphics g) {
                final Rectangle trackBounds = trackRect;

                if (horizontal()) {
                    final int cy = (trackBounds.height / 2) - (TRACK_THICKNESS/2);

                    g.translate(trackBounds.x, trackBounds.y + cy);

                    // Paint sub, not covered slider area.
                    g.setColor(sliderEmptyColor);
                    g.fillRoundRect(0, 1, trackBounds.width, trackBounds.height-2, TRACK_ARC, TRACK_ARC);
                    // Paint top, covered slider area.
                    g.setColor(sliderColor);
                    g.fillRoundRect(0, 1, thumbRect.x, trackBounds.height-2, TRACK_ARC, TRACK_ARC);

                    g.translate(-trackBounds.x, -(trackBounds.y));
                }
                else {
                    int cx = (trackBounds.width / 2) - (TRACK_THICKNESS/2);

                    g.translate(trackBounds.x + cx, trackBounds.y);

                    // Paint sub, not covered slider area.
                    g.setColor(sliderEmptyColor);
                    g.fillRoundRect(1, 0, trackBounds.height, trackBounds.width-2, TRACK_ARC, TRACK_ARC);
                    // Paint top, covered slider area.
                    g.setColor(sliderColor);
                    g.fillRoundRect(1, 0, thumbRect.y, trackBounds.width-2, TRACK_ARC, TRACK_ARC);
                    
                    g.translate(-(trackBounds.x), -trackBounds.y);
                }
            }

            @Override
            public void paintThumb(final Graphics g) {
                if (sliderThumbColor != null) g.setColor(sliderThumbColor);
                g.fillRoundRect(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height, TRACK_ARC, TRACK_ARC);
            }
        });
    }
    
    /**
     * Sets the Color of the slider. The slider is the portion which is filled and
     * represents the progress within the entire bar. This differs from the empty
     * slider portion, which is the underlying color representing the slider's total
     * available space.
     * 
     * @param c - the Color to use for the slider.
     * @see #setSliderEmptyColor(Color)
     * @see #setSliderThumbColor(Color)
     */
    public void setSliderColor(final Color c) {
        this.sliderColor = c;
    }
    
    /**
     * Sets the Color of the slider's empty portion. The empty portion of the slider,
     * which is the underlying color representing the slider's total available space.
     * 
     * @param c - the Color to use for the empty slider space.
     * @see #setSliderColor(Color)
     * @see #setSliderThumbColor(Color)
     */
    public void setSliderEmptyColor(final Color c) {
        this.sliderEmptyColor = c;
    }

    /**
     * Sets the Color of the slider's thumb. The thumb is located at the point in
     * the slider where the progress is. In other words, if a slider is 50% full,
     * the thumb will be located at that halfway, 50% mark. The user can move the
     * thumb to determine a new value for the slider.
     * 
     * @param c - the Color to use for the slider thumb.
     * @see #setSliderColor(Color)
     * @see #setSliderEmptyColor(Color)
     */
    public void setSliderThumbColor(final Color c) {
        this.sliderThumbColor = c;
    }
}
