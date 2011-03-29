package org.ihtsdo.workflow.refset;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefsetWriter.WorkflowHistoryRSFields;




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

	public void setReferencedComponent(I_GetConceptData con) {
		referencedComponent = con;
	}

	public void setReferencedComponentId(UUID refCompId) {
		try {
			referencedComponent = Terms.get().getConcept(refCompId);
		} catch (Exception e) {
			referencedComponent = null;
		}
	}

	public void setReferencedComponentUid(UUID uid) {
		setReferencedComponentId(uid);
	}


	public I_GetConceptData getReferencedComponent() {
		return referencedComponent;
	}

	public UUID getReferencedComponentId() {
		return referencedComponent.getPrimUuid();
	}
	
	public UUID getReferencedComponentUid() {
		return getReferencedComponentId();
	}
	
}
