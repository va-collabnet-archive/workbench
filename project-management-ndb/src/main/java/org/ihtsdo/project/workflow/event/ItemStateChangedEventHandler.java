package org.ihtsdo.project.workflow.event;

public interface ItemStateChangedEventHandler<T> extends EventHandler<ItemStateChangedEvent>{
	@Override
	public abstract void handleEvent(ItemStateChangedEvent event);
}
