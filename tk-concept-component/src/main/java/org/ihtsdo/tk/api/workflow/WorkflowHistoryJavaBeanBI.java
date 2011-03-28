package org.ihtsdo.tk.api.workflow;

import java.util.UUID;


public interface WorkflowHistoryJavaBeanBI {
	public void setConcept(UUID id);
	public void setWorkflowId(UUID id);
	public void setPath(UUID id);
	public void setModeler(UUID id);
	public void setState(UUID id);
	public void setAction(UUID id);
	public void setFSN(String desc);
	public void setEffectiveTime(Long t);
	public void setWorkflowTime(Long t);
	
	public UUID getConcept();
	public UUID getWorkflowId();
	public UUID getPath();
	public UUID getModeler();
	public UUID getState();
	public UUID getAction();
	public String getFSN();
	public Long getEffectiveTime();
	public Long getWorkflowTime();
	public boolean getAutoApproved();
	public boolean getOverridden();
	
	public String toString();
}
