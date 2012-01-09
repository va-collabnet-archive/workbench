package org.ihtsdo.translation.ui.event;

import org.ihtsdo.project.workflow.event.EventHandler;

public abstract class InboxItemSelectedEventHandler<T> extends EventHandler<InboxItemSelectedEvent>{
	public InboxItemSelectedEventHandler(Object parent) {
		super(parent);
	}

	@Override
	public abstract void handleEvent(InboxItemSelectedEvent event);
}
