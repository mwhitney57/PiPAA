package dev.mwhitney.gui.components.better;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.LayoutManager;

import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import dev.mwhitney.util.Loop;

/**
 * An incredibly-basic extension upon BetterTabbedPane that changes a few default settings/behaviors.
 * 
 * @author mwhitney57
 */
public class BetterTabbedPane extends JTabbedPane {
    /** A randomly-generated, unique serial ID for BetterTabbedPanes. */
    private static final long serialVersionUID = -1101549190177346722L;
    
    /**
     * Creates a BetterTabbedPane with the passed text font.
     * 
     * @param f - a Font to use for the tab labels.
     */
    public BetterTabbedPane(Font f) {
        super();
        
        setFont(f);
        setBorder(null);
        setFocusable(false);
        setOpaque(false);
        addMouseWheelListener(e -> {
            // Setup loop of tab indices, then progress forward or backward based on scroll direction.
            final Integer[] tabIndices = new Integer[this.getTabCount()];
            for (int i = 0; i < tabIndices.length; i++) {
                tabIndices[i] = i;
            }
            final Loop<Integer> loop = new Loop<>(tabIndices, this.getSelectedIndex());
            this.setSelectedIndex(e.getPreciseWheelRotation() < 0 ? loop.previous() : loop.next());
        });
        setUI(new BasicTabbedPaneUI() {
            // Do nothing to avoid painting the border.
            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {};
            // Disable the scroll buttons visible with SCROLL_TAB_LAYOUT.
            @Override
            protected JButton createScrollButton(int direction) {
                // Create an empty button with a preferred size of zero.
                final JButton btn = new JButton();
                btn.setPreferredSize(new Dimension());
                // Remove it later. Allow Swing code to do its thing, but undo some of its work.
                // This prevents empty tabs for being created for each button.
                SwingUtilities.invokeLater(() -> this.tabPane.remove(btn));
                return btn;
            }
            // Add horizontal space between tabs if using WRAP_TAB_LAYOUT.
            @Override
            protected LayoutManager createLayoutManager() {
                return getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT ? super.createLayoutManager()
                        : new BasicTabbedPaneUI.TabbedPaneLayout() {
                            @Override
                            protected void calculateTabRects(int tabPlacement, int tabCount) {
                                super.calculateTabRects(tabPlacement, tabCount);

                                final int space = 6;
                                final int indent = 4;
                                for (int i = 0; i < rects.length; i++) {
                                    rects[i].x += i * space + indent;
                                }
                            }
                        };
            }
        });
    }
}
