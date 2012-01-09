package org.ihtsdo.project.workflow.event;

public abstract class ItemTaggedEventHandler<T> extends EventHandler<ItemTaggedEvent>{
	public ItemTaggedEventHandler(Object parent) {
		super(parent);
	}

	@Override
	public abstract void handleEvent(ItemTaggedEvent event);
}
