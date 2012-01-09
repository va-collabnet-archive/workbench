package org.ihtsdo.project.workflow.tag;

import java.util.List;

public class InboxTag {
	private String tagName;
	private String color;
	private String textColor;
	private List<String> uuidList;

	public InboxTag(String tagName, String color,String textColor, List<String> uuidList) {
		super();
		this.tagName = tagName;
		this.uuidList = uuidList;
		this.textColor = textColor;
		this.color = color;
	}

	public void setTextColor(String textColor) {
		this.textColor = textColor;
	}

	public String getTextColor() {
		return textColor;
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public List<String> getUuidList() {
		return uuidList;
	}

	public void setUuidList(List<String> uuidList) {
		this.uuidList = uuidList;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getColor() {
		return color;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof InboxTag) {
			InboxTag t = (InboxTag) obj;
			if (this.tagName != null && this.color != null) {
				return this.tagName.equals(t.getTagName()) && this.getColor().equals(t.getColor());
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private String toItemString() {
		return TagManager.getInstance().getHeader(this);
	}

	@Override
	public String toString() {
		return this.toItemString();
	}

}
