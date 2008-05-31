package org.dwfa.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;

/**
 * Compress database log files so there utilization is the value provided. 
 * Call this goal prior to closing the database. 
 * 
 * @goal vodb-compress
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class VodbCompress extends AbstractMojo {

    /**
     * Minimum file utilization value.
     * @parameter
     */
    private int minFileUtilization = 90;

	public void execute() throws MojoExecutionException, MojoFailureException {
		I_ImplementTermFactory termFactoryImpl = (I_ImplementTermFactory) LocalVersionedTerminology
				.get();
		try {
			termFactoryImpl.compress(minFileUtilization);
		} catch (Exception e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
	}
}
