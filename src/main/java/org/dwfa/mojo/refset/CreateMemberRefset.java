package org.dwfa.mojo.refset;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.refset.MemberRefsetCalculator;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.tapi.TerminologyException;

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

	public void execute() throws MojoExecutionException, MojoFailureException {
		// TODO Auto-generated method stub
		try {

			MemberRefsetCalculator calc = new MemberRefsetCalculator();
			calc.setOutputDirectory(outputDirectory);
			calc.setPathConcept(memberSetPathDescriptor.getVerifiedConcept());
			calc.setValidateOnly(false);
			calc.run();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
