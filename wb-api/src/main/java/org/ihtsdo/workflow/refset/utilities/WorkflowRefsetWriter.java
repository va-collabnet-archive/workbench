package org.ihtsdo.workflow.refset.utilities;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.RefsetPropertyMap.REFSET_PROPERTY;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
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
	
	public boolean addMember() {
		boolean retVal = true;
		
		try {
			RefsetPropertyMap propMap = new RefsetPropertyMap();
			
			if (fields.valuesExist()) {
				propMap.put(REFSET_PROPERTY.STRING_VALUE, fieldsToRefsetString());

				I_ExtendByRef ref = helper.makeWfMetadataMemberAndSetup(refsetNid, fields.getReferencedComponent().getConceptNid(),
																		REFSET_TYPES.STR, propMap, UUID.randomUUID());
				Terms.get().addUncommitted(ref);
			} 
		} catch (Exception io) {
        	AceLog.getAppLog().log(Level.WARNING, "Failed to Add Member with error: " + io.getMessage());
        	retVal = false;
		}

		fields.cleanValues();
		return retVal;
	}

	public boolean retireMember()  {
		boolean retVal = false;
		try {
			RefsetPropertyMap propMap = new RefsetPropertyMap();

			if (fields.valuesExist()) {
				propMap.put(REFSET_PROPERTY.STRING_VALUE, fieldsToRefsetString());
				I_ExtendByRef ref = helper.getRefsetExtension(refsetNid, fields.getReferencedComponent().getConceptNid(), propMap);
				retVal = helper.retireRefsetStrExtension(refsetNid, 
														 fields.getReferencedComponent().getConceptNid(), 
														 propMap);
				Terms.get().addUncommitted(ref);
			}
		} catch (Exception io) {
			AceLog.getAppLog().log(Level.WARNING, "Failed to retire member with error: " + io.getMessage());
		}
		
		fields.cleanValues();
		return retVal;
	}
	
	public void commit() throws Exception {
        Terms.get().addUncommitted(refsetConcept);
        Terms.get().commit();		
	}
}
