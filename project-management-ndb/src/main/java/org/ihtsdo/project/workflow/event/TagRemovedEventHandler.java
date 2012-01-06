package org.ihtsdo.project.workflow.event;

public interface TagRemovedEventHandler<T> extends EventHandler<TagRemovedEvent>{
	@Override
	public abstract void handleEvent(TagRemovedEvent event);
}
