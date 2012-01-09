package org.ihtsdo.translation.ui.event;

import org.ihtsdo.project.workflow.event.EventHandler;

public abstract class ItemDestinationChangedEventHandler<T> extends EventHandler<ItemDestinationChangedEvent>{
	public ItemDestinationChangedEventHandler(Object parent) {
		super(parent);
	}

	@Override
	public abstract void handleEvent(ItemDestinationChangedEvent event);
}
