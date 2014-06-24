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
package org.ihtsdo.translation.workflow.filters;

import java.io.IOException;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.workflow.api.WfFilterBI;
import org.ihtsdo.tk.workflow.api.WfProcessInstanceBI;

/**
 * The Class WfDefaultDescFilter.
 */
public class WfDefaultDescFilter implements WfFilterBI {

	/** The wf instance text filter. */
	private String wfInstanceTextFilter;
	/** The TYPE. */
	private final String TYPE = "WF_DEFAULT_DESC_FILTER";

	/**
	 * Instantiates a new wf component filter.
	 * 
	 * @param wfInstanceTextFilter
	 *            the wf instance text filter
	 */
	public WfDefaultDescFilter(String wfInstanceTextFilter) {
		super();
		this.wfInstanceTextFilter = wfInstanceTextFilter;
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
		try {
			I_GetConceptData concept = Terms.get().getConcept(instance.getComponentPrimUuid());
			return concept.toString().toLowerCase().contains(wfInstanceTextFilter.toLowerCase());
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Sets the wf instance text filter.
	 * 
	 * @param wfInstanceTextFilter
	 *            the new wf instance text filter
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.project.workflow.filters.WfSearchFilterBI#getType()
	 */
	@Override
	public String getType() {
		return TYPE;
	}

}
