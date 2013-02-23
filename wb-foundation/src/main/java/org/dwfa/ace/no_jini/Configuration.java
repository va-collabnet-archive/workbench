package org.dwfa.ace.no_jini;

/**
 * Dummy class to provide compile-time support for Jini references
 * added as part of merge from wb-toolkit_trek.
 * @author ocarlsen
 */
public final class Configuration {
    /**
     * Copied from {@code net.jini.config.Configuration} object.
     * @return Always throws {@link UnsupportedOperationException}.
     */
    public Object getEntry(String component, String name, Class type, Object defaultValue) {
//      return defaultValue;
        throw new UnsupportedOperationException("TODO: No Jini.");
    }
}