package org.ihtsdo.project.workflow.filters;

import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfState;

public class WfStateFilter implements WfSearchFilterBI{
	private final String TYPE = "WF_STATE_FILTER";
	private WfState state;

	public WfStateFilter(WfState state) {
		super();
		this.state = state;
	}

	public void setState(WfState state) {
		this.state = state;
	}

	public WfState getState() {
		return state;
	}

	@Override
	public boolean filter(WfInstance instance) {
		return instance.getState().equals(state);
	}

	@Override
	public String getType() {
		return TYPE;
	}
	
}
