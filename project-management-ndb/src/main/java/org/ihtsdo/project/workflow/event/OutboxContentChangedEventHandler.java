package org.ihtsdo.project.workflow.event;

public interface OutboxContentChangedEventHandler<T> extends EventHandler<OutboxContentChangeEvent>{
	@Override
	public void handleEvent(OutboxContentChangeEvent event);
}
