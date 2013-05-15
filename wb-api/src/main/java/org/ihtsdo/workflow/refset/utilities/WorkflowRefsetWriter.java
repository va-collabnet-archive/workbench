package org.ihtsdo.workflow.refset.utilities;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import org.dwfa.ace.api.I_GetConceptData;

import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.RefsetPropertyMap.REFSET_PROPERTY;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_string.RefexStringVersionBI;
import org.ihtsdo.tk.refset.SpecRefsetHelper;
import org.ihtsdo.tk.spec.ConceptSpec;
import org.ihtsdo.workflow.refset.WorkflowRefset;

/*
 * @author Jesse Efron
 *
 */
public abstract class WorkflowRefsetWriter extends WorkflowRefset {

//    protected I_HelpRefsets helper = null;

    public abstract String fieldsToRefsetString() throws IOException;

    protected WorkflowRefsetWriter(I_ConceptualizeUniversally refsetConcept)
            throws TerminologyException, IOException {
        super(refsetConcept);

        if (Terms.get() != null) {
//            helper = Terms.get().getRefsetHelper(Terms.get().getActiveAceFrameConfig());
        }
    }

    public I_ExtendByRef addMember(boolean autoCommit) {
        ConceptSpec wfHxRefSpec = new ConceptSpec("history workflow refset",UUID.fromString("0b6f0e24-5fe2-3869-9342-c18008f53283"));
        I_ExtendByRef ref = null;
        try {
            if (fields.valuesExist()) {
                RefsetPropertyMap propMap = new RefsetPropertyMap();
                propMap.put(REFSET_PROPERTY.STRING_VALUE, fieldsToRefsetString());
                SpecRefsetHelper helper = new SpecRefsetHelper(Terms.get().getActiveAceFrameConfig().getViewCoordinate(),
                        Terms.get().getActiveAceFrameConfig().getEditCoordinate());
                ref = (I_ExtendByRef) helper.newStringRefsetExtension(refsetNid, fields.getReferencedComponentNid(), fieldsToRefsetString());
               
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

    public I_ExtendByRef retireMember(){
        RefexVersionBI ref = null;
        

        try {
            ViewCoordinate vc = Terms.get().getActiveAceFrameConfig().getViewCoordinate();
            EditCoordinate ec = Terms.get().getActiveAceFrameConfig().getEditCoordinate();
            RefsetPropertyMap propMap = new RefsetPropertyMap();

            if (fields.valuesExist()) {
                ComponentVersionBI component = (RefexStringVersionBI) Ts.get().getComponentVersion(
                        vc,fields.getReferencedComponentNid());
                for(RefexVersionBI r : component.getRefexMembersActive(vc, refsetNid)){
                    RefexStringVersionBI refStr = (RefexStringVersionBI) r;
                    if(refStr.getString1().equals(fieldsToRefsetString())){
                        ref = r;
                    }
                }
                

                if (ref != null) {
                    RefexCAB retireBp = ref.makeBlueprint(vc);
                    retireBp.setRetired();
                    retireBp.setComponentUuidNoRecompute(ref.getPrimUuid());
                    Ts.get().getTerminologyBuilder(ec, vc).construct(retireBp);
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
        return (I_ExtendByRef) ref;
    }
}
