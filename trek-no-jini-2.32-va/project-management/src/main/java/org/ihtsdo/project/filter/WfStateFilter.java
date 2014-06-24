/*
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.project.filter;

import java.util.ArrayList;
import java.util.List;

import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.model.WfState;
import org.ihtsdo.tk.workflow.api.WfFilterBI;
import org.ihtsdo.tk.workflow.api.WfProcessInstanceBI;
import org.ihtsdo.tk.workflow.api.WfStateBI;

/**
 * The Class WfStateFilter.
 */
public class WfStateFilter implements WfFilterBI {

	/** The TYPE. */
	private final String TYPE = "WF_STATE_FILTER";

	/** The state. */
	private WfState state;

	public WfStateFilter() {
		super();
	}

	/**
	 * Instantiates a new wf state filter.
	 * 
	 * @param state
	 *            the state
	 */
	public WfStateFilter(WfState state) {
		super();
		this.state = state;
	}

	/**
	 * Sets the state.
	 * 
	 * @param state
	 *            the new state
	 */
	public void setState(WfState state) {
		this.state = state;
	}

	/**
	 * Gets the state.
	 * 
	 * @return the state
	 */
	public WfState getState() {
		return state;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.project.workflow.filters.WfSearchFilterBI#filter(org.ihtsdo
	 * .project.workflow.model.WfInstance)
	 */
	@Override
	public boolean evaluateInstance(WfProcessInstanceBI instance) {
		return instance.getState().equals(state);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.project.workflow.filters.WfSearchFilterBI#getType()
	 */
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String toString() {
		return "current state";
	}

	public List<WfState> getFilterOptions() {
		List<WfState> users = new ArrayList<WfState>();
		users.addAll(new WfComponentProvider().getAllStates());
		return users;
	}

}
