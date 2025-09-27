package dev.mwhitney.gui.components;

import java.awt.Insets;

import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import dev.mwhitney.main.PiPProperty.THEME_OPTION;
import dev.mwhitney.resources.AppRes;

/**
 * An incredibly-basic extension upon JTextPane that changes a few default settings/behaviors.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public class BetterTextPane extends JTextPane {
    /** A randomly-generated, unique serial ID. */
    private static final long serialVersionUID = -7200948484484711063L;

    /** Padding values for each side surrounding the text. */
    private static final int PAD_T = 10, PAD_B = 10, PAD_L = 5, PAD_R = 5;
    
    /**
     * Creates a new BetterTextPane with the passed text.
     * 
     * @param text - a String with the text to display in the text area.
     */
    public BetterTextPane(String text) {
        super();
        
        this.setText(text);
        this.setEditable(false);
        this.setBackground(THEME_OPTION.LIGHT_BG);
        this.setFont(AppRes.FONT_TEXT_COMPONENT);
        
        // Custom padding surrounding text.
        this.setMargin(new Insets(PAD_T, PAD_L, PAD_B, PAD_R));
        // JTextPane by default has a greater height for its minimum size, which we don't want.
        this.setMinimumSize(getPreferredSize());
        
        // Custom styling.
        final StyledDocument doc = this.getStyledDocument();
        final SimpleAttributeSet center = new SimpleAttributeSet();
        // Align text centrally.
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        // Give slightly more space above and below each text line.
        StyleConstants.setSpaceAbove(center, 2.0f);
        StyleConstants.setSpaceBelow(center, 2.0f);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);
    }
}
