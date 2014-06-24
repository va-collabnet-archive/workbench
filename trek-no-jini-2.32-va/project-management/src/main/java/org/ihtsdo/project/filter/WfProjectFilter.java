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
import java.util.UUID;

import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.tk.workflow.api.WfFilterBI;
import org.ihtsdo.tk.workflow.api.WfProcessInstanceBI;

/**
 * The Class WfWorklistFilter.
 */
public class WfProjectFilter implements WfFilterBI {

	/** The TYPE. */
	private final String TYPE = "WF_PROJECT_FILTER";

	/** The worklist uuid. */
	private UUID projectUUID;

	public WfProjectFilter() {
	}

	public WfProjectFilter(UUID projectUUID) {
		super();
		this.projectUUID = projectUUID;
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
			I_TerminologyProject project = TerminologyProjectDAO.getProjectForWorklist((WorkList)instance.getWorkList(), Terms.get().getActiveAceFrameConfig());
			return projectUUID.equals(project.getUids().iterator().next());
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public UUID getProjectUUID() {
		return projectUUID;
	}

	public void setProjectUUID(UUID worklistUUID) {
		this.projectUUID = worklistUUID;
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
		return "project";
	}

}
