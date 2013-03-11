package org.dwfa.bpa.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

/**
 * Class for accessing application properties, loaded from a file (usually
 * generated from a Mojo).  Provides a singleton for all application
 * classes to access the properties after being loaded once.
 * 
 * @author ocarlsen
 */
public final class AppInfo {

    private static final Properties APP_INFO = new Properties();
    
    private static boolean loaded = false;
    
    /**
     * Singleton to access application properties.  If properties have not been
     * loaded yet, an {@link IllegalStateException} will be thrown.
     */
    public static Properties getProperties() {
        // Throw IllegalStateException if not loaded yet.
        if (! loaded) {
            throw new IllegalStateException("Please load properties first!");
        }
        
        return APP_INFO;
    }
    
    /**
     * Convenience method for getting a property from the internal instance.
     * If properties have not been loaded yet, an {@link IllegalStateException}
     * will be thrown.
     * @param key The property to look up. 
     */
    public static String getProperty(String key) {
        return getProperties().getProperty(key);
    }
    
    /**
     * Load properties from the specified file into a {@link Properties} object,
     * and return it when complete.
     * If properties have already been loaded, an {@link IllegalStateException}
     * will be thrown.
     * @param appInfoPropertiesFile  An XML file containing the application properties.
     * @throws IOException 
     * @throws FileNotFoundException 
     * @throws InvalidPropertiesFormatException 
     */
    public static Properties loadProperties(File appInfoPropertiesFile)
            throws InvalidPropertiesFormatException, FileNotFoundException, IOException {
        // Throw IllegalStateException if already loaded.
        if (loaded) {
            throw new IllegalStateException("Properties have already been loaded!");
        }

        // Load properties from file.
        APP_INFO.loadFromXML(new FileInputStream(appInfoPropertiesFile));
 
        // Update state to prevent reloads in future.
        loaded = true;
        
        return APP_INFO;
    }
    
    /**
     * Convenience method for loading properties from the specified file,
     * parent directory.
     * @throws IOException 
     * @throws FileNotFoundException 
     * @throws InvalidPropertiesFormatException 
     */
    public static Properties loadProperties(File parentDir, String propertiesFilename)
            throws InvalidPropertiesFormatException, FileNotFoundException, IOException {
        File appInfoPropertiesFile = new File(parentDir, propertiesFilename);
        return loadProperties(appInfoPropertiesFile);
    }
   
    /**
     * Helpful for testing outside an editor bundle.
     */
    public static void main(String[] args) 
            throws InvalidPropertiesFormatException, FileNotFoundException, IOException {
        // Configure image from command-line args.
        String propertiesFilename = args[0];
        File propertiesFile = new File(propertiesFilename);
        
        Properties p = loadProperties(propertiesFile);
        System.out.println(p.getProperty("version"));
    }
}