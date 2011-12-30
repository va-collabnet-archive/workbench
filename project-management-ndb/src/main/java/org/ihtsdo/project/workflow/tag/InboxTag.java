package org.ihtsdo.project.workflow.tag;

import java.util.List;

public class InboxTag {
	private String tagName;
	private String color;
	private List<String> uuidList;

	public InboxTag(String tagName, String color, List<String> uuidList) {
		super();
		this.tagName = tagName;
		this.uuidList = uuidList;
		this.color = color;
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
			return this.tagName.equals(t.getTagName()) && this.getColor().equals(t.getColor());
		} else {
			return false;
		}
	}
	
	private String toItemString(){
		return "<html><body><table><tr><td style=\"background-color:" + this.color + ";width:10px; height: 10px;\"><td>" + this.tagName;
	}

	@Override
	public String toString() {
		return this.toItemString();
	}

}
