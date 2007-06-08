package org.dwfa.mojo;

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

	public void execute() throws MojoExecutionException, MojoFailureException {
		I_TermFactory termFactory = LocalVersionedTerminology.get();
		try {
			termFactory.commit();
		} catch (Exception e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}		
	}
}