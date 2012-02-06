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
package org.ihtsdo.project.workflow.filters;

import java.io.IOException;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.workflow.model.WfInstance;

/**
 * The Class WfComponentFilter.
 */
public class WfComponentFilter implements WfSearchFilterBI {

	/** The wf instance text filter. */
	private String wfInstanceTextFilter;
	
	/** The TYPE. */
	private final String TYPE = "WF_COMPONENT_FILTER";

	/**
	 * Instantiates a new wf component filter.
	 *
	 * @param wfInstanceTextFilter the wf instance text filter
	 */
	public WfComponentFilter(String wfInstanceTextFilter) {
		super();
		this.wfInstanceTextFilter = wfInstanceTextFilter;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.workflow.filters.WfSearchFilterBI#filter(org.ihtsdo.project.workflow.model.WfInstance)
	 */
	@Override
	public boolean filter(WfInstance instance) {
		try {
			I_GetConceptData instanceComponent = Terms.get().getConcept(instance.getComponentId());
			return instanceComponent.getInitialText().contains(wfInstanceTextFilter);
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Sets the wf instance text filter.
	 *
	 * @param wfInstanceTextFilter the new wf instance text filter
	 */
	public void setWfInstanceTextFilter(String wfInstanceTextFilter) {
		this.wfInstanceTextFilter = wfInstanceTextFilter;
	}

	/**
	 * Gets the wf instance text filter.
	 *
	 * @return the wf instance text filter
	 */
	public String getWfInstanceTextFilter() {
		return wfInstanceTextFilter;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.workflow.filters.WfSearchFilterBI#getType()
	 */
	@Override
	public String getType() {
		return TYPE;
	}

}
