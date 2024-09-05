package dev.mwhitney.gui;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.LayoutManager;

import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

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
        setUI(new BasicTabbedPaneUI() {
            // Do nothing to avoid painting the border.
            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {};
            // Add horizontal space between tabs.
            @Override
            protected LayoutManager createLayoutManager() {
                return new BasicTabbedPaneUI.TabbedPaneLayout() {
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
