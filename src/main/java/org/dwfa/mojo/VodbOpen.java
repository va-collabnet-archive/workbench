package org.dwfa.mojo;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.maven.MojoUtil;
/**
 * 
 * @goal vodb-open
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class VodbOpen  extends AbstractMojo {

	/**
	 * Location of the vodb directory.
	 * 
	 * @parameter default-value="${project.build.directory}/generated-resources/berkeley-db"
	 * @required
	 */
	File vodbDirectory;
	
	/**
	 * True if the database is readonly.
	 * 
	 * @parameter 
	 */
	Boolean readOnly = false;
	
	/**
	 * Size of cache used by the database.
	 * 
	 * @parameter 
	 */
	Long cacheSize = 600000000L;

	/**
	 * Use existing if it is already open
	 * 
	 * @parameter 
	 * 
	 */
	boolean useExistingDb = false;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			if (useExistingDb && LocalVersionedTerminology.get()!=null) {
				return;
			}
            try {
                if (MojoUtil.alreadyRun(getLog(), vodbDirectory.getCanonicalPath())) {
                    return;
                }
            } catch (NoSuchAlgorithmException e) {
                throw new MojoExecutionException(e.getLocalizedMessage(), e);
            }
			LocalVersionedTerminology.openDefaultFactory(vodbDirectory, readOnly, cacheSize);
		} catch (InstantiationException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		} catch (IllegalAccessException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		} catch (ClassNotFoundException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		} catch (IOException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
		
	}
	
}
