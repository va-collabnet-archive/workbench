package org.ihtsdo.project.workflow.event;

public interface ItemTaggedEventHandler<T> extends EventHandler<ItemTaggedEvent>{
	@Override
	public abstract void handleEvent(ItemTaggedEvent event);
}
