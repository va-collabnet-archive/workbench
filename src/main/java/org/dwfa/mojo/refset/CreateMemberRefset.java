package org.dwfa.mojo.refset;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.refset.MemberRefsetCalculator;
import org.dwfa.mojo.ConceptDescriptor;

/**
 * 
 * @author Tore Fjellheim
 *
 * @goal createMemberRefset
 *
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class CreateMemberRefset extends AbstractMojo {

	/**
	 * Location of the build directory.
	 *
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;

	/**
	 * @parameter
	 * @required
	 * The concept descriptor for the member set path.
	 */
	private ConceptDescriptor memberSetPathDescriptor;

	/**
	 * @parameter
	 * The number of items to add to the uncommitted list before committing
	 */
	private int commitSize = 1000;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {

			MemberRefsetCalculator calc = new MemberRefsetCalculator();
			calc.setOutputDirectory(outputDirectory);
			calc.setPathConcept(memberSetPathDescriptor.getVerifiedConcept());
			calc.setValidateOnly(false);
			calc.setCommitSize(commitSize);
			calc.run();
		} catch (Exception e) {
			throw new MojoExecutionException("member refset calculation failed with exception", e);
		}

	}
}
