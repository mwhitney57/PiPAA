package dev.mwhitney.util;

import java.awt.Dimension;

/**
 * A small extension upon {@link Dimension} which allows for the size to be
 * scaled while maintaining the aspect ratio determined by its current
 * proportions.
 * <p>
 * This class also offers the ability to set minimums for the width and height
 * values, ensuring that they are respected even during scaling operations.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public class ScalingDimension extends Dimension {
    /** The randomly-generated serial ID. */
    private static final long serialVersionUID = -4111589005859796875L;
    
    /** The minimum <b>width</b> value, which is zero by default and equates to no minimum. */
    private int minimumWidth;
    /** The minimum <b>height</b> value, which is zero by default and equates to no minimum. */
    private int minimumHeight;
    
    /**
     * Creates a {@link ScalingDimension} from the passed {@link Dimension},
     * deriving its size.
     * 
     * @param d - the {@link Dimension} to derive from.
     * @return the new {@link ScalingDimension} instance.
     */
    public static ScalingDimension from(Dimension d) {
        final ScalingDimension scalingD = new ScalingDimension();
        scalingD.setSize(d);
        return scalingD;
    }
    
    /**
     * Sets the minimum size for both the width and height.
     * 
     * @param size - an int with the size in pixels.
     */
    public void setMinimumSize(int size) {
        setMinimumWidth(size);
        setMinimumHeight(size);
    }
    
    /**
     * Sets the minimum size for the width.
     * 
     * @param size - an int with the size in pixels.
     */
    public void setMinimumWidth(int size) {
        this.minimumWidth = Math.max(0, size);
    }
    
    /**
     * Sets the minimum size for the height.
     * 
     * @param size - an int with the size in pixels.
     */
    public void setMinimumHeight(int size) {
        this.minimumHeight = Math.max(0, size);
    }
    
    /**
     * Scales the dimension to the passed width, maintaining the current aspect
     * ratio while respecting any set minimums in size.
     * <p>
     * After first scaling the size, this method checks to ensure that the new
     * height will not be below the minimum set by {@link #setMinimumHeight(int)} or
     * {@link #setMinimumSize(int)}. If it is, this method will scale up past the
     * desired width to keep itself at the minimum height.
     * 
     * @param w - an int with the width to scale to in pixels.
     * @return this {@link ScalingDimension} instance.
     */
    public ScalingDimension scaleToWidth(int w) {
        // Keep the target width in bounds at or above the minimum.
        w = Math.max(this.minimumWidth, w);
        // Determine the ratio, then find the scaled height to match the target width.
        final double ratio = ((double) getWidth() / (double) getHeight());
        final int scaledH = (int) (w / ratio);
        // If the scaled height does not meet the minimum, scale it up to the minimum and return that result.
        if (scaledH < this.minimumHeight) return scaleToHeight(this.minimumHeight);
        // Otherwise, continue to set the new scaled size and return.
        this.setSize(w, scaledH);
        return this;
    }
    
    /**
     * Scales the dimension to the passed height, maintaining the current aspect
     * ratio while respecting any set minimums in size.
     * <p>
     * After first scaling the size, this method checks to ensure that the new width
     * will not be below the minimum set by {@link #setMinimumWidth(int)} or
     * {@link #setMinimumSize(int)}. If it is, this method will scale up past the
     * desired height to keep itself at the minimum width.
     * 
     * @param h - an int with the height to scale to in pixels.
     * @return this {@link ScalingDimension} instance.
     */
    public ScalingDimension scaleToHeight(int h) {
        // Keep the target height in bounds at or above the minimum.
        h = Math.max(this.minimumHeight, h);
        // Determine the ratio, then find the scaled width to match the target height.
        final double ratio = ((double) getWidth() / (double) getHeight());
        final int scaledW = (int) (ratio * h);
        // If the scaled width does not meet the minimum, scale it up to the minimum and return that result.
        if (scaledW < this.minimumWidth) return scaleToWidth(this.minimumWidth);
        // Otherwise, continue to set the new scaled size and return.
        this.setSize(scaledW, h);
        return this;
    }
}
