package org.dwfa.mojo;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.maven.MojoUtil;

/**
 * 
 * 
 * @goal classify
 * 
 * @author michaellawley
 *
 */
public class Classify extends AbstractMojo {
    
    /**
     * The classify factory name
     *
     * @parameter
     */
    String classifyFactory = "java.lang.String";

    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName() + classifyFactory,
            		targetDirectory)) {
                return;
            }
        } catch (NoSuchAlgorithmException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
        I_TermFactory termFactory = LocalVersionedTerminology.get();
        
        getLog().info("classify mojo stub");
    }
    
}