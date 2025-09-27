package dev.mwhitney.gui.popup.interfaces;

/**
 * An interface for receiving selections.
 * 
 * @author mwhitney57
 * @since 0.9.4
 */
@FunctionalInterface
public interface SelectionReceiver {
    /**
     * Called when a selection is received. The passed int represents the index of
     * the option or button which was selected.
     * 
     * @param button - the int index of the option or button which was pressed.
     */
    public void selected(int button);
}
