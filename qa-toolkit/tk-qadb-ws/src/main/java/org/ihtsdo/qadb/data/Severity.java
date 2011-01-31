package org.ihtsdo.qadb.data;

import java.io.Serializable;

public class Severity implements Serializable {

	private static final long serialVersionUID = 6249417883634088128L;
	private String severityUuid;
	private int severityStatus;
	private String severityName;
	private String severityDescription;
	private String severityAuthor;

	public String getSeverityUuid() {
		return severityUuid;
	}

	public void setSeverityUuid(String severityUuid) {
		this.severityUuid = severityUuid;
	}

	public int getSeverityStatus() {
		return severityStatus;
	}

	public void setSeverityStatus(int severityStatus) {
		this.severityStatus = severityStatus;
	}

	public String getSeverityName() {
		return severityName;
	}

	public void setSeverityName(String severityName) {
		this.severityName = severityName;
	}

	public String getSeverityDescription() {
		return severityDescription;
	}

	public void setSeverityDescription(String severityDescription) {
		this.severityDescription = severityDescription;
	}

	public String getSeverityAuthor() {
		return severityAuthor;
	}

	public void setSeverityAuthor(String severityAuthor) {
		this.severityAuthor = severityAuthor;
	}

	@Override
	public String toString() {
		return "Severity [severityUuid=" + severityUuid + ", severityStatus=" + severityStatus + ", severityName=" + severityName + ", severityDescription=" + severityDescription
				+ ", severityAuthor=" + severityAuthor + "]";
	}

}
