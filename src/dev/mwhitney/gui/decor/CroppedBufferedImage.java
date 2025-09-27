package dev.mwhitney.gui.decor;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * A wrapper for the {@link BufferedImage} class which allows for easy cropping,
 * especially with transparent edges.
 * 
 * @author mwhitney57
 */
public class CroppedBufferedImage {
    /** The BufferedImage with the original, uncropped image. */
    private BufferedImage img;
    /** The crop amounts for each side, measured in pixels. */
    private int cropL, cropR, cropT, cropB;
    
    /**
     * Creates a CroppedBufferedImage using the passed {@link BufferedImage}.
     * 
     * @param img - the {@link BufferedImage} to wrap around and allow for cropping.
     */
    public CroppedBufferedImage(final BufferedImage img) {
        this.img = img;
        cropL = 0;
        cropR = 0;
        cropT = 0;
        cropB = 0;
    }
    
    /**
     * Determines the image's crop amounts on each side, using the passed
     * transparency limit int. The int represents a number <code>0-255</code> for
     * the maximum alpha value a pixel can have while being considered
     * "transparent." Any pixel with an alpha value greater than this int will be
     * considered transparent and potentially be cropped. Any int <code><0</code> or
     * <code>>255</code> will be functionally identical to passing the number at the
     * respective end of the range.
     * <p>
     * The new image crop amounts are stored internally within this
     * CroppedBufferedImage instance and can be accessed via {@link #cropLeft()},
     * {@link #cropRight()}, {@link #cropTop()}, and {@link #cropBottom()}.
     * <p>
     * This method does nothing if the internal image is <code>null</code> or this
     * CroppedBufferedImage has been flushed via {@link #flush()}.
     * 
     * @param transparencyLimit - an int with the maximum alpha value for a pixel to
     *                          be considered "transparent."
     */
    public void determineCrop(final int transparencyLimit) {
        if (!imgValid()) return;

        // Trims all transparency on edges surrounding content of image.
        final int width = img.getWidth(), height = img.getHeight();

        boolean hasLeft = true, hasRight = true, hasTop = true, hasBottom = true;
        // LEFT
        for (int x = 0; x < width      && hasLeft; x++) {
            for (int y = 0; y < height && hasLeft; y++) {
//                System.out.println("Checking pixel: (" + x + ", " + y + ")");
                if (new Color(img.getRGB(x, y), true).getAlpha() > transparencyLimit) hasLeft = false;
            }
            if (hasLeft) cropL++;
        }
        // TOP
        for (int y = 0; y < height    && hasTop; y++) {
            for (int x = 0; x < width && hasTop; x++) {
//                System.out.println("Checking pixel: (" + x + ", " + y + ")");
                if (new Color(img.getRGB(x, y), true).getAlpha() > transparencyLimit) hasTop = false;
            }
            if (hasTop) cropT++;
        }
        // RIGHT
        for (int x = width - 1; x >= 0 && hasRight; x--) {
            for (int y = 0; y < height && hasRight; y++) {
//                System.out.println("Checking pixel: (" + x + ", " + y + ")");
                if (new Color(img.getRGB(x, y), true).getAlpha() > transparencyLimit) hasRight = false;
            }
            if (hasRight) cropR++;
        }
        // BOTTOM
        for (int y = height - 1; y >= 0 && hasBottom; y--) {
            for (int x = 0; x < width   && hasBottom; x++) {
//                System.out.println("Checking pixel: (" + x + ", " + y + ")");
                if (new Color(img.getRGB(x, y), true).getAlpha() > transparencyLimit) hasBottom = false;
            }
            if (hasBottom) cropB++;
        }
        System.out.println("Finished determining crop for frame. " + cropL + "/" + cropT + "/" + cropR + "/" + cropB);
    }
    
    /**
     * Checks if the internal image is valid (non-<code>null</code>).
     * 
     * @return <code>true</code> if the image is valid; <code>false</code>
     *         otherwise.
     */
    private boolean imgValid() {
        return (this.img != null);
    }
    
    /**
     * Returns the raw, uncropped {@link BufferedImage} stored within this CroppedBufferedImage.
     * 
     * @return the internal {@link BufferedImage}.
     */
    public BufferedImage raw() {
        return this.img;
    }
    
    /**
     * Flushes the internal {@link BufferedImage} and nullifies it to prepare for
     * garbage collection. This method will fully invalidate or break certain future
     * calls, such as {@link #determineCrop(int)}, {@link #raw()}, or
     * {@link #cropped()}. However, the internal crop amounts will still be
     * accessible for reference.
     */
    public void flush() {
        if (imgValid()) {
            this.img.flush();
            this.img = null;
        }
    }
    
    /**
     * Returns a new, cropped version of the internal {@link BufferedImage} using the
     * internal crop amounts. The internal crop amounts are determined by calling
     * {@link #determineCrop(int)} or setting them manually via
     * {@link #setCrop(int, int, int, int)}.
     * 
     * @return the cropped {@link BufferedImage}.
     * @see {@link #cropped(int, int, int, int)} for specifying custom crop amounts for each side.
     */
    public BufferedImage cropped() {
        return cropped(this.cropL, this.cropR, this.cropT, this.cropB);
    }
    
    /**
     * Returns a new, cropped version of the internal {@link BufferedImage} using
     * the specified crop amounts.
     * 
     * @param l - the left side crop amount.
     * @param r - the right side crop amount.
     * @param t - the top side crop amount.
     * @param b - the bottom side crop amount.
     * @return the cropped {@link BufferedImage}.
     * @see {@link #cropped()} for automatically using the internal crop amounts.
     */
    public BufferedImage cropped(final int l, final int r, final int t, final int b) {
        if (!imgValid()) return null;
        
        // crop using crop values and return cropped image.
        return this.img.getSubimage(l, t, this.img.getWidth() - (l + r), this.img.getHeight() - (t + b));
    }
    
    /**
     * Gets the cropped width of the internal image, which is its normal width minus
     * the left and right internal crop amounts.
     * 
     * @return the cropped width of the image, or <code>-1</code> if the internal
     *         image is <code>null</code>.
     */
    public int croppedWidth() {
        return (imgValid() ? this.img.getWidth()  - (this.cropL + this.cropR) : -1);
    }
    
    /**
     * Gets the cropped height of the internal image, which is its normal height minus
     * the top and bottom internal crop amounts.
     * 
     * @return the cropped height of the image, or <code>-1</code> if the internal
     *         image is <code>null</code>.
     */
    public int croppedHeight() {
        return (imgValid() ? this.img.getHeight() - (this.cropT + this.cropB) : -1);
    }
    
    /**
     * Checks if the image would be cropped at all on any side based on the current
     * internal crop amounts.
     * 
     * @return <code>true</code> if the image would be cropped on any side using the
     *         internal crop amounts; <code>false</code> otherwise.
     * @see #cropLeft()
     * @see #cropRight()
     * @see #cropTop()
     * @see #cropBottom()
     */
    public boolean canCrop() {
        System.err.println("Checking if croppable: Crop Amounts (L/T/R/B): " + this.cropL + "/" + this.cropT + "/" + this.cropR + "/" + this.cropB);
        return (this.cropL != 0 || this.cropR != 0 || this.cropT != 0 || this.cropB != 0);
    }
    
    /**
     * Sets the internal crop amounts using the passed ints.
     * 
     * @param l - the left side crop amount.
     * @param r - the right side crop amount.
     * @param t - the top side crop amount.
     * @param b - the bottom side crop amount.
     */
    public void setCrop(final int l, final int r, final int t, final int b) {
        this.cropL = l;
        this.cropR = r;
        this.cropT = t;
        this.cropB = b;
    }
    
    /**
     * Gets the left internal crop amount.
     * 
     * @return an int with the left crop amount.
     */
    public int cropLeft() {
        return this.cropL;
    }
    
    /**
     * Gets the right internal crop amount.
     * 
     * @return an int with the right crop amount.
     */
    public int cropRight() {
        return this.cropR;
    }
    
    /**
     * Gets the top internal crop amount.
     * 
     * @return an int with the top crop amount.
     */
    public int cropTop() {
        return this.cropT;
    }
    
    /**
     * Gets the bottom internal crop amount.
     * 
     * @return an int with the bottom crop amount.
     */
    public int cropBottom() {
        return this.cropB;
    }
}
