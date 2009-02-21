package org.dwfa.mojo;

import java.io.File;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.maven.MojoUtil;

/**
 * 
 * @goal vodb-close
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class VodbClose extends AbstractMojo {

    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
		I_ImplementTermFactory termFactoryImpl = (I_ImplementTermFactory) LocalVersionedTerminology.get();
		try {
            try {
                if (MojoUtil.alreadyRun(getLog(), "VodbClose", 
                		this.getClass(), targetDirectory)) {
                    return;
                }
            } catch (NoSuchAlgorithmException e) {
                throw new MojoExecutionException(e.getLocalizedMessage(), e);
            }
			termFactoryImpl.close();
		} catch (Exception e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}		
	}
}