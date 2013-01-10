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

import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfUser;

/**
 * The Class WfDestinationFilter.
 */
public class WfDestinationFilter implements WfSearchFilterBI {
	
	/** The TYPE. */
	public final String TYPE = "WF_DESTIANTION_FILTER";
	
	/** The destination. */
	private WfUser destination;
	
	/**
	 * Instantiates a new wf destination filter.
	 */
	public WfDestinationFilter() {
		super();
	}

	/**
	 * Instantiates a new wf destination filter.
	 *
	 * @param destination the destination
	 */
	public WfDestinationFilter(WfUser destination) {
		super();
		this.destination = destination;
	}

	/**
	 * Gets the destination.
	 *
	 * @return the destination
	 */
	public WfUser getDestination() {
		return destination;
	}

	/**
	 * Sets the destination.
	 *
	 * @param destination the new destination
	 */
	public void setDestination(WfUser destination) {
		this.destination = destination;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.workflow.filters.WfSearchFilterBI#filter(org.ihtsdo.project.workflow.model.WfInstance)
	 */
	@Override
	public boolean filter(WfInstance instance) {
		return instance.getDestination().equals(destination);
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.workflow.filters.WfSearchFilterBI#getType()
	 */
	@Override
	public String getType() {
		return TYPE;
	}

}
