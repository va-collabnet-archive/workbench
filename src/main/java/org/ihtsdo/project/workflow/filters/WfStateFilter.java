package org.ihtsdo.project.workflow.filters;

import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfState;

public class WfStateFilter implements WfSearchFilterBI{
	private WfState state;

	private WfStateFilter(WfState state) {
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
	
}
