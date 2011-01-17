package org.ihtsdo.tk.api.workflow;

import java.util.UUID;


public interface WorkflowHistoryJavaBeanBI {
	public void setWorkflowId(UUID id);
	public void setConceptId(UUID id);
	public void setUseCase(UUID id);
	public void setPath(UUID id);
	public void setModeler(UUID id);
	public void setState(UUID id);
	public void setAction(UUID id);
	public void setFSN(String desc);
	public void setTimeStamp(Long t);
	public void setRefsetColumnTimeStamp(Long t);
	
	public UUID getWorkflowId();
	public UUID getConceptId();
	public UUID getUseCase();
	public UUID getPath();
	public	UUID getModeler();
	public UUID getState();
	public UUID getAction();
	public String getFSN();
	public Long getTimeStamp();
	public Long getRefsetColumnTimeStamp();
	
	public String toString();
	public boolean getAutoApproved();
	public boolean getOverridden();
}
