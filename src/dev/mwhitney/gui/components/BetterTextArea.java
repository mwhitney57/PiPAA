package dev.mwhitney.gui.components;

import javax.swing.JTextArea;

import dev.mwhitney.main.PiPProperty.THEME_OPTION;
import dev.mwhitney.resources.AppRes;

/**
 * An incredibly-basic extension upon JTextArea that changes a few default settings/behaviors.
 * 
 * @author mwhitney57
 */
public class BetterTextArea extends JTextArea {
    /** A randomly-generated, unique serial ID for BetterTextAreas. */
    private static final long serialVersionUID = 5875847404056500329L;
    
    /**
     * Creates a new BetterTextArea with the passed text.
     * 
     * @param text - a String with the text to display in the text area.
     */
    public BetterTextArea(String text) {
        super(text);
        
        this.setEditable(false);
        this.setBackground(THEME_OPTION.LIGHT_BG);
        this.setFont(AppRes.FONT_TEXT_COMPONENT);
    }
}
