package dev.mwhitney.gui;

import java.awt.Font;

import javax.swing.JLabel;

/**
 * An incredibly-basic extension upon JLabel that changes a few default settings/behaviors.
 * 
 * @author mwhitney57
 */
public class BetterLabel extends JLabel {
    /** A randomly-generated, unique serial ID for BetterLabels. */
    private static final long serialVersionUID = -5093004319704976291L;
    
    /**
     * Creates a BetterLabel with the passed label and text font.
     * 
     * @param label - a String with the label text.
     * @param f - the Font of the label text.
     */
    public BetterLabel(String label, Font f) {
        super("<html>" + label + "</html>");
        
        setFont(f);
    }
}
