package org.ihtsdo.mojo.release.qa;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;

/**
 * @author Alejandro Rodriguez
 * 
 * @goal update-gmdn-concepts
 * @requiresDependencyResolution compile
 */
public class UpdatedConceptsMojo extends AbstractMojo implements I_ProcessConcepts {

	protected I_ConfigAceFrame currenAceConfig;
	private I_GetConceptData root;
	private NidSetBI allStatuses;
	@Override
	public void processConcept(I_GetConceptData concept) throws Exception {

		if (this.root.isParentOf(concept, 
				currenAceConfig.getAllowedStatus(),
				currenAceConfig.getDestRelTypes(), 
				currenAceConfig.getViewPositionSetReadOnly(), 
				currenAceConfig.getPrecedence(), 
				currenAceConfig.getConflictResolutionStrategy())) {



			List<? extends I_ConceptAttributeTuple> conceptAttributes = concept.getConceptAttributeTuples(
					allStatuses, 
					currenAceConfig.getViewPositionSetReadOnly(), 
					Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());

			if (conceptAttributes != null && !conceptAttributes.isEmpty()) {
				I_ConceptAttributeTuple attributes = conceptAttributes.iterator().next();

				if (attributes.isDefined()) {
					I_ConceptAttributePart lastAttributePart = getLastestAttributePart(concept);
					for (PathBI editPath : currenAceConfig.getEditingPathSet()) {
						I_ConceptAttributePart newAttributeVersion = (I_ConceptAttributePart) lastAttributePart.makeAnalog(lastAttributePart.getStatusNid(), Long.MAX_VALUE, lastAttributePart.getAuthorNid(), lastAttributePart.getModuleNid(),
								editPath.getConceptNid());
						newAttributeVersion.setDefined(false);
						concept.getConAttrs().addVersion(newAttributeVersion);
					}
					Terms.get().addUncommittedNoChecks(concept);
				}


			}
		}
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {


		try {
			this.allStatuses = getAllStatuses();
			this.currenAceConfig = Terms.get().getActiveAceFrameConfig();
			// root: phyical object
			this.root=Terms.get().getConcept(UUID.fromString("72765109-6b53-3814-9b05-34ebddd16592"));
			Terms.get().iterateConcepts(this);

		} catch (Exception e) {
			e.printStackTrace();
			throw new MojoExecutionException(e.getMessage());
		}		
	}


	public I_ConceptAttributePart getLastestAttributePart(I_GetConceptData concept) throws IOException {
		Collection<? extends ConceptAttributeVersionBI> refsetAttibuteParts = concept.getConceptAttributes().getVersions();
		ConceptAttributeVersionBI latestAttributePart = null;
		for (ConceptAttributeVersionBI attributePart : refsetAttibuteParts) {
			if (latestAttributePart == null || attributePart.getTime() >= latestAttributePart.getTime()) {
				latestAttributePart = attributePart;
			}
		}

		if (latestAttributePart == null) {
			throw new IOException("No parts on this viewpositionset.");
		}

		return (I_ConceptAttributePart) latestAttributePart;
	}
	public NidSetBI getAllStatuses() throws TerminologyException, IOException {
		NidSetBI allStatuses = new NidSet();
		Set<I_GetConceptData> descendants = new HashSet<I_GetConceptData>();
		I_GetConceptData statusRoot =  Terms.get().getConcept(UUID.fromString("d944af55-86d9-33f4-bebd-a10bf3f4712c"));
		descendants = getDescendantsLocal(descendants, statusRoot );
		for (I_GetConceptData loopConcept : descendants) {
			allStatuses.add(loopConcept.getNid());
		}
		I_GetConceptData activeValue =  Terms.get().getConcept(UUID.fromString("d12702ee-c37f-385f-a070-61d56d4d0f1f"));
		allStatuses.add(activeValue.getNid());
		descendants = getDescendantsLocal(descendants, activeValue );
		for (I_GetConceptData loopConcept : descendants) {
			allStatuses.add(loopConcept.getNid());
		}		
		I_GetConceptData inactiveValue =  Terms.get().getConcept(UUID.fromString("a5daba09-7feb-37f0-8d6d-c3cadfc7f724"));
		allStatuses.add(inactiveValue.getNid());
		descendants = getDescendantsLocal(descendants, inactiveValue );
		for (I_GetConceptData loopConcept : descendants) {
			allStatuses.add(loopConcept.getNid());
		}
		return allStatuses;
	}

	public  Set<I_GetConceptData> getDescendantsLocal(Set<I_GetConceptData> descendants, I_GetConceptData concept) {
		try {
			I_TermFactory termFactory = Terms.get();
			I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
			allowedDestRelTypes.add(termFactory.uuidToNative(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25")));

			Set<I_GetConceptData> childrenSet = new HashSet<I_GetConceptData>();
			childrenSet.addAll(concept.getDestRelOrigins(config.getAllowedStatus(), allowedDestRelTypes, 
					config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy()));
			descendants.addAll(childrenSet);
			for (I_GetConceptData loopConcept : childrenSet) {
				descendants = getDescendantsLocal(descendants, loopConcept);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		return descendants;
	}
}
