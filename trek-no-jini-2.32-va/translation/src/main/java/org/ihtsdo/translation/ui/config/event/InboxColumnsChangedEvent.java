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
package org.ihtsdo.translation.ui.config.event;

import java.util.LinkedHashSet;

import org.ihtsdo.project.view.event.GenericEvent;
import org.ihtsdo.translation.ui.ConfigTranslationModule.InboxColumn;

/**
 * The Class InboxColumnsChangedEvent.
 */
public class InboxColumnsChangedEvent extends GenericEvent {

	/** The inbox column components. */
	private LinkedHashSet<InboxColumn> inboxColumnComponents;

	/**
	 * Instantiates a new inbox columns changed event.
	 *
	 * @param inboxColumnComponents the inbox column components
	 */
	public InboxColumnsChangedEvent(LinkedHashSet<InboxColumn> inboxColumnComponents) {
		super();
		this.inboxColumnComponents = inboxColumnComponents;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.workflow.event.GenericEvent#getAssociatedType()
	 */
	@Override
	public EventType getAssociatedType() {
		return EventType.INBOX_COLUMNS_CHANGED;
	}

	/**
	 * Sets the inbox column components.
	 *
	 * @param inboxColumnComponents the new inbox column components
	 */
	public void setInboxColumnComponents(LinkedHashSet<InboxColumn> inboxColumnComponents) {
		this.inboxColumnComponents = inboxColumnComponents;
	}

	/**
	 * Gets the inbox column components.
	 *
	 * @return the inbox column components
	 */
	public LinkedHashSet<InboxColumn> getInboxColumnComponents() {
		return inboxColumnComponents;
	}
}
