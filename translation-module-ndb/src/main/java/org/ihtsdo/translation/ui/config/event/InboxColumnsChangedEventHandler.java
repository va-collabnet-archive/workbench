package org.ihtsdo.translation.ui.config.event;

import org.ihtsdo.project.workflow.event.EventHandler;

public abstract class InboxColumnsChangedEventHandler<T> extends EventHandler<InboxColumnsChangedEvent>{
	public InboxColumnsChangedEventHandler(Object parent) {
		super(parent);
	}

	@Override
	public abstract void handleEvent(InboxColumnsChangedEvent event);
}
