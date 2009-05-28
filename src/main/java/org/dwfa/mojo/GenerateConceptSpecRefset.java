package org.dwfa.mojo;

import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.cement.RefsetAuxiliary.Concept;

/**
 * Generate a Refset for all concepts in the Concept enum
 * 
 * @goal generate-concept-spec-refset
 * 
 * @phase generate-resources
 * 
 * @requiresDependencyResolution compile
 */
public class GenerateConceptSpecRefset extends AbstractMojo {

	/**
	 * The name of the RefSet.
	 * 
	 * @parameter
	 */
	private String refsetName;

	protected I_GetConceptData createRefsetConcept() throws Exception {
		I_TermFactory termFactory = LocalVersionedTerminology.get();
		I_GetConceptData fully_specified_description_type = termFactory
				.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE
						.getUids());
		I_GetConceptData preferred_description_type = termFactory
				.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE
						.getUids());
		I_ConfigAceFrame config = termFactory.newAceFrameConfig();
		I_Path path = termFactory
				.getPath(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH
						.getUids());
		config.addEditingPath(path);
		config.setDefaultStatus(termFactory
				.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()));
		I_GetConceptData newConcept = termFactory.newConcept(UUID.randomUUID(),
				false, config);
		// Install the FSN
		termFactory.newDescription(UUID.randomUUID(), newConcept, "en",
				refsetName, fully_specified_description_type, config);
		// Install the preferred term
		termFactory.newDescription(UUID.randomUUID(), newConcept, "en",
				refsetName, preferred_description_type, config);
		termFactory
				.newRelationship(
						UUID.randomUUID(),
						newConcept,
						termFactory
								.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL
										.getUids()),
						termFactory
								.getConcept(RefsetAuxiliary.Concept.REFSET_IDENTITY
										.getUids()),
						termFactory
								.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC
										.getUids()),
						termFactory
								.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE
										.getUids()),
						termFactory
								.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE
										.getUids()), 0, config);
		termFactory
				.newRelationship(
						UUID.randomUUID(),
						newConcept,
						termFactory
								.getConcept(RefsetAuxiliary.Concept.REFSET_TYPE_REL
										.getUids()),
						termFactory
								.getConcept(RefsetAuxiliary.Concept.CONCEPT_EXTENSION
										.getUids()),
						termFactory
								.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC
										.getUids()),
						termFactory
								.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE
										.getUids()),
						termFactory
								.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE
										.getUids()), 0, config);
		termFactory.commit();
		return newConcept;
	}

	private void addToRefset(I_TermFactory termFactory, int refsetId,
			int conceptId) throws Exception {
		I_GetConceptData include_individual = termFactory
				.getConcept(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL
						.getUids());
		// System.out.println("Include: " +
		// include_individual.getUids().get(0));
		int typeId = include_individual.getConceptId();
		I_GetConceptData active_status = termFactory
				.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids());
		int statusId = active_status.getConceptId();
		int memberId = termFactory.uuidToNativeWithGeneration(
				UUID.randomUUID(),
				ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize()
						.getNid(), termFactory.getPaths(), Integer.MAX_VALUE);
		I_ThinExtByRefVersioned newExtension = termFactory.newExtension(
				refsetId, memberId, conceptId, typeId);
		I_ThinExtByRefPartConcept conceptExtension = termFactory
				.newConceptExtensionPart();
		conceptExtension.setConceptId(conceptId);
		I_GetConceptData path = termFactory
				.getConcept(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH
						.getUids());
		// System.out.println("Path: " + path.getUids().get(0));
		conceptExtension.setPathId(path.getConceptId());
		conceptExtension.setStatusId(statusId);
		conceptExtension.setVersion(Integer.MAX_VALUE);
		newExtension.addVersion(conceptExtension);
		termFactory.addUncommitted(newExtension);
	}

	private void buildRefset() throws Exception {
		I_TermFactory termFactory = LocalVersionedTerminology.get();
		I_GetConceptData refset = createRefsetConcept();
		getLog().info("Refset: " + refset.getInitialText());
		getLog().info("Refset: " + refset.getUids().get(0));
		for (Concept c : Concept.values()) {
			getLog().info("Processing concept:" + c.name());
			try {
				I_GetConceptData member = termFactory.getConcept(c.getUids());
				addToRefset(termFactory, refset.getConceptId(), member
						.getConceptId());
			} catch (Exception ex) {
				getLog().error("Skipping concept:" + c.name());
			}
		}
		termFactory.commit();
	}

	// <execution>
	// <id>generate-concept-spec-refset</id>
	// <phase>generate-sources</phase>
	// <goals>
	// <goal>generate-concept-spec-refset</goal>
	// </goals>
	// <configuration>
	// <refsetName>IHTSDO Concept Spec Refset</refsetName>
	// </configuration>
	// </execution>

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			buildRefset();
		} catch (Exception e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
	}
}
