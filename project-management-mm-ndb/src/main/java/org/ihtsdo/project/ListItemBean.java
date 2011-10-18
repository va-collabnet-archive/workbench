package org.ihtsdo.project;

import org.dwfa.ace.api.I_GetConceptData;

public class ListItemBean {
	private ContextualizedDescription sourceFsn;
	private ContextualizedDescription sourcePrefered;
	private ContextualizedDescription targetPrefered;
	private I_GetConceptData status;
	
	public ContextualizedDescription getSourceFsn() {
		return sourceFsn;
	}
	
	public void setSourceFsn(ContextualizedDescription sourceFsn) {
		this.sourceFsn = sourceFsn;
	}
	
	public ContextualizedDescription getSourcePrefered() {
		return sourcePrefered;
	}
	
	public void setSourcePrefered(ContextualizedDescription sourcePrefered) {
		this.sourcePrefered = sourcePrefered;
	}
	
	public ContextualizedDescription getTargetPrefered() {
		return targetPrefered;
	}
	
	public void setTargetPrefered(ContextualizedDescription targetPref) {
		this.targetPrefered = targetPref;
	}
	
	public I_GetConceptData getStatus() {
		return status;
	}
	
	public void setStatus(I_GetConceptData status) {
		this.status = status;
	}
}
