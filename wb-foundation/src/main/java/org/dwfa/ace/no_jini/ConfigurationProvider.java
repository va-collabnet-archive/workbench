package org.dwfa.ace.no_jini;

/**
 * Dummy class to provide compile-time support for Jini references
 * added as part of merge from wb-toolkit_trek.
 * @author ocarlsen
 */
public class ConfigurationProvider {
    /**
     * Copied from {@code net.jini.config.ConfigurationProvider} object.
     * @return Always returns a new {@link Configuration} object.
     */
    public static Configuration getInstance(String[] options) {
        return new Configuration();
    }
}
