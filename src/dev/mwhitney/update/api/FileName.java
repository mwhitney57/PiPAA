package dev.mwhitney.update.api;

/**
 * A filename which is automatically separated by name and file extension. This
 * class automates this simple, yet repetitive, task. It also provides methods
 * to get those specific parts. In the case of the file extension, the
 * accompanying period may be excluded via {@link #getExt()}, or included via
 * {@link #getDotExt()}.
 * <p>
 * This class does not treat compound extensions as one extension, such as with
 * a file named: {@code file.tar.gz}. The name would be {@code file.tar}, and
 * the extension would be {@code gz}. This is intentional. Everything after the
 * <b>last period</b> is treated as the extension.
 * 
 * @author mwhitney57
 * @since 0.9.5
 */
public class FileName {
    /**
     * The raw String that was provided at construction. The only case where this
     * won't match that passed String is if it was <code>null</code>, which this
     * String will never be. However, it may be empty.
     */
    private String raw;
    /** The name portion of the filename. */
    private String name;
    /** The file extension portion of the filename. */
    private String ext;
    
    /**
     * Creates a new {@link FileName} using the passed String.
     * 
     * @param name - the String filename representation.
     */
    public FileName(String name) {
        // Ensure name is non-null then keep its raw value.
        if (name == null) name = "";
        this.raw = name;
        
        // Determine the last period (dot) index.
        final int lastDotIndex = name.lastIndexOf('.');
        if (lastDotIndex >= 0) {
            // Dot found, separate name and extension.
            this.name = name.substring(0, lastDotIndex);
            this.ext  = name.substring(lastDotIndex + 1);
        } else {
            // Dot not found. Name is entire string. No extension.
            this.name = name;
            this.ext  = "";
        }
    }
    
    /**
     * Checks if there is a name, meaning it is non-<code>null</code> and not blank.
     * 
     * @return <code>true</code> if there's a name; <code>false</code> otherwise.
     */
    public boolean hasName() {
        return name != null && !name.isBlank();
    }
    
    /**
     * Gets the name, not to be confused with the full filename which would include
     * the extension.
     * 
     * @return a String with the name portion of the filename.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Checks if there is an extension, meaning it is non-<code>null</code> and not
     * blank.
     * 
     * @return <code>true</code> if there's an extension; <code>false</code>
     *         otherwise.
     */
    public boolean hasExt() {
        return ext != null && !ext.isBlank();
    }
    
    /**
     * Gets the extension.
     * <p>
     * <b>Example:</b> <code>jar</code>
     * 
     * @return a String with the extension portion of the filename.
     */
    public String getExt() {
        return this.ext;
    }
    
    /**
     * Gets the extension, including the period prefix before it.
     * <p>
     * <b>Example:</b> <code>.jar</code>
     * 
     * @return a String with the extension portion of the filename, including the
     *         period prefix.
     */
    public String getDotExt() {
        return hasExt() ? "." + this.ext : "";
    }
    
    @Override
    public String toString() {
        return this.raw;
    }
}
