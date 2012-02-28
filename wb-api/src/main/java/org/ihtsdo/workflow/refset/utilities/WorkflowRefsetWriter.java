package org.ihtsdo.workflow.refset.utilities;

import java.io.IOException;
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
        I_ExtendByRef ref = null;
        try {
            if (fields.valuesExist()) {
                RefsetPropertyMap propMap = new RefsetPropertyMap();

                if (fields.valuesExist()) {
                    propMap.put(REFSET_PROPERTY.STRING_VALUE, fieldsToRefsetString());

                    ref = helper.makeWfMetadataMemberAndSetup(refsetNid, fields.getReferencedComponentNid(), REFSET_TYPES.STR, propMap, UUID.randomUUID());
                }
                ConceptChronicleBI concept = Ts.get().getConcept(fields.getReferencedComponentUid());
                ConceptChronicleBI refset = Ts.get().getConcept(refsetNid);
             
                if (autoCommit) {
	            	Terms.get().addUncommittedNoChecks(ref);
                if (refset.isAnnotationStyleRefex()) {
                    Terms.get().addUncommittedNoChecks((I_GetConceptData) concept);
                } else {
                    Terms.get().addUncommittedNoChecks((I_GetConceptData) refset);
                }
	            } else {
	            	Terms.get().addUncommitted(ref);
	                if (refset.isAnnotationStyleRefex()) {
	                    Terms.get().addUncommitted((I_GetConceptData) concept);
	                } else {
	                    Terms.get().addUncommitted((I_GetConceptData) refset);
	                }
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

                    ConceptChronicleBI concept = Ts.get().getConcept(fields.getReferencedComponentUid());
                    ConceptChronicleBI refset = Ts.get().getConcept(refsetNid);
                    if (refset.isAnnotationStyleRefex()) {
                        Terms.get().addUncommittedNoChecks((I_GetConceptData) concept);
                    } else {
                        Terms.get().addUncommittedNoChecks((I_GetConceptData) refset);
                    }
                }
            }
        } catch (Exception io) {
            AceLog.getAppLog().log(Level.WARNING, "Failed to retire member with error: " + io.getMessage());
        }

        fields.cleanValues();
        return ref;
    }
}
