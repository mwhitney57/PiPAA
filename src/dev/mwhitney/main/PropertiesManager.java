package dev.mwhitney.main;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

import javax.swing.JOptionPane;

import dev.mwhitney.gui.popup.TopDialog;
import dev.mwhitney.listeners.PropertyListener;

/**
 * Manages the properties and configuration of PiPAA.
 * 
 * @author mwhitney57
 */
public class PropertiesManager {
    /**
     * <b>DO NOT USE: TODO Scheduled for removal.</b>
     * Having public static mutable objects is undesirable, and given there is only one usage of this at the time
     * of writing this comment, it should be removed and the solution should be reworked.
     * <p>
     * A mediator {@link PropertyListener} used across the entire application for changing properties.
     */
    public static PropertyListener mediator = null;
    
    /** Manages the properties loaded in memory. Not guaranteed to match what is stored in the properties file at any given time. */
    private final Properties props;
    
    /** A String containing the properties file name. */
    private static final String CONFIG_FILE      = Initializer.APP_FOLDER + "/config.properties";
    /** A String containing the properties file description. */
    private static final String CONFIG_FILE_DESC = Initializer.APP_NAME_SHORT + " Configuration\nDon't make modifications manually unless you know what you're doing.";
    
    /**
     * Constructs the PropertiesManager. Automatically performs internal properties
     * setup via {@link #setupPropertiesFile()}.
     */
    public PropertiesManager() {
        this.props = new Properties();
        setupPropertiesFile();
    }
    
    /**
     * Performs setup for the PropertiesManager. This creates the missing directory
     * and/or file, and it also loads properties from the file, if it exists. If the
     * properties file cannot be created, the user will be notified before exiting.
     */
    private void setupPropertiesFile() {
        // Create the file/directory if it doesn't exist.
        final File file = new File(CONFIG_FILE);
        if (!file.exists()) {
            try {
                // This method might fail if the parent folder(s) to PiPAA's don't exist.
                file.getParentFile().mkdir();
                file.createNewFile();
            } catch (IOException | SecurityException e) {
                // Since the properties file is essential for the application, notify the user of the error and close.
                e.printStackTrace();
                TopDialog.showMsg("Cannot access application properties file. To troubleshoot, ensure this folder exists and can be modified:"
                        + "\n" + Initializer.APP_FOLDER
                        + "\n\nIf this issue persists, please contact the developer:"
                        + "\nhttps://github.com/mwhitney57/PiPAA/issues", "Fatal error occurred.", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        }

        // Read the properties from the configuration file.
        readPropertiesFile();
    }
    
    /**
     * Reads the properties from the configuration file and stores them internally
     * in memory.
     */
    public void readPropertiesFile() {
        try (final FileReader fr = new FileReader(CONFIG_FILE)) {
            props.clear();
            props.load(fr);
            fr.close();
        } catch (IOException | IllegalArgumentException | NullPointerException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Writes the current properties to the configuration file.
     */
    public void writeToPropertiesFile() {
        try (final FileWriter fw = new FileWriter(CONFIG_FILE)) {
            props.store(fw, CONFIG_FILE_DESC);
            fw.close();
        } catch (IOException ioe) { ioe.printStackTrace(); }
        System.out.println("Wrote to props.");
    }
    
    /**
     * Sets the default configuration value for the passed property.
     * 
     * @param prop - the PiPProperty to reset to default.
     * @return <code>true</code> if the property has a default value and was set;
     *         <code>false</code> otherwise.
     */
    public boolean setDefault(final PiPProperty prop) {
        final String defValue = prop.stock();
        if (defValue != null) {
            set(prop, defValue);
            return true;
        }
        return false;
    }
    
    /**
     * Checks if the passed String property key exists in the properties in memory.
     * 
     * @param propKey - a String with the property key.
     * @return <code>true</code> if a property under the passed property key exists;
     *         <code>false</code> otherwise.
     */
    public boolean has(final String propKey) {
        return propKey == null ? false : this.props.containsKey(propKey);
    }

    /**
     * Checks if the passed {@link PiPProperty} exists in the properties in memory.
     * 
     * @param propKey - a {@link PiPProperty} key.
     * @return <code>true</code> if the passed property exists; <code>false</code>
     *         otherwise.
     */
    public boolean has(final PiPProperty prop) {
        return prop == null ? false : has(prop.toString());
    }
    
    /**
     * Gets the value for the passed property in memory. This method returns
     * <code>null</code> if the passed String is <code>null</code> or there is no
     * property under the passed key.
     * 
     * @param propKey - a String with the property key.
     * @return a String with the value corresponding to the passed property key;
     *         <code>null</code> if the property is not found in memory.
     */
    public String get(final String propKey) {
        return propKey == null ? null : this.props.getProperty(propKey);
    }
    
    /**
     * Gets the value for the passed property in memory. This method returns
     * <code>null</code> if the passed String is <code>null</code> or there is no
     * property under the passed key.
     * 
     * @param prop - the PiPProperty to get.
     * @return a String with the value corresponding to the passed property;
     *         <code>null</code> if the property is not found in memory.
     */
    public String get(final PiPProperty prop) {
        return prop == null ? null : get(prop.toString());
    }
    
    /**
     * Sets the property using the passed key and value Strings then writes it to
     * the configuration file.
     * <p>
     * The String key must be non-<code>null</code>. Passing a <code>null</code> key
     * will therefore do nothing. However, the passed value may be
     * <code>null</code>, which equates to an empty String.
     * 
     * @param key   - the String key part of the property.
     * @param value - the String value part of the property.
     */
    public void set(final String key, final String value) {
        if (key == null) return;
        this.props.setProperty(key, Objects.toString(value, ""));
        writeToPropertiesFile();
    }
    
    /**
     * Sets the property using the passed {@link PiPProperty} key and value Strings
     * then writes it to the configuration file.
     * <p>
     * The key must be non-<code>null</code>. Passing a <code>null</code> key will
     * therefore do nothing. However, the passed value may be <code>null</code>,
     * which equates to an empty String.
     * 
     * @param key   - the {@link PiPProperty} key part of the property.
     * @param value - the String value part of the property.
     */
    public void set(final PiPProperty prop, final String value) {
        if (prop != null) set(prop.toString(), value);
    }
    
    /**
     * Removes the property using the passed String key then writes the change to the
     * configuration file.
     * <p>
     * This method is similar to {@link #set(String, String)}, but removes the key
     * entirely instead of replacing its value. Importantly, this method also writes
     * the changes to the properties file immediately. Solely removing the key from
     * the properties will only affect what's in memory, not the properties file:
8     * <pre>
     * PropertiesManager.getProperties().remove(String)
     * </pre>
     * 
     * @param key - the String key of the property.
     */
    public void remove(final String key) {
        if (key == null) return;
        this.props.remove(key);
        writeToPropertiesFile();
    }
    
    /**
     * Removes the property using the passed {@link PiPProperty} key then writes the
     * change to the configuration file.
     * <p>
     * This method is similar to {@link #set(PiPProperty, String)}, but removes the key
     * entirely instead of replacing its value. Importantly, this method also writes
     * the changes to the properties file immediately. Solely removing the key from
     * the properties will only affect what's in memory, not the properties file:
     * <pre>
     * PropertiesManager.getProperties().remove(String)
     * </pre>
     * 
     * @param key - the {@link PiPProperty} key.
     */
    public void remove(final PiPProperty prop) {
        if (prop != null) remove(prop.toString());
    }
    
    /**
     * Gets the properties.
     * <p>
     * Remember that <b>any changes to the properties directly will remain in
     * memory</b> until written to the properties file via
     * {@link #writeToPropertiesFile()} or one of the {@link PropertiesManager}'s
     * methods, such as {@link #set(String, String)}.
     * 
     * @return the Properties object containing the properties.
     * @see {@link #set(String, String)} to set properties and write the changes to
     *      the file.
     * @see {@link #remove(String)} to remove properties by their key and write the
     *      changes to the file.
     */
    public Properties getProperties() {
        return this.props;
    }
}
