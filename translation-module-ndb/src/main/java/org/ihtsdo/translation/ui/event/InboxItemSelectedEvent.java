package org.ihtsdo.translation.ui.event;

import org.ihtsdo.project.workflow.event.GenericEvent;

public class InboxItemSelectedEvent extends GenericEvent {

	private Object oldInboxItem;
	private Object inboxItem;

	public InboxItemSelectedEvent(Object oldInboxItem, Object inboxItem) {
		super();
		this.oldInboxItem = oldInboxItem;
		this.inboxItem = inboxItem;
	}

	public Object getInboxItem() {
		return inboxItem;
	}

	public void setInboxItem(Object inboxItem) {
		this.inboxItem = inboxItem;
	}

	@Override
	public EventType getAssociatedType() {
		return EventType.INBOX_ITEM_SELECTED;
	}

	public void setOldInboxItem(Object oldInboxItem) {
		this.oldInboxItem = oldInboxItem;
	}

	public Object getOldInboxItem() {
		return oldInboxItem;
	}
}
