package org.ihtsdo.project.workflow.filters;

import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.tag.InboxTag;

public class WfTagFilter implements WfSearchFilterBI {
	private final String TYPE = "WF_TAG_FILTER";
	private InboxTag tag;

	public WfTagFilter(InboxTag tag) {
		this.tag = tag;
	}

	@Override
	public boolean filter(WfInstance instance) {
		return tag.getUuidList().contains(instance.getComponentId().toString());
	}

	@Override
	public String getType() {
		return TYPE;
	}

	public void setTag(InboxTag tag) {
		this.tag = tag;
	}

	public InboxTag getTag() {
		return tag;
	}
}
