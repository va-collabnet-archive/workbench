package org.dwfa.maven;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Sets specified loggers and handlers to the appropriate level
 * 
 * @goal dwfa-set-logs
 *
 */
public class DwfaSetLog extends AbstractMojo {
    
    /**
     * Specifications to apply to the specified logs
     * @parameter 
     */
    private SetLogSpec[] logSettings;
    
    /**
     * Specifications to apply to all handlers associated with the specified logs
     * @parameter 
     */
    private SetLogSpec[] handlerSettings;
    
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (logSettings != null) {
            for (SetLogSpec logSpec: logSettings) {
                Logger log = Logger.getLogger(logSpec.getLogger());
                log.setLevel(Level.parse(logSpec.getLevel()));
            }
        }
        
        if (handlerSettings != null) {
            for (SetLogSpec handlerSpec: handlerSettings) {
                Logger log = Logger.getLogger(handlerSpec.getLogger());
                for (Handler h: log.getHandlers()) {
                    h.setLevel(Level.parse(handlerSpec.getLevel()));
                }
            }
        }
    }

}
