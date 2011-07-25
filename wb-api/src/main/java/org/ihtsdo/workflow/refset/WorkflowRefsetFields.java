package org.ihtsdo.workflow.refset;

import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;




/* 
* @author Jesse Efron
* 
*/
public abstract class WorkflowRefsetFields {
	private UUID referencedComponentUid = null;
	private int referencedComponentNid = 0;
	
	public abstract String toString();
	public abstract boolean valuesExist();
	public abstract void cleanValues();
	
	public WorkflowRefsetFields() {
		
	}

	public void setReferencedComponentUid(UUID uid) {
		referencedComponentUid = uid;
		try {
			referencedComponentNid = Terms.get().uuidToNative(uid);
		} catch (Exception e) {
		}
	}


	public UUID getReferencedComponentUid() {
		return referencedComponentUid;
	}

	public int getReferencedComponentNid() {
		return referencedComponentNid;
	}

}
