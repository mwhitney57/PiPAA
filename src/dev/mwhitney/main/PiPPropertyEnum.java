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
     * Gets the property or property option's String label.
     * 
     * @return a String with the label.
     */
    public default String label() { return ""; }
    /**
     * Gets the property or property option's label, <b>as well as all other labels
     * within its enum class</b>.
     * <p>
     * Though it would make more sense for this method to be attached to the class
     * itself, there does not seem to be a good way to do it.
     * 
     * @return a String[] with the label and all of its siblings' labels.
     */
    public default String[] labels() {
        final PiPPropertyEnum<?>[] options = this.getClass().getEnumConstants();
        final String[] labels = new String[options.length];
        for (int i = 0; i < labels.length; i++) {
            labels[i] = options[i].label();
        }
        return labels;
    }
    /**
     * Gets the property or property option's String description.
     * 
     * @return a String with the description.
     */
    public default String description() { return ""; }
}
