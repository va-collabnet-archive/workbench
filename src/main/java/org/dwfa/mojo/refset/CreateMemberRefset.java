package org.dwfa.mojo.refset;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
	
	/**
	 * @parameter
	 * Use the direct non-transaction ACE API (defaults to false)
	 */
	private boolean useNonTxInterface = false;

	/**
	 * Output location for the change sets
	 *
	 * @parameter
	 * @required
	 */
	private File changeSetOutputDirectory;
	
	/**
	 * Specification refsets to be processed.
	 * @parameter
	 */
	ConceptDescriptor[] specRefsets = null;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {

			MemberRefsetCalculator calc = new MemberRefsetCalculator();
			
			calc.setOutputDirectory(outputDirectory);
			calc.setChangeSetOutputDirectory(changeSetOutputDirectory);
			calc.setPathConcept(memberSetPathDescriptor.getVerifiedConcept());
			calc.setValidateOnly(false);
			calc.setCommitSize(commitSize);
			calc.setUseNonTxInterface(useNonTxInterface);

			if (specRefsets != null) {
		        List<Integer> allowedRefsets = new ArrayList<Integer>();
		        for (ConceptDescriptor conceptDesc : specRefsets) {
			        allowedRefsets.add(conceptDesc.getVerifiedConcept().getConceptId());
		        }
		        calc.setAllowedRefsets(allowedRefsets);			
			}
			
			calc.run();
			
		} catch (Exception e) {
			throw new MojoExecutionException("member refset calculation failed with exception", e);
		}

	}
}
