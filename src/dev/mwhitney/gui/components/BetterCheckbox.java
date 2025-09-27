package dev.mwhitney.gui.components;

import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;

import dev.mwhitney.resources.AppRes;

/**
 * An incredibly-basic extension upon JCheckBox that changes a few default
 * settings/behaviors.
 * 
 * @author mwhitney57
 */
public class BetterCheckbox extends JCheckBox {
    /** The randomly-generated, unique serial ID for BetterCheckboxes. */
    private static final long serialVersionUID = -1593603022271144819L;
    
    /** A custom {@link ImageIcon} for the checkbox. */
    private static final ImageIcon ICON     = new ImageIcon(BetterCheckbox.class.getResource(AppRes.ICON_CHECKBOX));
    /** A custom {@link ImageIcon} for the checkbox when it's selected. */
    private static final ImageIcon ICON_SEL = new ImageIcon(BetterCheckbox.class.getResource(AppRes.ICON_CHECKBOX_SELECTED));

    /**
     * Creates a BetterCheckbox with the passed label, default checked state, and
     * text font.
     * 
     * @param label   - a String with the checkbox's label.
     * @param checked - a boolean for the default checked/enabled state of the
     *                checkbox.
     * @param f       - the Font of the checkbox's label.
     */
    public BetterCheckbox(String label, boolean checked, Font f) {
        super(label, checked);

        setIcon(ICON);
        setSelectedIcon(ICON_SEL);
        setFocusPainted(false);
        if (f != null) setFont(f);
    }
}
