package org.ihtsdo.document.report.model;

public class UserStatusCount {
	private String date;
	private String userName;
	private String status;

	public UserStatusCount() {
		super();
	}

	public UserStatusCount(String date, String userName, String status) {
		super();
		this.date = date;
		this.userName = userName;
		this.status = status;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof UserStatusCount) {
			UserStatusCount usc = (UserStatusCount) obj;
			return usc.getDate().equals(this.date) && usc.getUserName().equals(this.userName) && usc.getStatus().equals(this.status);
		} else {
			return false;
		}
	}

}
