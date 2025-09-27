package dev.mwhitney.gui.popup.interfaces;

/**
 * An interface for receiving selections with a specific value.
 * <p>
 * This interface extends {@link SelectionReceiver} but contains no logic for
 * {@link #selected(int)} by default, since {@link #valueSelected(Object)} is
 * intended to be a functional interface method and usage of both is less
 * likely.
 * 
 * @param <T> - the type of value to receive.
 * @author mwhitney57
 * @since 0.9.5
 */
@FunctionalInterface
public interface ValueReceiver<T> extends SelectionReceiver {
    @Override
    public default void selected(int button) {}
    /**
     * Called when a value is selected and received. The selected value is passed
     * when calling this method.
     * 
     * @param value - the value which was selected.
     */
    public void valueSelected(T value);
}
