package org.ihtsdo.workflow.refset;

import java.io.IOException;

import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.computer.refset.RefsetHelper;



/* 
* @author Jesse Efron
* 
*/
public abstract class RefsetUtility {
	protected String refsetName = null;
	protected int refsetId = 0;
	protected RefsetFields fields = null;
	protected RefsetHelper helper = null;

	protected RefsetUtility() throws TerminologyException, IOException 
	{
		helper = new RefsetHelper(Terms.get().getActiveAceFrameConfig());
	}
	
	protected void setRefsetId(int id) {
		refsetId = id;
	}
	
	protected void setRefsetName(String name) {
		refsetName = name;
	}
	
	protected int getRefsetId() {
		return refsetId;
	}
	
	protected String getRefsetName() {
		return refsetName;
	}
	
	protected void setFields(RefsetFields p) {
		fields = p;
	}
	
	protected RefsetFields getProperties() {
		return fields;
	}

	public String toString() {
		return "Refset: " + refsetName + " (refsetId: " + refsetId + ") with fields: " + 
			   "\n" + fields.toString();
	}
}
