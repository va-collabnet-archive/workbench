package org.ihtsdo.translation.ui.event;

import org.ihtsdo.project.workflow.event.EventHandler;

public abstract class ItemStateChangedEventHandler<T> extends EventHandler<ItemStateChangedEvent>{
	public ItemStateChangedEventHandler(Object parent) {
		super(parent);
	}
	@Override
	public abstract void handleEvent(ItemStateChangedEvent event);
}
