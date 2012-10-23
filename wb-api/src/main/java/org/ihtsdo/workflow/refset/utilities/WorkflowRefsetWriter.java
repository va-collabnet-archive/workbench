package org.ihtsdo.workflow.refset.utilities;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import org.dwfa.ace.api.I_GetConceptData;

import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.RefsetPropertyMap.REFSET_PROPERTY;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.spec.ConceptSpec;
import org.ihtsdo.workflow.refset.WorkflowRefset;

/*
 * @author Jesse Efron
 *
 */
public abstract class WorkflowRefsetWriter extends WorkflowRefset {

    protected I_HelpRefsets helper = null;

    public abstract String fieldsToRefsetString() throws IOException;

    protected WorkflowRefsetWriter(I_ConceptualizeUniversally refsetConcept)
            throws TerminologyException, IOException {
        super(refsetConcept);

        if (Terms.get() != null) {
            helper = Terms.get().getRefsetHelper(Terms.get().getActiveAceFrameConfig());
        }
    }

    public I_ExtendByRef addMember(boolean autoCommit) {
        ConceptSpec wfHxRefSpec = new ConceptSpec("history workflow refset",UUID.fromString("0b6f0e24-5fe2-3869-9342-c18008f53283"));
        I_ExtendByRef ref = null;
        try {
            if (fields.valuesExist()) {
                RefsetPropertyMap propMap = new RefsetPropertyMap();
                propMap.put(REFSET_PROPERTY.STRING_VALUE, fieldsToRefsetString());

                ref = helper.makeWfMetadataMemberAndSetup(refsetNid, fields.getReferencedComponentNid(), REFSET_TYPES.STR, propMap, UUID.randomUUID());
               
                if (ref != null) {
	                I_GetConceptData refset = Terms.get().getConcept(refsetNid);
	
	                if (refset.isAnnotationStyleRefex()) {
	                	// Workflow history refset only annotated workflow refset at this point
		                if (autoCommit) {
		                	// Updated via AdvanceWf, Undo, override
			                Terms.get().addUncommittedNoChecks(ref);
			                I_GetConceptData concept = Terms.get().getConcept(fields.getReferencedComponentUid());
		                	Ts.get().commit(concept);
		                } else {
		                	// Updated via UpdateWorkflowUponCommit (includes autoApprove) 
		                	Terms.get().addUncommittedNoChecks(ref);
		                }
	                } else {
                            UUID refsetUuid = Ts.get().getUuidPrimordialForNid(refsetNid);
                            if (refsetUuid.equals(wfHxRefSpec.getLenient().getPrimUuid())) {
                                Terms.get().addUncommittedNoChecks(refset);
                            } else {
                                // Other workflow refsets (ie editor category)
                                Ts.get().commit(refset);
                            }
	                }
	            } else {
	                throw new NullPointerException("Null wfhx refset member created for concept: " + fields.getReferencedComponentNid()); 
	            }
            }
        } catch (Exception io) {
            AceLog.getAppLog().log(Level.WARNING, "Failed to Add Member with error: " + io.getMessage());
        }

        fields.cleanValues();
        return ref;
    }

    public I_ExtendByRef retireMember() {
        I_ExtendByRef ref = null;

        try {
            RefsetPropertyMap propMap = new RefsetPropertyMap();

            if (fields.valuesExist()) {
                propMap.put(REFSET_PROPERTY.STRING_VALUE, fieldsToRefsetString());
                ref = helper.getRefsetExtension(refsetNid, fields.getReferencedComponentNid(), propMap);

                if (ref != null) {
                    helper.retireRefsetStrExtension(refsetNid, fields.getReferencedComponentNid(), propMap);
                    I_GetConceptData refset = Terms.get().getConcept(refsetNid);
                    
                    if (refset.isAnnotationStyleRefex()) {
	                	// Workflow history refset only annotated workflow refset at this point
                        I_GetConceptData concept = Terms.get().getConcept(fields.getReferencedComponentUid());
	                    Ts.get().commit(concept);
                    } else {
	                	// Other workflow refsets (ie editor category)
	                    Ts.get().commit(refset);
                    }
                } else {
	                throw new NullPointerException("Null wfhx refset member retirement for concept: " + fields.getReferencedComponentNid()); 
	            }

            }
        } catch (Exception io) {
            AceLog.getAppLog().log(Level.WARNING, "Failed to retire member with error: " + io.getMessage());
        }

        fields.cleanValues();
        return ref;
    }
}
