package org.dwfa.mojo;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.maven.SetLogSpec;

/**
 * Sets specified loggers and handlers to the appropriate level
 * 
 * @goal ace-set-logs
 *
 */
public class AceSetLog extends AbstractMojo {
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
                getLog().info("Setting " + logSpec.getLogger() + " to:" + Level.parse(logSpec.getLevel()));
            }
        }
        
        if (handlerSettings != null) {
            for (SetLogSpec handlerSpec: handlerSettings) {
                String spec = handlerSpec.getLogger();
                if (spec==null || spec.equals(" ")) {
                    spec = "";
                }
                Logger log = Logger.getLogger(spec);
                for (Handler h: log.getHandlers()) {
                    getLog().info("Setting " + handlerSpec.getLogger() + " handler: " + h + " to:" + Level.parse(handlerSpec.getLevel()));
                    h.setLevel(Level.parse(handlerSpec.getLevel()));
                }
            }
        }
    }

}
