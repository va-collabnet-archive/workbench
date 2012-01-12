package org.ihtsdo.translation.ui.event;

import org.ihtsdo.project.workflow.event.EventHandler;

public abstract class EmptyInboxItemSelectedEventHandler<T> extends EventHandler<EmptyInboxItemSelectedEvent>{
	public EmptyInboxItemSelectedEventHandler(Object parent) {
		super(parent);
	}

	@Override
	public abstract void handleEvent(EmptyInboxItemSelectedEvent event);
}
