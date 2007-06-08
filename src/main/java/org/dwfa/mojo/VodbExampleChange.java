package org.dwfa.mojo;

import java.util.HashSet;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;

/**
 * 
 * @goal vodb-example-change
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class VodbExampleChange extends AbstractMojo {

	/**
	 * Flag a concept to demonstrate how to change make changes to components...
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		I_TermFactory termFactory = LocalVersionedTerminology.get();
		try {
			I_GetConceptData architectonicRoot = termFactory
					.getConcept(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT
							.getUids());
			I_ConceptAttributeVersioned conceptAttributes = architectonicRoot.getConceptAttributes();
			I_GetConceptData architectonicBranch = termFactory.getConcept(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids());
			I_Path architectonicPath = termFactory.getPath(architectonicBranch.getUids());
			I_Position latestOnArchitectonicPath = termFactory.newPosition(architectonicPath, Integer.MAX_VALUE);
			Set<I_Position> positions = new HashSet<I_Position>();
			positions.add(latestOnArchitectonicPath);
			I_GetConceptData flaggedStatus = termFactory.getConcept(ArchitectonicAuxiliary.Concept.FLAGGED_FOR_REVIEW.getUids());
			
			
			for (I_ConceptAttributeTuple tuple: architectonicRoot.getConceptAttributeTuples(null, positions)) {
				I_ConceptAttributePart part = tuple.duplicatePart();
				part.setVersion(Integer.MAX_VALUE);
				part.setConceptStatus(flaggedStatus.getConceptId());
				conceptAttributes.addVersion(part);
				termFactory.addUncommitted(architectonicRoot);
			}

		} catch (Exception e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
	}

}