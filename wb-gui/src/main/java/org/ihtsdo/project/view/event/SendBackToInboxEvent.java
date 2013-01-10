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
package org.ihtsdo.project.view.event;

import org.ihtsdo.project.workflow.model.WfInstance;

/**
 * The Class SendBackToInboxEvent.
 */
public class SendBackToInboxEvent extends GenericEvent {

	/** The new instance. */
	private WfInstance newInstance;

	/**
	 * Instantiates a new send back to inbox event.
	 *
	 * @param newInstance the new instance
	 */
	public SendBackToInboxEvent(WfInstance newInstance) {
		super();
		this.newInstance = newInstance;
	}

	/**
	 * Gets the new instance.
	 *
	 * @return the new instance
	 */
	public WfInstance getNewInstance() {
		return newInstance;
	}

	/**
	 * Sets the new instance.
	 *
	 * @param newInstance the new new instance
	 */
	public void setNewInstance(WfInstance newInstance) {
		this.newInstance = newInstance;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.workflow.event.GenericEvent#getAssociatedType()
	 */
	@Override
	public EventType getAssociatedType() {
		return EventType.SEND_BACK_TO_INBOX;
	}

}
