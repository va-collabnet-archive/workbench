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
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.workflow.api.WfFilterBI;
import org.ihtsdo.tk.workflow.api.WfProcessInstanceBI;

/**
 * The Class WfIsKindOfFilter.
 */
public class WfIsKindOfFilter implements WfFilterBI {

	/** The TYPE. */
	private final String TYPE = "WF_ISKINDOF_FILTER";

	/** The worklist uuid. */
	private int parentNid;

	public WfIsKindOfFilter() {
	}

	public WfIsKindOfFilter(int parentNid) {
		super();
		this.parentNid = parentNid;
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
			int childnid = Ts.get().getNidForUuids(instance.getComponentPrimUuid());
			return Ts.get().isKindOf(childnid, parentNid, Terms.get().getActiveAceFrameConfig().getViewCoordinate());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ContradictionException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		return false;
	}

	public int getParentNid() {
		return parentNid;
	}

	public void setParentNid(int parentNid) {
		this.parentNid = parentNid;
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
		return "ancestor";
	}

}
