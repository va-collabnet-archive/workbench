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

/**
 * The Class EmptyInboxItemSelectedEvent.
 */
public class ClearDescriptionPanelEvent extends GenericEvent {

	private Boolean readOnlyMode;

	/**
	 * Instantiates a new empty inbox item selected event.
	 */
	public ClearDescriptionPanelEvent(Boolean readOnlyMode) {
		super();
		this.readOnlyMode = readOnlyMode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.project.workflow.event.GenericEvent#getAssociatedType()
	 */
	@Override
	public EventType getAssociatedType() {
		return EventType.CLEAR_DESCRIPTION_PANEL_EVENT;
	}

	public Boolean getReadOnlyMode() {
		return readOnlyMode;
	}

	public void setReadOnlyMode(Boolean readOnlyMode) {
		this.readOnlyMode = readOnlyMode;
	}

}