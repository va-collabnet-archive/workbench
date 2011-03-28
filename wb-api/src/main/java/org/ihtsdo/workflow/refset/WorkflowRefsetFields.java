package org.ihtsdo.workflow.refset;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;




/* 
* @author Jesse Efron
* 
*/
public abstract class WorkflowRefsetFields {
	private I_GetConceptData referencedComponent = null;
	
	public abstract String toString();
	public abstract boolean valuesExist();
	public abstract void cleanValues();
	
	public WorkflowRefsetFields() {
		
	}

	public void setReferencedComponentId(UUID refCompId) {
		try {
			if (refCompId == null)
				referencedComponent = null;
			else
				referencedComponent = Terms.get().getConcept(refCompId);
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Failed to Add Member", e);
		}
	}

	public UUID getReferencedComponentId() {
		return referencedComponent.getPrimUuid();
	}
	
	public I_GetConceptData getReferencedComponent() {
		return referencedComponent;
	}
}
