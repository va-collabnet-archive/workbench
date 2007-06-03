package org.dwfa.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;

/**
 * 
 * @goal vodb-checkpoint
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class VodbCheckpoint extends AbstractMojo {

	
	public void execute() throws MojoExecutionException, MojoFailureException {
			I_ImplementTermFactory termFactoryImpl = (I_ImplementTermFactory) LocalVersionedTerminology
				.get();
		try {
			termFactoryImpl.checkpoint();
		} catch (Exception e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}		
	}
}