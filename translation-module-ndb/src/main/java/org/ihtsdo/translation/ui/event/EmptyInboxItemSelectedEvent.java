package org.ihtsdo.translation.ui.event;

import org.ihtsdo.project.workflow.event.GenericEvent;

public class EmptyInboxItemSelectedEvent extends GenericEvent {

	public EmptyInboxItemSelectedEvent() {
		super();
	}

	@Override
	public EventType getAssociatedType() {
		return EventType.EMPTY_INBOX_ITEM_SELECTED;
	}

}
