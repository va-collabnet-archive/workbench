package org.dwfa.mojo;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.dwfa.ace.api.I_GetConceptData;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;


import org.dwfa.maven.MojoUtil;
import org.dwfa.tapi.TerminologyException;
/**
 *This goal will add all the  given concept as path.
 * 
 * @goal vodb-create-new-path-from-exist-concept
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 * @author Ming Zhang
 *
 */

public class VodbCreateNewPathFromParentConcept extends AbstractMojo {

	/**
	 * Path origins
	 * 
	 * @parameter
	 */
	SimpleUniversalAcePosition[] origins;

	
	/**
	 * Parent of the new pathes.
	 * 
	 * @parameter
	 * @required
	 */
	private ConceptDescriptor pathConcept;

    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

	public void execute() throws MojoExecutionException, MojoFailureException {
		// Use the architectonic branch for all path editing.
		try {
			try {
				if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName() +  pathConcept.getDescription(),
						targetDirectory)) {
					return;
				}
			} catch (NoSuchAlgorithmException e) {
				throw new MojoExecutionException(e.getLocalizedMessage(), e);
			} 
			I_TermFactory tf = LocalVersionedTerminology.get();
			Set<I_Position> pathOrigins = null;
			if (origins != null) {
				pathOrigins = new HashSet<I_Position>(origins.length);
				for (SimpleUniversalAcePosition pos : origins) {
					I_Path originPath = tf.getPath(pos.getPathId());
					pathOrigins.add(tf.newPosition(originPath, pos.getTime()));
				}
			}
			I_GetConceptData path = pathConcept.getVerifiedConcept();
								
			tf.newPath(pathOrigins, path);

		} catch (TerminologyException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		} catch (IOException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		} catch (ParseException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		} catch (Exception e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
	}
}
