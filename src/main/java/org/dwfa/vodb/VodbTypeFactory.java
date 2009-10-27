package org.dwfa.vodb;

import org.dwfa.tapi.TerminologyRuntimeException;

/**
 * Create a new concrete instance of an interface.
 * <p>
 * The created instance type will be determined from by removing the "I_" prefix
 * from the class name and changing the package to "org.dwfa.vodb.types". 
 * <p>
 * eg. <code>org.dwfa.ace.api.ebr.I_<b>ThinExtByRefPartConcept</b></code> 
 *      will resolve to an instance of
 *     <code>org.dwfa.vodb.types.<b>ThinExtByRefPartConcept</b></code>
 */
public class VodbTypeFactory {
    
    /** 
     * May be overridden by extending implementations, for example to return a
     * mock class from a testing package.
     */
    protected static String IMPL_PACKAGE = "org.dwfa.vodb.types";
    
    
    public static <T> T create(Class<T> t) throws TerminologyRuntimeException {
        try {
            return resolve(t).newInstance();
            
        } catch (Exception e) {
            throw new TerminologyRuntimeException(e);
        }
    }   
    
    protected static <T> Class<? extends T> resolve(Class<T> t) throws ClassNotFoundException {
        
        Class<?> targetClass = Class.forName( 
            t.getName().replaceFirst(t.getPackage().getName(), IMPL_PACKAGE).replaceFirst("I_", ""));
        
        return targetClass.asSubclass(t);
    }
}
