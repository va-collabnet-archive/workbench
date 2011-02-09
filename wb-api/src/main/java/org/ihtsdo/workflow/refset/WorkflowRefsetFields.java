package org.ihtsdo.workflow.refset;

import java.util.UUID;




/* 
* @author Jesse Efron
* 
*/
public abstract class WorkflowRefsetFields {
	private UUID referencedComponentId = null;
	
	public abstract String toString();
	public abstract boolean valuesExist();
	public abstract void cleanValues();
	
	public WorkflowRefsetFields() {
		
	}

	public void setReferencedComponentId(UUID refCompId) {
		referencedComponentId = refCompId;
	}

	public UUID getReferencedComponentId() {
		return referencedComponentId;
	}
	
}
