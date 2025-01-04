package dev.mwhitney.gui;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 * A helper class for utilizing {@link JOptionPane} methods with an
 * always-on-top dialog window.
 * 
 * @author mwhitney57
 */
public class TopDialog {
    /**
     * Generates an always-on-top parent JDialog and returns it for use.
     * This method does not automatically dispose of the JDialog.
     * This must be done manually.
     * 
     * @return an always-on-top JDialog.
     */
    private static JDialog genTopParent() {
        final JDialog parent = new JDialog();
        parent.setAlwaysOnTop(true);
        return parent;
    }
    
    /**
     * Shows an always-on-top message dialog.
     * Reference {@link JOptionPane} for documentation.
     * 
     * @param message - the message to display.
     * @param title - the title of the displayed dialog.
     * @param messageType - the type of dialog message to display.
     * @see {@link JOptionPane#showMessageDialog(Component, Object, String, int)}
     */
    public static void showMsg(Object message, String title, int messageType) {
        showMsg(message, title, messageType, null);
    }
    
    /**
     * Shows an always-on-top message dialog.
     * Reference {@link JOptionPane} for documentation.
     * 
     * @param message - the message to display.
     * @param title - the title of the displayed dialog.
     * @param messageType - the type of dialog message to display.
     * @param icon - the icon to display inside of the dialog.
     * @see {@link JOptionPane#showMessageDialog(Component, Object, String, int, Icon)}
     */
    public static void showMsg(Object message, String title, int messageType, Icon icon) {
        final JDialog parent = genTopParent();
        JOptionPane.showMessageDialog(parent, message, title, messageType, icon);
        parent.dispose();
    }
    
    /**
     * Shows an always-on-top confirmation dialog.
     * Reference {@link JOptionPane} for documentation.
     * 
     * @param message - the message to display.
     * @param title - the title of the displayed dialog.
     * @param optionType - the type of dialog option to display.
     * @see {@link JOptionPane#showConfirmDialog(Component, Object, String, int)}
     */
    public static int showConfirm(Object message, String title, int optionType) {
        final JDialog parent = genTopParent();
        final int value = JOptionPane.showConfirmDialog(parent, message, title, optionType);
        parent.dispose();
        return value;
    }
    
    /**
     * Shows an always-on-top input dialog.
     * Reference {@link JOptionPane} for documentation.
     * 
     * @param message - the message to display.
     * @see {@link JOptionPane#showInputDialog(Component, Object)}
     */
    public static String showInput(Object message) {
        final JDialog parent = genTopParent();
        final String value = JOptionPane.showInputDialog(parent, message);
        parent.dispose();
        return value;
    }
}
