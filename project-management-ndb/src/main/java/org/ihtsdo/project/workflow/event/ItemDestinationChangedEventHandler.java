package org.ihtsdo.project.workflow.event;

public interface ItemDestinationChangedEventHandler<T> extends EventHandler<ItemDestinationChangedEvent>{
	@Override
	public abstract void handleEvent(ItemDestinationChangedEvent event);
}
