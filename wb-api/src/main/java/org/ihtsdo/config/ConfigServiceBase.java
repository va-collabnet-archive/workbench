package org.ihtsdo.config;

public interface ConfigServiceBase {
    
    /**
     * Initialises the ConfigService
     * @return A string message with any errors. If null or empty then OK
     */
    String init();

}
