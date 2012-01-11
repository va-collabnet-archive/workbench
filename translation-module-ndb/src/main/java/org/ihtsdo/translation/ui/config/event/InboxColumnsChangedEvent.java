package org.ihtsdo.translation.ui.config.event;

import java.util.LinkedHashSet;

import org.ihtsdo.project.workflow.event.GenericEvent;
import org.ihtsdo.translation.ui.ConfigTranslationModule.InboxColumn;

public class InboxColumnsChangedEvent extends GenericEvent {

	private LinkedHashSet<InboxColumn> inboxColumnComponents;

	public InboxColumnsChangedEvent(LinkedHashSet<InboxColumn> inboxColumnComponents) {
		super();
		this.inboxColumnComponents = inboxColumnComponents;
	}

	@Override
	public EventType getAssociatedType() {
		return EventType.INBOX_COLUMNS_CHANGED;
	}

	public void setInboxColumnComponents(LinkedHashSet<InboxColumn> inboxColumnComponents) {
		this.inboxColumnComponents = inboxColumnComponents;
	}

	public LinkedHashSet<InboxColumn> getInboxColumnComponents() {
		return inboxColumnComponents;
	}
}
