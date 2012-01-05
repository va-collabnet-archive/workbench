package org.ihtsdo.config;



public interface ConfigServiceBase {
    
    /**
     * Initialises the ConfigService
     * @return A string message with any errors. If null or empty then OK
     */
    String init();
    
    /**
     * Given a string key value this should return an object.
     * @param lookupVal The key value
     * @return AN object from the config service
     */
    Object getConfigObject(String lookupVal);    
    

}
