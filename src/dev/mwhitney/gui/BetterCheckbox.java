package dev.mwhitney.gui;

import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;

/**
 * An incredibly-basic extension upon JCheckBox that changes a few default settings/behaviors.
 * 
 * @author mwhitney57
 */
public class BetterCheckbox extends JCheckBox {
    /** The randomly-generated, unique serial ID for BetterCheckboxes. */
    private static final long serialVersionUID = -1593603022271144819L;

    /**
     * Creates a BetterCheckbox with the passed label, default checked state, and text font.
     * 
     * @param label - a String with the checkbox's label.
     * @param checked - a boolean for the default checked/enabled state of the checkbox.
     * @param f - the Font of the checkbox's label.
     */
    public BetterCheckbox(String label, boolean checked, Font f) {
        super(label, checked);
        
        setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(BetterCheckbox.class.getResource("/dev/mwhitney57/images/iconUICheck.png"))));
        setSelectedIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(BetterCheckbox.class.getResource("/dev/mwhitney57/images/iconUICheckFill.png"))));
        setFocusPainted(false);
        if (f != null)
            setFont(f);
    }
}
