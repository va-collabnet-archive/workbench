package org.ihtsdo.issue;

import java.io.Serializable;

public class IssueDependency implements Serializable {

	private Issue issueTarget;
	
	private String Description;
	
	private static final long serialVersionUID = 1L;


	public Issue getTargetIssue() {
		return issueTarget;
	}

	public void setTargetIssue(Issue issueTarget) {
		this.issueTarget = issueTarget;
	}

	public String getDescription() {
		return Description;
	}

	public void setDescription(String description) {
		Description = description;
	}
	
}
