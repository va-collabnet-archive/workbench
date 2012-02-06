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
package org.ihtsdo.translation.ui.event;

import org.ihtsdo.project.workflow.event.GenericEvent;
import org.ihtsdo.project.workflow.model.WfInstance;

/**
 * The Class ItemDestinationChangedEvent.
 */
public class ItemDestinationChangedEvent extends GenericEvent {

	/** The wf instance. */
	private WfInstance wfInstance;

	/**
	 * Instantiates a new item destination changed event.
	 *
	 * @param wfInstance the wf instance
	 */
	public ItemDestinationChangedEvent(WfInstance wfInstance) {
		super();
		this.wfInstance = wfInstance;
	}

	/**
	 * Gets the wf instance.
	 *
	 * @return the wf instance
	 */
	public WfInstance getWfInstance() {
		return wfInstance;
	}

	/**
	 * Sets the wf instance.
	 *
	 * @param wfInstance the new wf instance
	 */
	public void setWfInstance(WfInstance wfInstance) {
		this.wfInstance = wfInstance;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.workflow.event.GenericEvent#getAssociatedType()
	 */
	@Override
	public EventType getAssociatedType() {
		return EventType.ITEM_STATE_CHANGED;
	}

}
