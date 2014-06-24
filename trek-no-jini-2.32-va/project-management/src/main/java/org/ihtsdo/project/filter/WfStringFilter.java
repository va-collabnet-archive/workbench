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

import java.io.IOException;

import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.workflow.api.WfFilterBI;
import org.ihtsdo.tk.workflow.api.WfProcessInstanceBI;

/**
 * The Class WfStringFilter.
 */
public class WfStringFilter implements WfFilterBI {

	/** The TYPE. */
	private final String TYPE = "WF_STRING_FILTER";

	/** The worklist uuid. */
	private String string;

	public WfStringFilter() {
	}

	public WfStringFilter(String string) {
		super();
		this.string = string;
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
			ConceptVersionBI concept = Ts.get().getConceptVersion(Terms.get().getActiveAceFrameConfig().getViewCoordinate(), instance.getComponentPrimUuid());
			return concept.getDescriptionPreferred().getText().toLowerCase().contains(string.toLowerCase());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public String getParentNid() {
		return string;
	}

	public void setParentNid(String string) {
		this.string = string;
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
		return "term";
	}

}
