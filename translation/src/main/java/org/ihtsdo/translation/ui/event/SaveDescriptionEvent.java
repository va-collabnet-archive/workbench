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

import org.ihtsdo.project.view.event.GenericEvent;
import org.ihtsdo.project.workflow.model.WfInstance;

/**
 * The Class EmptyInboxItemSelectedEvent.
 */
public class SaveDescriptionEvent extends GenericEvent {
	public enum ActionType{
		SEND_TO_OUTBOX_LAUNCHED,
		ACTION_LAUNCHED,
		NO_ACTION,
		NEXT_ITEM;
	}

	private WfInstance previousItem;
	private WfInstance newItem;
	private ActionType type;

	public SaveDescriptionEvent(WfInstance previousItem, WfInstance newItem, ActionType type) {
		super();
		this.previousItem = previousItem;
		this.newItem = newItem;
		this.type = type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.project.workflow.event.GenericEvent#getAssociatedType()
	 */
	@Override
	public EventType getAssociatedType() {
		return EventType.SAVE_DESCRIPTION;
	}

	public WfInstance getPreviousItem() {
		return previousItem;
	}

	public void setPreviousItem(WfInstance previousItem) {
		this.previousItem = previousItem;
	}

	public WfInstance getNewItem() {
		return newItem;
	}

	public void setNewItem(WfInstance newItem) {
		this.newItem = newItem;
	}

	public ActionType getType() {
		return type;
	}

	public void setType(ActionType type) {
		this.type = type;
	}

}
