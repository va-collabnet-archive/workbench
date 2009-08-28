package org.dwfa.tapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used by the terminology factory to look back up the call stack to determine
 * if the execution of the data checks should be suppressed. 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SuppressDataChecks {
	
}
