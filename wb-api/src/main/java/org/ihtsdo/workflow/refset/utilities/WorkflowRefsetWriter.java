package org.ihtsdo.workflow.refset.utilities;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.RefsetPropertyMap.REFSET_PROPERTY;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.WorkflowRefset;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefset;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefsetWriter;



/* 
* @author Jesse Efron
* 
*/
public abstract class WorkflowRefsetWriter extends WorkflowRefset {
	protected WorkflowRefset refset = null;

	public abstract String fieldsToRefsetString() throws IOException;
	
	protected WorkflowRefsetWriter() throws TerminologyException, IOException {
		super();
	}

	public boolean addMember() {
		String refCompStr = null;
		boolean retVal = false;
		
		try {
			RefsetPropertyMap propMap = new RefsetPropertyMap();
			
			if (fields.valuesExist())
			{
		        refCompStr = fields.getReferencedComponentId().getInitialText();
				propMap.put(REFSET_PROPERTY.STRING_VALUE, fieldsToRefsetString());
				helper.newRefsetExtension(refsetId, fields.getReferencedComponentId().getConceptNid(), REFSET_TYPES.STR, propMap, Terms.get().getActiveAceFrameConfig());
				retVal = true;
			} else
				return false;
		} catch (Exception io) {
        	AceLog.getAppLog().alertAndLog(Level.SEVERE, "Failed to Add Member", io);
		}

		fields.cleanValues();
		return retVal;
	}

	//public boolean retireMember(int refsetId, int memberId)  {
	public boolean retireMember()  {
		String refCompStr = null;
		
		try {
			RefsetPropertyMap propMap = new RefsetPropertyMap();

			if (fields.valuesExist())
			{
				refCompStr = fields.getReferencedComponentId().getInitialText();

				propMap.put(REFSET_PROPERTY.STRING_VALUE, fieldsToRefsetString());
				helper.retireRefsetExtension(refsetId, fields.getReferencedComponentId().getConceptNid(), propMap);
	
				return true;
			}
		} catch (Exception io) {
        	AceLog.getAppLog().alertAndLog(Level.SEVERE, "Failed to retire member", io);
		}
		
		fields.cleanValues();
			return false;
		}
	
	

	public Collection<UUID> getRefsetUids() throws TerminologyException, IOException
	{
		return refset.getRefsetConcept().getUids();
	}

	public void commit() throws Exception {
        Terms.get().addUncommitted(refset.getRefsetConcept());
        Terms.get().commit();		
	}
}
