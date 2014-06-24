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
package org.ihtsdo.translation.ui.inbox.event;

import org.ihtsdo.project.view.event.GenericEvent;

/**
 * The Class InboxItemSelectedEvent.
 */
public class InboxItemSelectedEvent extends GenericEvent {

	/** The old inbox item. */
	private Object oldInboxItem;
	
	/** The inbox item. */
	private Object inboxItem;

	/**
	 * Instantiates a new inbox item selected event.
	 *
	 * @param oldInboxItem the old inbox item
	 * @param inboxItem the inbox item
	 */
	public InboxItemSelectedEvent(Object oldInboxItem, Object inboxItem) {
		super();
		this.oldInboxItem = oldInboxItem;
		this.inboxItem = inboxItem;
	}

	/**
	 * Gets the inbox item.
	 *
	 * @return the inbox item
	 */
	public Object getInboxItem() {
		return inboxItem;
	}

	/**
	 * Sets the inbox item.
	 *
	 * @param inboxItem the new inbox item
	 */
	public void setInboxItem(Object inboxItem) {
		this.inboxItem = inboxItem;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.workflow.event.GenericEvent#getAssociatedType()
	 */
	@Override
	public EventType getAssociatedType() {
		return EventType.INBOX_ITEM_SELECTED;
	}

	/**
	 * Sets the old inbox item.
	 *
	 * @param oldInboxItem the new old inbox item
	 */
	public void setOldInboxItem(Object oldInboxItem) {
		this.oldInboxItem = oldInboxItem;
	}

	/**
	 * Gets the old inbox item.
	 *
	 * @return the old inbox item
	 */
	public Object getOldInboxItem() {
		return oldInboxItem;
	}

}
