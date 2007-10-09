package org.dwfa.mojo;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.maven.MojoUtil;

/**
 * Read a binary change set, and apply the results of that change set to the
 * open database. 
 * @goal bcs-read
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */


public class BinaryChangeSetRead extends AbstractMojo {
	/**
	 * The change set directory
	 * 
	 * @parameter default-value="${project.build.directory}/generated-resources/changesets/"
	 */
	File changeSetDir;
	
	/**
	 * The change set file name
	 * 
	 * @parameter 
	 * @required
	 */
	String changeSetFileName;

	public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName() + changeSetDir.getCanonicalPath() + changeSetFileName)) {
                return;
            }
        } catch (NoSuchAlgorithmException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
		I_TermFactory termFactory = LocalVersionedTerminology.get();
		try {
			I_ReadChangeSet reader = termFactory.newBinaryChangeSetReader(new File(changeSetDir, changeSetFileName));
			reader.read();
		} catch (Exception e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}		
	}
}