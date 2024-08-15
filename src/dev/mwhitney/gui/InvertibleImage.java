package dev.mwhitney.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

/**
 * A class which stores both a regular Image, as well as a calculated inverse of that Image.
 * 
 * @author mwhitney57
 */
public class InvertibleImage {
    /** The standard Image. */
    private Image img;
    /** The inverted version of the Image. */
    private Image imgInv;
    
    /**
     * Creates a new InvertibleImage which stores the normal Image passed to the
     * constructor. It also sets and stores the inverse of that image.
     * 
     * @param img - the Image to store, alongside its inverse.
     */
    public InvertibleImage(Image img) {
        this.img = img;
        
        setInverse();
    }
    
    /**
     * Sets the inverse representation of the Image.
     */
    private void setInverse() {
        BufferedImage inputFile = null;
        
        // If the image is not already a BufferedImage, convert it to be such.
        if (!(img instanceof BufferedImage)) {
            // Create a buffered image with transparency
            final ImageIcon icon = new ImageIcon(img);
            final BufferedImage buffImg = new BufferedImage(icon.getImage().getWidth(null), icon.getImage().getHeight(null), BufferedImage.TYPE_INT_ARGB);
            
            // Draw the image on to the buffered image
            final Graphics2D bGr = buffImg.createGraphics();
            bGr.drawImage(img, 0, 0, null);
            bGr.dispose();
            
            inputFile = buffImg;
        } else
            inputFile = (BufferedImage) img;
        
        // Inverts each pixel of the image, while respecting alpha values.
        for (int x = 0; x < inputFile.getWidth(); x++) {
            for (int y = 0; y < inputFile.getHeight(); y++) {
                final int rgba = inputFile.getRGB(x, y);
                Color pixColor = new Color(rgba, true);
                pixColor = new Color(255 + pixColor.getRed(),
                                     255 + pixColor.getGreen(),
                                     255 + pixColor.getBlue(),
                                     pixColor.getAlpha());
                inputFile.setRGB(x, y, pixColor.getRGB());
            }
        }
        
        this.imgInv = inputFile;
    }
    
    /**
     * Gets the standard Image.
     * 
     * @return the standard Image without any modifications.
     */
    public Image image() {
        return this.img;
    }
    
    /**
     * Gets the inverse representation of the standard Image.
     * 
     * @return the inverse Image.
     */
    public Image inverse() {
        return this.imgInv;
    }
}
