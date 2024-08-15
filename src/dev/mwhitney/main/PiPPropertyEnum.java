package dev.mwhitney.main;

/**
 * An interface for PiPAA property-related enums.
 * <p>
 * <b>Do not implement this interface in standard classes</b>, as it is only
 * intended to be used with enums. Certain methods, such as
 * {@link #matchAny(String)} will fail if the implementing class is not an enum.
 * 
 * @param <E> the enum that is implementing this interface.
 * @author mwhitney57
 */
public interface PiPPropertyEnum<E extends Enum<E>> extends PiPEnum<E> {
    /**
     * Gets the property or property option's String description.
     * 
     * @return a String with the description.
     */
    public default String description() { return ""; }
}
