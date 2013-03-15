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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ihtsdo.tk.workflow.api.WfFilterBI;
import org.ihtsdo.tk.workflow.api.WfProcessInstanceBI;

/**
 * The Class WfStateFilter.
 */
public class WfCompletionFilter implements WfFilterBI {

	/** The TYPE. */
	private final String TYPE = "WF_STATE_FILTER";

	/** The state. */
	private CompletionOption completion;

	public WfCompletionFilter() {
		super();
	}

	/**
	 * Instantiates a new wf state filter.
	 * 
	 * @param state
	 *            the state
	 */
	public WfCompletionFilter(CompletionOption completion) {
		super();
		this.completion = completion;
	}

	public CompletionOption getCompletionOption() {
		return completion;
	}

	public void setCompletionOption(CompletionOption completion) {
		this.completion = completion;
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
		switch (completion){
		case INCOMPLETE_INSTACES:
			return !instance.isCompleted();
		case COMPLETE_INSTANCES:
			return instance.isCompleted();
		case ALL_INSTANCES:
			return true;
		}
		return false;
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
		return "completion";
	}

	public List<CompletionOption> getFilterOptions() {
		List<CompletionOption> completionValues = new ArrayList<CompletionOption>();
		completionValues.addAll(Arrays.asList(CompletionOption.values()));
		return completionValues;
	}

	public enum CompletionOption implements Serializable {

		ALL_INSTANCES("All Instances"), COMPLETE_INSTANCES("Complete Instances"), INCOMPLETE_INSTACES("Incomplete Instances");

		/** The name. */
		private final String name;

		/**
		 * Instantiates a new editor mode.
		 * 
		 * @param name
		 *            the name
		 */
		private CompletionOption(String name) {
			this.name = name;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		public String toString() {
			return this.name;
		}

	}
}
