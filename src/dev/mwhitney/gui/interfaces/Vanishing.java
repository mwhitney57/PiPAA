package dev.mwhitney.gui.interfaces;

/**
 * An interface that defines basic methods for revealing and concealing an
 * object.
 * <p>
 * Designed for Java Swing, the provided {@link #reveal()} and
 * {@link #conceal()} methods can simplify visibility calls while performing
 * additional logic.
 * <p>
 * However, any class can implement this interface, not just GUI components.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public interface Vanishing {
    /**
     * Reveals this object, making it visible. Typically used with Java Swing to
     * reveal a component in a window.
     */
    public void reveal();
    /**
     * Conceals this object, making it invisible. Typically used with Java Swing to
     * conceal a component in a window.
     */
    public void conceal();
}
