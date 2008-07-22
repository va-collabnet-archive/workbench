package org.dwfa.mojo;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;

/**
 * 
 * @goal vodb-commit
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */

public class VodbCommit  extends AbstractMojo {

    /**
     * Location of the directory to output data files to.
     * KEC: I added this field, because the maven plugin plugin would 
     * crash unless there was at least one commented field. This field is
     * not actually used by the plugin. 
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    @SuppressWarnings("unused")
    private String outputDirectory;
    /**
     * The maven session
     * 
     * @parameter expression="${session}"
     * @required
     */
    private MavenSession session;
    
	public void execute() throws MojoExecutionException, MojoFailureException {
		I_TermFactory termFactory = LocalVersionedTerminology.get();
        getLog().info("commiting: " + termFactory);        
        if (termFactory.getUncommitted() != null 
        		&& termFactory.getUncommitted().size() > 0) {
            try {

                termFactory.commit();
            } catch (Exception e) {
                throw new MojoExecutionException(e.getLocalizedMessage(), e);
            }       
        } else {
        	getLog().info("termfactory.getUncommitted().size() = " + termFactory.getUncommitted().size());
        }
        
	}
}