package dev.mwhitney.main;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import dev.mwhitney.listeners.PropertyListener;

/**
 * Manages the properties and configuration of PiPAA.
 * 
 * @author mwhitney57
 */
public class PropertiesManager {
    /** A mediator used across the entire application for changing properties. */
    public static PropertyListener MEDIATOR = null;
    
    /** Manages the properties. */
    private Properties props;
    /** Writes properties to the configuration file. */
    private FileWriter writer;
    /** Reads properties from the configuration file. */
    private FileReader reader;
    
    /** A <tt>String</tt> containing the properties file name. */
    private static final String CONFIG_FILE      = Initializer.APP_FOLDER + "/config.properties";
    /** A <tt>String</tt> containing the properties file description. */
    private static final String CONFIG_FILE_DESC = Initializer.APP_NAME_SHORT + " Configuration\nDon't make modifications manually unless you know what you're doing.";
    
    /**
     * Constructs the PropertiesManager.
     */
    public PropertiesManager() {
        setupProperties();
    }
    
    /**
     * Performs setup for the PropertiesManager. This creates the missing directory
     * and/or file, and it also loads properties from the file if it already
     * existed.
     */
    private void setupProperties() {
        // Create the file/directory if it doesn't exist.
        final File file = new File(CONFIG_FILE);
        if (!file.exists()) {
            file.getParentFile().mkdir();
            try {
                file.createNewFile();
            } catch (IOException | SecurityException e) { e.printStackTrace(); }
        }

        // Read the properties from the configuration file.
        this.props = new Properties();
        readPropertiesFile();
    }
    
    /**
     * Writes the current properties to the configuration file.
     */
    public void writeToPropertiesFile() {
        if (props != null) {
            setPropertiesWriter();
            try (final FileWriter fw = writer) {
                props.store(fw, CONFIG_FILE_DESC);
                fw.close();
            } catch (IOException ioe) { ioe.printStackTrace(); }
            System.out.println("Wrote to props.");
        }
    }
    
    /**
     * Reads the properties from the configuration file.
     * @return the Properties object containing the loaded properties.
     */
    public Properties readPropertiesFile() {
        setPropertiesReader();
        try (final FileReader fr = reader) {
            props.clear();
            props.load(fr);
            fr.close();
            return props;
        } catch(IOException | IllegalArgumentException | NullPointerException e) { 
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Sets the default configuration value for the passed property.
     * 
     * @param prop - the PiPProperty to reset to default.
     * @return <code>true</code> if the property has a default value and was set; <code>false</code> otherwise.
     */
    public boolean setDefault(final PiPProperty prop) {
        final String defValue = prop.stock();
        if (defValue != null) {
            set(prop.toString(), defValue);
            return true;
        }
        return false;
    }
    
    /**
     * Sets the reader that reads properties from the configuration file.
     */
    public void setPropertiesReader() {
        try {
            this.reader = new FileReader(CONFIG_FILE);
        } catch(Exception e) {}
    }
    
    /**
     * Sets the writer that writes properties to the configuration file.
     */
    public void setPropertiesWriter() {
        try {
            this.writer = new FileWriter(CONFIG_FILE);
        } catch(Exception e) {}
    }
    
    /**
     * Checks if the property under the passed String key exists in the properties.
     * 
     * @param propKey - a String with the property key.
     * @return <code>true</code> if a property under the passed property key exists;
     *         <code>false</code> otherwise.
     */
    public boolean has(final String propKey) {
        return this.props == null ? false : this.props.containsKey(propKey);
    }
    
    /**
     * Checks if the passed {@link PiPProperty} exists in the properties.
     * 
     * @param propKey - a {@link PiPProperty} key.
     * @return <code>true</code> if the passed property exists; <code>false</code>
     *         otherwise.
     */
    public boolean has(final PiPProperty prop) {
        return prop == null ? false : has(prop.toString());
    }
    
    /**
     * Gets the property under the passed String key. This method returns
     * <code>null</code> if the properties are not set up or if there is no property
     * under the passed key.
     * 
     * @param propKey - a String with the property key.
     * @return a String with the value corresponding to the passed property key;
     *         <code>null</code> if the property is not found.
     */
    public String get(final String propKey) {
        return this.props == null ? null : this.props.getProperty(propKey);
    }
    
    /**
     * Gets the value for the passed property. This method returns <code>null</code>
     * if the properties are not set up or if the passed property is null.
     * 
     * @param prop - the PiPProperty to get.
     * @return a String with the value corresponding to the passed property;
     *         <code>null</code> if the property is not found.
     */
    public String get(final PiPProperty prop) {
        return prop == null ? null : get(prop.toString());
    }
    
    /**
     * Sets the property using the passed key and value Strings then writes it to
     * the configuration file.
     * 
     * @param key   - the String key part of the property.
     * @param value - the String value part of the property.
     */
    public void set(final String key, final String value) {
        this.props.setProperty(key, value);
        writeToPropertiesFile();
    }
    
    /**
     * Gets the properties.
     * @return the Properties object containing the properties.
     */
    public Properties getProperties() {
        return this.props;
    }
    
    /**
     * Gets the writer that writes the properties to the configuration file.
     * @return the FileWriter that writes the properties.
     */
    public FileWriter getFileWriter() {
        return this.writer;
    }
    
    /**
     * Gets the reader that reads the properties from the configuration file.
     * @return the FileReader that reads the properties.
     */
    public FileReader getFileReader() {
        return this.reader;
    }
}
