package dev.mwhitney.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import dev.mwhitney.main.PiPProperty.THEME_OPTION;
import dev.mwhitney.main.PiPProperty.THEME_OPTION.COLOR;
import net.miginfocom.swing.MigLayout;

/**
 * An easy-to-use extension upon the {@link TopDialog} with simpler styling, and
 * easy, automatic dismissal.
 * 
 * @author mwhitney57
 */
public class EasyTopDialog {
    /**
     * Closes the passed {@link JDialog} by making it unfocusable, invisible, and
     * then disposing of it.
     * 
     * @param window - the {@link JDialog} to close.
     */
    private static void close(JDialog window) {
        window.setVisible(false);
        window.dispose();
    }
    
    /**
     * Generates an easy, always-on-top parent {@link JDialog} and returns it for
     * use. This method does not automatically set the dialog to be visible, nor
     * does it automatically dispose of it. Dispose of the generated {@link JDialog}
     * via {@link #close(JDialog)}.
     * 
     * @param message - the String message to be displayed in the dialog.
     * @param theme   - the {@link THEME_OPTION} to use for the dialog.
     * @return an easy, always-on-top JDialog.
     */
    private static JDialog genTopParent(String message, THEME_OPTION theme) {
        final JDialog parent = new JDialog();
        parent.setAlwaysOnTop(true);
        parent.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
        parent.setUndecorated(true);
        parent.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        parent.setResizable(false);
//        parent.setFocusable(false);
//        parent.setFocusableWindowState(false);
        parent.setBackground(new Color(0, 0, 0, 0));
        parent.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent e) { close(parent); }
        });
        parent.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e)         { close(parent); }
        });
        final MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e)     { close(parent); }
        };
        parent.addMouseListener(mouseAdapter);
        
        final BetterPanel panel = new BetterPanel(new MigLayout("fill, insets 5 10 5 10")).useDropShadow();
        panel.setBackground(theme.color(COLOR.BG));
        panel.setBorderColor(theme.color(COLOR.BG_ACCENT));
        panel.setRoundedArc(30);
        final BetterTextArea text = new BetterTextArea(message);
        text.setFocusable(false);
        text.setBackground(theme.color(COLOR.BG));
        text.setForeground(theme.color(COLOR.TXT));
        text.setFont(new Font("Dialog", Font.BOLD, 16));
        text.addMouseListener(mouseAdapter);
        panel.add(text);
        
        parent.setContentPane(panel);
        parent.pack();
        return parent;
    }
    
    /**
     * Shows an easy always-on-top message dialog. The easy dialog will remain open
     * for a default of ~<code>1500</code>ms before automatically closing. The
     * dialog can be closed early by the user clicking or pressing a key on it. It
     * will also close when it loses focus.
     * 
     * @param parent  - the {@link JFrame} to display the dialog relative to.
     * @param message - the String message to be displayed in the dialog.
     * @param theme   - the {@link THEME_OPTION} to use for the dialog.
     * @see {@link #showMsg(JFrame, String, THEME_OPTION, int)} for specifying a
     *      custom lifespan duration.
     */
    public static void showMsg(JFrame parent, String message, THEME_OPTION theme) {
        showMsg(parent, message, theme, 1500, true);
    }
    
    /**
     * Shows an easy always-on-top message dialog. The easy dialog will remain open
     * for the passed <code>lifespan</code>ms before automatically closing. The
     * dialog can be closed early by the user clicking or pressing a key on it. It
     * will also close when it loses focus.
     * 
     * @param parent   - the {@link JFrame} to display the dialog relative to.
     * @param message  - the String message to be displayed in the dialog.
     * @param theme    - the {@link THEME_OPTION} to use for the dialog.
     * @param lifespan - the lifespan of the message dialog, in milliseconds.
     * @param
     * @see {@link #showMsg(JFrame, String, THEME_OPTION, int)} for specifying a
     *      custom lifespan duration.
     */
    public static void showMsg(JFrame parent, String message, THEME_OPTION theme, int lifespan) {
        showMsg(parent, message, theme, lifespan, true);
    }
    
    /**
     * Shows an easy always-on-top message dialog. The easy dialog will remain open
     * for a default of ~<code>1500</code>ms before automatically closing. The
     * dialog can be closed early by the user clicking or pressing a key on it. It
     * will also close when it loses focus.
     * 
     * @param parent     - the {@link JFrame} to display the dialog relative to.
     * @param message    - the String message to be displayed in the dialog.
     * @param theme      - the {@link THEME_OPTION} to use for the dialog.
     * @param grabsFocus - a boolean for whether or not the dialog grabs focus when
     *                   it shows.
     * @param
     * @see {@link #showMsg(JFrame, String, THEME_OPTION, int)} for specifying a
     *      custom lifespan duration.
     */
    public static void showMsg(JFrame parent, String message, THEME_OPTION theme, boolean grabsFocus) {
        showMsg(parent, message, theme, 1500, grabsFocus);
    }
    
    /**
     * Shows an easy always-on-top message dialog. The easy dialog will remain open
     * for the passed <code>lifespan</code>ms before automatically closing. The
     * dialog can be closed early by the user clicking or pressing a key on it. It
     * will also close when it loses focus.
     * <p>
     * <b>Note:</b> If the passed boolean argument is <code>false</code>, then
     * keyboard presses or clicking on other windows will not close the dialog
     * early. The only remaining method to closing it early would be directly
     * clicking on it.
     * 
     * @param parent     - the {@link JFrame} to display the dialog relative to.
     * @param message    - the message to be displayed in the dialog.
     * @param theme      - the {@link THEME_OPTION} to use for the dialog.
     * @param lifespan   - the lifespan of the message dialog, in milliseconds.
     * @param grabsFocus - a boolean for whether or not the dialog grabs focus when
     *                   it shows.
     * @see {@link #showMsg(JFrame, String, THEME_OPTION)} for using the default
     *      duration of <code>1500</code>ms.
     */
    public static void showMsg(JFrame parent, String message, THEME_OPTION theme, int lifespan, boolean grabsFocus) {
        SwingUtilities.invokeLater(() -> {
            final JDialog window = genTopParent(message, theme);
            window.setLocationRelativeTo(parent);
            if (!grabsFocus) window.setFocusableWindowState(false);
            window.setVisible(true);
            final Timer t = new Timer(lifespan, (e) -> close(window));
            t.setRepeats(false);
            t.start();
        });
    }
}
