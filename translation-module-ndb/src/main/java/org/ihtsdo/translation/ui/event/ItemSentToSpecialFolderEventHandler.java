package org.ihtsdo.translation.ui.event;

import org.ihtsdo.project.workflow.event.EventHandler;

public abstract class ItemSentToSpecialFolderEventHandler<T> extends EventHandler<ItemSentToSpecialFolderEvent>{
	public ItemSentToSpecialFolderEventHandler(Object parent) {
		super(parent);
	}

	@Override
	public abstract void handleEvent(ItemSentToSpecialFolderEvent event);
}
