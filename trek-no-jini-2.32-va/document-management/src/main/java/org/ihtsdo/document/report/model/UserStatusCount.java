/*
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.document.report.model;

/**
 * The Class UserStatusCount.
 */
public class UserStatusCount {
	
	/** The date. */
	private String date;
	
	/** The user name. */
	private String userName;
	
	/** The role. */
	private String role;
	
	/** The status. */
	private String status;

	/**
	 * Instantiates a new user status count.
	 */
	public UserStatusCount() {
		super();
	}

	/**
	 * Instantiates a new user status count.
	 *
	 * @param date the date
	 * @param userName the user name
	 * @param status the status
	 * @param role the role
	 */
	public UserStatusCount(String date, String userName, String status, String role) {
		super();
		this.date = date;
		this.userName = userName;
		this.status = status;
		this.role = role;
	}

	/**
	 * Gets the role.
	 *
	 * @return the role
	 */
	public String getRole() {
		return role;
	}

	/**
	 * Sets the role.
	 *
	 * @param role the new role
	 */
	public void setRole(String role) {
		this.role = role;
	}

	/**
	 * Gets the date.
	 *
	 * @return the date
	 */
	public String getDate() {
		return date;
	}

	/**
	 * Sets the date.
	 *
	 * @param date the new date
	 */
	public void setDate(String date) {
		this.date = date;
	}

	/**
	 * Gets the user name.
	 *
	 * @return the user name
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Sets the user name.
	 *
	 * @param userName the new user name
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Sets the status.
	 *
	 * @param status the new status
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof UserStatusCount) {
			UserStatusCount usc = (UserStatusCount) obj;
			return usc.getDate().equals(this.date) && usc.getUserName().equals(this.userName) && usc.getStatus().equals(this.status);
		} else {
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.date + " " + this.userName + " " + this.role + " " + this.status;
	}

}
