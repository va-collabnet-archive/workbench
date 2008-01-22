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
     * Specifications to apply to the specified logs. If you want to specify the root log, use the
     * logger name of "root". 
     * @parameter 
     */
    private SetLogSpec[] logSettings;
    
    /**
     * Specifications to apply to all handlers associated with the specified logs. If you want to specify the root log, use the
     * logger name of "root". 
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
                if (spec==null || spec.equals(" ") || spec.equals("root")) {
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
