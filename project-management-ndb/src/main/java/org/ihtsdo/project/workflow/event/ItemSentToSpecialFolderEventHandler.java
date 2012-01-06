package org.ihtsdo.project.workflow.event;

public interface ItemSentToSpecialFolderEventHandler<T> extends EventHandler<ItemSentToSpecialFolderEvent>{
	@Override
	public abstract void handleEvent(ItemSentToSpecialFolderEvent event);
}
