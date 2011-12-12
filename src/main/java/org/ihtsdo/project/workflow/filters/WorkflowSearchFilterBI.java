package org.ihtsdo.project.workflow.filters;

import org.ihtsdo.project.workflow.model.WfInstance;

public interface WorkflowSearchFilterBI {
	public boolean filter(WfInstance instance);
}
