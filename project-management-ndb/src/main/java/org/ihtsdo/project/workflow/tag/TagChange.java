package org.ihtsdo.project.workflow.tag;

public class TagChange {
	public static final String NEW_TAG_ADDED = "new tag created";
	public static final String ITEM_TAGGED = "item tagged";
	public static final String TAG_REMOVED = "untaggede";
	public static final String SPECIAL_TAG_ADDED = "outbox items changed";
	
	private String type;
	private InboxTag tag;
	
	public TagChange(String type, InboxTag tag) {
		super();
		this.type = type;
		this.tag = tag;
	}

	public void setTag(InboxTag tag) {
		this.tag = tag;
	}

	public InboxTag getTag() {
		return tag;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

}
