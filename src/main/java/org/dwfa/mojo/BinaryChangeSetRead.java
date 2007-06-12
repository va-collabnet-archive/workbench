package org.dwfa.mojo;

import java.io.File;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.cs.I_ReadChangeSet;

/**
 * 
 * @goal bcs-read
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */


public class BinaryChangeSetRead extends AbstractMojo {
	/**
	 * The change set directory
	 * 
	 * @parameter expression="${project.build.directory}/generated-resources/changesets/"

	 * @required
	 */
	File changeSetDir;
	
	/**
	 * The change set file name
	 * 
	 * @parameter 
	 * @required
	 */
	String changeSetFileName = UUID.randomUUID() + ".bcs";

	public void execute() throws MojoExecutionException, MojoFailureException {
		I_TermFactory termFactory = LocalVersionedTerminology.get();
		try {
			I_ReadChangeSet reader = termFactory.newBinaryChangeSetReader(new File(changeSetDir, changeSetFileName));
			reader.read();
		} catch (Exception e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}		
	}
}