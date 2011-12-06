package org.ihtsdo.project.workflow.api;

import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfUser;

public class DestinationFilter implements WorkflowSearchFilterBI {

	private WfUser destination;

	public DestinationFilter(WfUser destination) {
		super();
		this.destination = destination;
	}

	@Override
	public boolean filter(WfInstance instance) {
		if (instance.getDestination().equals(destination)) {
			return true;
		} else {
			return false;
		}
	}

	public WfUser getDestination() {
		return destination;
	}

	public void setDestination(WfUser destination) {
		this.destination = destination;
	}

}
