package org.dwfa.mojo;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.maven.MojoUtil;

/**
 * 
 * @goal vodb-commit
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */

public class VodbCommit  extends AbstractMojo {
    
    /**
     * The execution information for this commit operation. 
     * @parameter expression="${mojoExecution}"
     */
    MojoExecution execution;
     
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName() + execution.getExecutionId())) {
				return;
			}
		} catch (NoSuchAlgorithmException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		} catch (IOException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		} 
		I_TermFactory termFactory = LocalVersionedTerminology.get();
        getLog().info("commiting (id: " + execution.getExecutionId() + "): " + termFactory);  
        if (termFactory != null) {
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
        } else {
            Exception ex = new Exception("Attempting commit with null term factory (id: " + execution.getExecutionId() + "): " + termFactory);
            throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
        }
        
	}
}