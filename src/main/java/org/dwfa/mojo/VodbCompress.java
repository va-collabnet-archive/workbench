package org.dwfa.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;

/**
 * Compress database log files so their utilization is the value provided. 
 * Call this goal prior to closing the database. Corresponds to je.cleaner.minUtilization.
 * 
 * @goal vodb-compress
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class VodbCompress extends AbstractMojo {

    /**
     * Ensure the total disk space utilization percentage is above this value. 
     * The default is set to 90 percent. The maximum is 90 percent. The environmental
     * default is 50 percent.
     * @parameter
     */
    private int minUtilization = 90;

	public void execute() throws MojoExecutionException, MojoFailureException {
		I_ImplementTermFactory termFactoryImpl = (I_ImplementTermFactory) LocalVersionedTerminology
				.get();
		try {
			termFactoryImpl.compress(minUtilization);
		} catch (Exception e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
	}
}
