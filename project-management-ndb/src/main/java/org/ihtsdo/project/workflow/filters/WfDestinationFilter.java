package org.ihtsdo.project.workflow.filters;

import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfUser;

public class WfDestinationFilter implements WfSearchFilterBI {
	public final String TYPE = "WF_DESTIANTION_FILTER";
	private WfUser destination;
	
	public WfDestinationFilter() {
		super();
	}

	public WfDestinationFilter(WfUser destination) {
		super();
		this.destination = destination;
	}

	public WfUser getDestination() {
		return destination;
	}

	public void setDestination(WfUser destination) {
		this.destination = destination;
	}

	@Override
	public boolean filter(WfInstance instance) {
		return instance.getDestination().equals(destination);
	}

	@Override
	public String getType() {
		return TYPE;
	}

}
