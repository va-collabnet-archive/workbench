package org.ihtsdo.rf2.changeanalyzer.model;

public class Relationship {
	public Relationship(String rId, String active, String source,
			String sourceId, String destination, String type, String charType) {
		super();
		this.rId = rId;
		this.active = active;
		this.source = source;
		this.sourceId = sourceId;
		this.destination = destination;
		this.type = type;
		this.charType = charType;
	}
	String rId;
	String active;
	String source;
	String sourceId;
	String destination;
	String type;
	String charType;
	public String getrId() {
		return rId;
	}
	public void setrId(String rId) {
		this.rId = rId;
	}
	public String getActive() {
		return active;
	}
	public void setActive(String active) {
		this.active = active;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getSourceId() {
		return sourceId;
	}
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}
	public String getDestination() {
		return destination;
	}
	public void setDestination(String destination) {
		this.destination = destination;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getCharType() {
		return charType;
	}
	public void setCharType(String charType) {
		this.charType = charType;
	}
	
}
