package org.ihtsdo.translation.ui.config.event;

import org.ihtsdo.project.workflow.event.GenericEvent;

public class InboxColumnsChangedEvent extends GenericEvent {

	public InboxColumnsChangedEvent() {
		super();
	}

	@Override
	public EventType getAssociatedType() {
		return EventType.INBOX_COLUMNS_CHANGED;
	}
}
