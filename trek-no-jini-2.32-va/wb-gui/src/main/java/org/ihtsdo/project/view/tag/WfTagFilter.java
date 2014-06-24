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
package org.ihtsdo.project.view.tag;

import org.ihtsdo.tk.workflow.api.WfFilterBI;
import org.ihtsdo.tk.workflow.api.WfProcessInstanceBI;

/**
 * The Class WfTagFilter.
 */
public class WfTagFilter implements WfFilterBI {
	
	/** The TYPE. */
	private final String TYPE = "WF_TAG_FILTER";
	
	/** The tag. */
	private InboxTag tag;

	/**
	 * Instantiates a new wf tag filter.
	 *
	 * @param tag the tag
	 */
	public WfTagFilter(InboxTag tag) {
		this.tag = tag;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.workflow.filters.WfSearchFilterBI#filter(org.ihtsdo.project.workflow.model.WfInstance)
	 */
	@Override
	public boolean evaluateInstance(WfProcessInstanceBI instance) {
		boolean result = false;
		for (String[] uuidList : tag.getUuidList()) {
			if(TagManager.getTagWorklistConceptUuids(instance)[InboxTag.TERM_WORKLIST_UUID_INDEX].equals(uuidList[InboxTag.TERM_WORKLIST_UUID_INDEX]) 
					&& TagManager.getTagWorklistConceptUuids(instance)[InboxTag.TERM_UUID_INDEX].equals(uuidList[InboxTag.TERM_UUID_INDEX])){
				result = true;
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.workflow.filters.WfSearchFilterBI#getType()
	 */
	@Override
	public String getType() {
		return TYPE;
	}

	/**
	 * Sets the tag.
	 *
	 * @param tag the new tag
	 */
	public void setTag(InboxTag tag) {
		this.tag = tag;
	}

	/**
	 * Gets the tag.
	 *
	 * @return the tag
	 */
	public InboxTag getTag() {
		return tag;
	}
}
