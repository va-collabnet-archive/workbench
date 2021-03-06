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

import org.ihtsdo.project.ContextualizedDescription;
import org.ihtsdo.project.view.event.GenericEvent;

/**
 * The Class EmptyInboxItemSelectedEvent.
 */
public class TargetTableItemSelectedEvent extends GenericEvent {

	private ContextualizedDescription description;
	private int last;
	
	/**
	 * Instantiates a new empty inbox item selected event.
	 */
	public TargetTableItemSelectedEvent(ContextualizedDescription description, int last) {
		super();
		this.setDescription(description);
		this.last = last;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.workflow.event.GenericEvent#getAssociatedType()
	 */
	@Override
	public EventType getAssociatedType() {
		return EventType.TARGET_TABLE_ITEM_SELECTED;
	}

	public ContextualizedDescription getDescription() {
		return description;
	}

	public void setDescription(ContextualizedDescription description) {
		this.description = description;
	}

	public int getLast() {
		return last;
	}

	public void setLast(int last) {
		this.last = last;
	}

}
