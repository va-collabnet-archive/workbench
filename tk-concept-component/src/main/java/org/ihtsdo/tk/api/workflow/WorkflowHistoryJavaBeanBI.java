package org.ihtsdo.tk.api.workflow;

import java.io.IOException;
import java.util.UUID;

import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

public interface WorkflowHistoryJavaBeanBI {
	public void setConcept(UUID conceptUuid);
	public void setWorkflowId(UUID workflowUuid);
	public void setPath(UUID pathUuid);
	public void setModeler(UUID modelerUuid);
	public void setState(UUID stateUuid);
	public void setAction(UUID actionUuid);
	public void setFullySpecifiedName(String text);
	public void setEffectiveTime(Long effectiveTime);
	public void setWorkflowTime(Long workflowTime);
	public void setRefexMemberNid(int refexMemberNid);
	public void setAutoApproved(boolean autoApprove);
	public void setOverridden(boolean override);
	
	public UUID getConcept();
	public UUID getWorkflowId();
	public UUID getPath();
	public UUID getModeler();
	public UUID getState();
	public UUID getAction();
	public String getFullySpecifiedName();
	public Long getEffectiveTime();
	public Long getWorkflowTime();
	public boolean getAutoApproved();
	public boolean getOverridden();
	public int getRefexMemberNid();
	
	public String getStateForTitleBar(ViewCoordinate viewCoordinate) throws IOException;
	public String getModelerForTitleBar(ViewCoordinate viewCoordinate) throws IOException;
	
	public String toString();
}
