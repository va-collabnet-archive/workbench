package org.ihtsdo.workflow.refset.utilities;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.RefsetPropertyMap.REFSET_PROPERTY;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.workflow.refset.WorkflowRefset;



/* 
* @author Jesse Efron
* 
*/
public abstract class WorkflowRefsetWriter extends WorkflowRefset {
	protected WorkflowRefset refset = null;

	public abstract String fieldsToRefsetString() throws IOException;
	
	protected WorkflowRefsetWriter(boolean setupDatabaseObjects) throws TerminologyException, IOException {
 		super();
 		if (setupDatabaseObjects) {
			helper = Terms.get().getRefsetHelper(Terms.get().getActiveAceFrameConfig());		
		}
	}

	protected WorkflowRefsetWriter() throws TerminologyException, IOException {
 		this(true);
	}

	public boolean addMember() {
		boolean retVal = false;
		
		try {
			RefsetPropertyMap propMap = new RefsetPropertyMap();
			
			if (fields.valuesExist())
			{
				propMap.put(REFSET_PROPERTY.STRING_VALUE, fieldsToRefsetString());
				helper.newRefsetExtension(refsetId, fields.getReferencedComponent().getConceptNid(), REFSET_TYPES.STR, propMap, Terms.get().getActiveAceFrameConfig());
				retVal = true;
			} else
				return false;
		} catch (Exception io) {
        	AceLog.getAppLog().log(Level.WARNING, "Failed to Add Member with error: " + io.getMessage());
		}

		fields.cleanValues();
		return retVal;
	}

	//public boolean retireMember(int refsetId, int memberId)  {
	public boolean retireMember()  {
		
		try {
			RefsetPropertyMap propMap = new RefsetPropertyMap();

			if (fields.valuesExist())
			{
				propMap.put(REFSET_PROPERTY.STRING_VALUE, fieldsToRefsetString());
				helper.retireRefsetStrExtension(refsetId, fields.getReferencedComponent().getConceptNid(), propMap);
	
				return true;
			}
		} catch (Exception io) {
        	AceLog.getAppLog().log(Level.WARNING, "Failed to retire member with error: " + io.getMessage());
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
