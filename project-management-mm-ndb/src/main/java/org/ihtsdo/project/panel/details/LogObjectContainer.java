package org.ihtsdo.project.panel.details;

import org.dwfa.ace.api.I_AmPart;

public class LogObjectContainer {
	public enum PARTS{PROMOTION_PART,DESCRIPTION_PART,LANG_REFSET_PART,COMMENTS_PART,ISSUE_PART};
	private PARTS partType;

	private I_AmPart part;
	private String stringPart;
	private Integer did;

	private String userName;

	public LogObjectContainer(PARTS logObjectContainerType, I_AmPart part, Integer did){
		this.partType=logObjectContainerType;
		this.part=part;
		this.did=did;
	}
	public LogObjectContainer(PARTS logObjectContainerType, String stringPart){
		this.partType=logObjectContainerType;
		this.stringPart=stringPart;
	}
	public LogObjectContainer(PARTS logObjectContainerType, String stringPart,String userName){
		this.partType=logObjectContainerType;
		this.stringPart=stringPart;
		this.userName=userName;
	}

	public PARTS getPartType() {
		return partType;
	}

	public I_AmPart getPart() {
		return part;
	}
	public String getStringPart() {
		return stringPart;
	}
	public Integer getDid() {
		return did;
	}
	public String getUserName() {
		return userName;
	}


}
