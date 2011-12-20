package org.ihtsdo.project.workflow.filters;

import org.ihtsdo.project.workflow.model.WfInstance;

public interface WfSearchFilterBI {
	public boolean filter(WfInstance instance);
	public String getType();
}
