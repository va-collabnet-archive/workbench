package org.ihtsdo.workflow.refset.utilities;

import java.io.IOException;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;



/* 
* @author Jesse Efron
* 
*/
public abstract class WorkflowRefsetCreator {
	I_GetConceptData parentRefsetCon = null;
	I_GetConceptData isAConcept = null;
	protected I_TermFactory tf = null;
	private I_ConfigAceFrame config = null;

	public abstract int create() throws IOException, TerminologyException;

	public WorkflowRefsetCreator() throws IOException, TerminologyException {
		config = Terms.get().getActiveAceFrameConfig();
		parentRefsetCon = Terms.get().getConcept(RefsetAuxiliary.Concept.REFSET_IDENTITY.getUids());
		isAConcept = Terms.get().getConcept(ConceptConstants.SNOMED_IS_A
				.getLenient().getNid()); // rel
	}

	public int createRefsetConcept(String refsetName) throws IOException, TerminologyException 
	{
		String name = refsetName + " member reference set";
        I_GetConceptData newRefsetConcept = createNewConcept(name);

        // Create Relationships for MemberSet
        createMembersetRels(newRefsetConcept);

        // Commit
        Terms.get().addUncommitted(newRefsetConcept);
                
		return newRefsetConcept.getConceptNid();
	}

	private I_GetConceptData createNewConcept(String conceptName) throws IOException, TerminologyException {
        I_GetConceptData newConcept = null;
        I_TermFactory tf = Terms.get();
        
        try {
            newConcept = tf.newConcept(UUID.randomUUID(), false, config);

            tf.newDescription(UUID.randomUUID(), newConcept, "en", conceptName,
                ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize(), config);

            tf.newDescription(UUID.randomUUID(), newConcept, "en", conceptName,
                ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize(), config);

            return newConcept;
        } catch (IOException e) {
            throw new IOException(e);
        } catch (TerminologyException e) {
            throw new TerminologyException(e);
        } 
    }// End method createNewConcept

    private void createMembersetRels(I_GetConceptData concept) throws IOException, TerminologyException {
        try {
            I_TermFactory tf = Terms.get();
 
            I_GetConceptData relChar = tf.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids());
            I_GetConceptData relStat = tf.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());

            String realParentUid = "3e0cd740-2cc6-3d68-ace7-bad2eb2621da";
            if (parentRefsetCon.getUids().iterator().next().toString().compareTo(realParentUid) != 0)
            	parentRefsetCon = tf.getConcept(new UUID[] { UUID.fromString(realParentUid) });
             
            /*
             * Create default "is a" relationship
             */
            tf.newRelationship(UUID.randomUUID(), concept, 
            	this.isAConcept // rel type
                , parentRefsetCon // dest concept
                , relChar, parentRefsetCon, relStat, 0, config);

            /*
             * Create default "refset type rel" relationship
             */

            tf.newRelationship(UUID.randomUUID(), concept,
                tf.getConcept(ConceptConstants.REFSET_TYPE_REL.getLenient().getNid()) // rel type
                , tf.getConcept(RefsetAuxiliary.Concept.STRING_EXTENSION.localize().getNid()) // dest concept
                , relChar, parentRefsetCon, relStat, 0, config);

        } catch (IOException e) {
            throw new IOException(e);
        } catch (TerminologyException e) {
            throw new TerminologyException (e);
        }
    }// End method createMembersetRels
}
