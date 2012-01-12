package org.ihtsdo.translation.ui.event;

import org.ihtsdo.project.workflow.event.EventHandler;

public abstract class ItemRemovedFromTodoEventHandler<T> extends EventHandler<ItemRemovedFromTodoEvent>{
	public ItemRemovedFromTodoEventHandler(Object parent) {
		super(parent);
	}
	@Override
	public abstract void handleEvent(ItemRemovedFromTodoEvent event);
}
