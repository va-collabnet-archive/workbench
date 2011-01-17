package org.ihtsdo.workflow.refset;

import org.dwfa.ace.api.I_GetConceptData;



/* 
* @author Jesse Efron
* 
*/
public abstract class WorkflowRefsetFields {
	private I_GetConceptData referencedComponentId = null;
	
	public abstract String toString();
	public abstract boolean valuesExist();
	public abstract void cleanValues();
	
	public WorkflowRefsetFields() {
		
	}

	public void setReferencedComponentId(I_GetConceptData referencedComponentId) {
		this.referencedComponentId = referencedComponentId;
	}

	public I_GetConceptData getReferencedComponentId() {
		return referencedComponentId;
	}
	
}
