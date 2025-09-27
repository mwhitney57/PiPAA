package dev.mwhitney.gui.components.better;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicComboPopup;

/**
 * An incredibly-basic extension upon JComboBox that changes a few default settings/behaviors.
 * 
 * @author mwhitney57
 */
public class BetterComboBox extends JComboBox<String> {
    /** The randomly-generated, unique serial ID for BetterComboBoxes. */
    private static final long serialVersionUID = 2522890016214754338L;
    
    /** A set of Colors for the combo box. */
    private Color boxColor, boxBorderColor, selectionFGColor;

    /**
     * Creates a BetterComboBox with the passed options and text font.
     * 
     * @param options - a String[] with the item options.
     * @param f - the Font of the combo box's label.
     */
    public BetterComboBox(String[] options, final Font f) {
        super(options);
        
        this.boxColor         = Color.DARK_GRAY;
        this.boxBorderColor   = Color.BLACK;
        this.selectionFGColor = Color.WHITE;
        setBorder(null);
        setFocusable(false);
        if (f != null)
            setFont(f);
        
        // Set Renderer for Drop-Down List
        final DefaultListCellRenderer customRenderer = new DefaultListCellRenderer() {
            /** The randomly-generated, unique serial ID for this DefaultListCellRenderer. */
            private static final long serialVersionUID = -9151690154758121683L;
            
            @Override
            public void paint(Graphics g) {
                setBackground(boxColor);
                super.paint(g);
            }
            @SuppressWarnings("rawtypes")
            @Override
            public Component getListCellRendererComponent(JList list, Object value,
                                    int index, boolean isSelected, boolean cellHasFocus) {
                final JComponent comp = (JComponent) super.getListCellRendererComponent(list,
                            value, index, isSelected, cellHasFocus);

                list.setForeground(BetterComboBox.this.getForeground());
                if (selectionFGColor != null)
                    list.setSelectionForeground(selectionFGColor);
                list.setOpaque(false);
                    
                return comp;
            }
        };
        customRenderer.setBorder(new LineBorder(Color.RED, 4));
        setRenderer(customRenderer);
    }

    /**
     * Creates a BetterComboBox with the passed options and text font, then adds the
     * passed listener.
     * 
     * @param options - a String[] with the item options.
     * @param f       - the Font of the combo box's label.
     * @param al      - the {@link ActionListener} to add after construction.
     */
    public BetterComboBox(String[] options, final Font f, ActionListener al) {
        this(options, f);
        this.addActionListener(al);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        // Create Graphics and Rendering Hints
        final Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Select the Color Based on Pressed State
        g2d.setColor(this.boxColor);
        g2d.fillRoundRect(1, 1, getWidth()-2, getHeight()-2, 20, 20);
        
        // Paint Text within Rect.
        if (this.getSelectedItem() instanceof String) {
            final String txt = (String) this.getSelectedItem();
            g2d.setColor(getForeground());
            g2d.drawString(txt, 8, 7 + (getHeight()/2));
        }
        
        // Dispose of the Graphics2D Object
        g2d.dispose();
        
//        super.paintComponent(g); // Commented-out -- Don't paint and ONLY use custom paint solution.
    }
    
    @Override
    protected void paintBorder(Graphics g) {
        // Create Graphics and Rendering Hints
        final Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draws the Rounded Border
        g2d.setColor(this.boxBorderColor);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawRoundRect(2, 1, getWidth()-4, getHeight()-3, 10, 10);
        
        // Dispose of the Graphics2D Object
        g2d.dispose();
    }
    
    /**
     * Updates the BetterComboBox's drop-down selection menu's border to use the
     * current internal box color and box border color.
     */
    private void updateDropDownBorder() {
        if (getAccessibleContext().getAccessibleChild(0) instanceof BasicComboPopup) {
            final BasicComboPopup popup = (BasicComboPopup) getAccessibleContext().getAccessibleChild(0);
            popup.setBorder(BorderFactory.createCompoundBorder(new LineBorder(this.boxBorderColor, 2), new LineBorder(this.boxColor, 5)));
        }
    }
    
    /**
     * Sets the color of the combo box's background.
     * 
     * @param c - the Color to set.
     */
    public void setBoxColor(final Color c) {
        this.boxColor = c;
        updateDropDownBorder();
    }
    
    /**
     * Sets the color of the combo box's border.
     * 
     * @param c - the Color to set.
     */
    public void setBoxBorderColor(final Color c) {
        this.boxBorderColor = c;
        updateDropDownBorder();
    }
    
    /**
     * Sets the color of the drop-down menu's selection foreground in the combo box.
     * 
     * @param c - the Color to set.
     */
    public void setSelectionForeground(final Color c) {
        this.selectionFGColor = c;
    }
}
