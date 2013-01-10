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

import java.util.List;
import java.util.UUID;

import org.ihtsdo.project.workflow.model.WfInstance;

/**
 * The Class WfWorklistFilter.
 */
public class WfWorklistFilter implements WfSearchFilterBI {
	
	/** The TYPE. */
	private final String TYPE = "WF_WORKLIST_FILTER";
	
	/** The worklist uuid. */
	private List<UUID> worklistUUID;

	/**
	 * Instantiates a new wf worklist filter.
	 *
	 * @param worklistUUID the worklist uuid
	 */
	public WfWorklistFilter(List<UUID> worklistUUID) {
		super();
		this.worklistUUID = worklistUUID;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.workflow.filters.WfSearchFilterBI#filter(org.ihtsdo.project.workflow.model.WfInstance)
	 */
	@Override
	public boolean filter(WfInstance instance) {
		return this.worklistUUID.contains(instance.getWorkList().getUids().get(0));
	}

	/**
	 * Gets the worklist uuid.
	 *
	 * @return the worklist uuid
	 */
	public List<UUID> getWorklistUUID() {
		return worklistUUID;
	}

	/**
	 * Sets the worklist uuid.
	 *
	 * @param worklistUUID the new worklist uuid
	 */
	public void setWorklistUUID(List<UUID> worklistUUID) {
		this.worklistUUID = worklistUUID;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.workflow.filters.WfSearchFilterBI#getType()
	 */
	@Override
	public String getType() {
		return TYPE;
	}
	
}
