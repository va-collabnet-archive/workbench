package org.ihtsdo.project.workflow.api;

import org.ihtsdo.project.workflow.model.WfInstance;

public interface WorkflowSearchFilterBI {
	
	public boolean filter(WfInstance instance);

}
