package org.ihtsdo.project.workflow.event;


public abstract class OutboxContentChangedEventHandler<T> extends EventHandler<OutboxContentChangeEvent>{
	public OutboxContentChangedEventHandler(Object parent) {
		super(parent);
	}

	@Override
	public abstract void handleEvent(OutboxContentChangeEvent event);
}
