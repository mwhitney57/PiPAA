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
        setOpaque(false);
        addMouseWheelListener(e -> changeTabByDirection((int) e.getPreciseWheelRotation()));
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
    
    /**
     * Creates an integer {@link Loop} which contains all of the valid indices, each
     * corresponding to a tab present in the {@link BetterTabbedPane}. The loop
     * starts at the position of the selected tab's index. This allows for easy
     * selection of a neighboring tab.
     * 
     * @return a {@link Loop} of integer indices corresponding to tabs in the pane.
     * @since 0.9.5
     */
    private Loop<Integer> getTabIndicesLoop() {
        // Setup loop of tab indices, then create loop with array starting at current tab selection.
        final Integer[] tabIndices = new Integer[this.getTabCount()];
        for (int i = 0; i < tabIndices.length; i++) {
            tabIndices[i] = i;
        }
        return new Loop<>(tabIndices, this.getSelectedIndex());
    }
    
    /**
     * Changes selection to a tab neighboring the current one, depending on the
     * direction. Negative numbers will select a previous tab. Any zero or positive
     * integer will cause the next tab to be selected.
     * 
     * @param direction - an int with the direction.
     * @since 0.9.5
     */
    public void changeTabByDirection(int direction) {
        final Loop<Integer> loop = getTabIndicesLoop();
        this.setSelectedIndex(direction < 0 ? loop.previous() : loop.next());
    }
    
    /**
     * Selects the previous tab that neighbors the one that is currently selected.
     * 
     * @since 0.9.5
     */
    public void selectPreviousTab() {
        changeTabByDirection(-1);
    }
    
    /**
     * Selects the next tab that neighbors the one that is currently selected.
     * 
     * @since 0.9.5
     */
    public void selectNextTab() {
        changeTabByDirection(1);
    }
}
