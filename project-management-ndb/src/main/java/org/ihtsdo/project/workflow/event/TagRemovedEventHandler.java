package org.ihtsdo.project.workflow.event;

public abstract class TagRemovedEventHandler<T> extends EventHandler<TagRemovedEvent>{
	public TagRemovedEventHandler(Object parent) {
		super(parent);
		// TODO Auto-generated constructor stub
	}

	@Override
	public abstract void handleEvent(TagRemovedEvent event);
}
