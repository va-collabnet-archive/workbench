package org.dwfa.mojo;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.LocalVersionedTerminology;
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

	
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
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
