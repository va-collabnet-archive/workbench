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

import java.io.IOException;
import java.util.UUID;

import org.apache.tools.ant.taskdefs.rmic.WLRmic;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.project.filter.WfDestinationFilter;
import org.ihtsdo.project.filter.WfStateFilter;
import org.ihtsdo.project.filter.WfWorklistFilter;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.workflow.model.WfState;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.tk.workflow.api.WfFilterBI;

/**
 * A factory for creating Filter objects.
 */
public class FilterFactory {
	
	/** The instance. */
	private static FilterFactory instance;

	/**
	 * Instantiates a new filter factory.
	 */
	private FilterFactory() {
	}

	/**
	 * Creates a new Filter object.
	 *
	 * @param obj the obj
	 * @return the wf search filter bi
	 */
	public WfFilterBI createFilterFromObject(Object obj) {
		WfFilterBI filter = null;
		if (obj instanceof WfState) {
			filter = new WfStateFilter((WfState) obj);
		} else if (obj instanceof WorkList) {
			WorkList wl = (WorkList) obj;
			filter = new WfWorklistFilter(wl.getUuid());
		} else if (obj instanceof InboxTag) {
			InboxTag tag = (InboxTag) obj;
			InboxTag filterTag = null;
			try {
				filterTag = TagManager.getInstance().getTagContent(tag.getTagName());
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
			filter = new WfTagFilter(filterTag);
		}
		return filter;
	}


	/**
	 * Creates a new Filter object.
	 *
	 * @param username the username
	 * @param id the id
	 * @return the wf destination filter
	 */
	public WfDestinationFilter createDestinationFilter(String username, UUID id) {
		WfUser destination = new WfUser(username, id);
		return new WfDestinationFilter(destination);
	}

	/**
	 * Creates a new Filter object.
	 *
	 * @param name the name
	 * @param id the id
	 * @return the wf state filter
	 */
	public WfStateFilter createWfStateFilter(String name, UUID id) {
		WfState state = new WfState(name, id);
		return new WfStateFilter(state);
	}

	/**
	 * Creates a new Filter object.
	 *
	 * @param worklistUUID the worklist uuid
	 * @return the wf worklist filter
	 */
	public WfWorklistFilter createWorklistFilter(UUID worklistUUID) {
		return new WfWorklistFilter(worklistUUID);
	}

	/**
	 * Gets the single instance of FilterFactory.
	 *
	 * @return single instance of FilterFactory
	 */
	public static FilterFactory getInstance() {
		if (instance != null) {
			return instance;
		} else {
			instance = new FilterFactory();
			return instance;
		}
	}
}
